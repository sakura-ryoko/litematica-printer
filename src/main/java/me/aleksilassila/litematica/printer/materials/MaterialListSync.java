package me.aleksilassila.litematica.printer.materials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListUtils;
import fi.dy.masa.malilib.util.ItemType;

import me.aleksilassila.litematica.printer.Printer;
import me.aleksilassila.litematica.printer.config.Configs;

/**
 * Keeps Litematica's material list in sync with the blocks the printer places.
 *
 * <p>The "missing" counts of a material list are a snapshot: {@code TaskCountBlocksPlacement}
 * produces them when the list is created or explicitly refreshed, and nothing recomputes them while
 * printing. The printer keeps consuming items, so "available" drops with every placed block while
 * the stored demand stays frozen, and the gap that the material list shows grows with every block
 * the printer places.
 *
 * <p>Every confirmed print therefore decrements the matching entries right away, using the same
 * state -> item mapping as {@code MaterialListUtils.convertStatesToStacks()}. Deliberately no
 * counting task is scheduled from here: Litematica's chunk counting never finishes while parts of
 * the placement are outside the loaded area, which on servers is the normal case for big builds.
 *
 * <p>HUD renderers that keep their own copy of the counting result instead of reading the entries
 * are handled separately, see {@link QuickCraftHudSync}.
 */
public final class MaterialListSync
{
    /** How many ticks to wait for a queued print to actually show up in the world. */
    private static final int PENDING_TIMEOUT_TICKS = 300;

    /** Client thread only. */
    private static final Map<BlockPos, PendingPlacement> PENDING = new HashMap<>();

    private MaterialListSync() { }

    private static class PendingPlacement
    {
        private final BlockState targetState;
        private final boolean wasMismatched;
        private int ticksLeft = PENDING_TIMEOUT_TICKS;

        private PendingPlacement(BlockState targetState, boolean wasMismatched)
        {
            this.targetState = targetState;
            this.wasMismatched = wasMismatched;
        }
    }

    /**
     * Called right before the printer queues the actions for one schematic block.
     * The block is only accounted for once it actually shows up in the client world,
     * so failed placements don't corrupt the material list.
     */
    public static void onPrintQueued(Level world, BlockPos pos, BlockState targetState)
    {
        if (Configs.MATERIAL_LIST_SYNC.getBooleanValue() == false)
        {
            return;
        }

        if (targetState == null || targetState.isAir())
        {
            return;
        }

        BlockState currentState = world.getBlockState(pos);
        // Air means the block was simply missing, anything else means it had a wrong state/block.
        PENDING.put(pos.immutable(), new PendingPlacement(targetState, currentState.isAir() == false));
    }

    /** Called once per client tick, after the printer has processed its action queue. */
    public static void clientTick(LocalPlayer player)
    {
        if (player == null)
        {
            return;
        }

        if (Configs.MATERIAL_LIST_SYNC.getBooleanValue() == false)
        {
            PENDING.clear();
            return;
        }

        if (PENDING.isEmpty())
        {
            return;
        }

        Level world = player.level();
        Iterator<Map.Entry<BlockPos, PendingPlacement>> iterator = PENDING.entrySet().iterator();

        while (iterator.hasNext())
        {
            Map.Entry<BlockPos, PendingPlacement> entry = iterator.next();
            PendingPlacement pending = entry.getValue();
            BlockState currentState = world.getBlockState(entry.getKey());

            if (currentState == pending.targetState || currentState.equals(pending.targetState))
            {
                iterator.remove();
                applyPlacement(player, pending);
            }
            else if (--pending.ticksLeft <= 0)
            {
                // The placement never materialized (ghost block, server denial, ...).
                iterator.remove();
                Printer.printDebug("MaterialListSync: dropping unresolved print at {}", entry.getKey());
            }
        }
    }

    private static void applyPlacement(LocalPlayer player, PendingPlacement pending)
    {
        MaterialListBase materialList = DataManager.getMaterialList();

        if (materialList == null)
        {
            Printer.printDebug("MaterialListSync: no active material list, skipping decrement");
            return;
        }

        // HUDs that render their own copy of the counting result (QuickCraft) ignore the entries.
        QuickCraftHudSync.onBlockPlaced(materialList, pending.targetState);

        ImmutableList<MaterialListEntry> entries = materialList.getMaterialsAll();

        if (entries.isEmpty())
        {
            return;
        }

        List<MaterialListEntry> updated = decrementMissing(entries, pending.targetState, pending.wasMismatched);

        if (updated == null)
        {
            Printer.printDebug("MaterialListSync: {} not in the material list of '{}'",
                               pending.targetState.getBlock(), materialList.getName());
            return;
        }

        materialList.setMaterialListEntries(updated);
        MaterialListUtils.updateAvailableCounts(materialList.getMaterialsAll(), player);
        Printer.printDebug("MaterialListSync: decremented counts for {} in '{}'",
                           pending.targetState.getBlock(), materialList.getName());
    }

    /**
     * Returns a new entry list with the counts for the given block state decremented,
     * or null if nothing in the list matched (e.g. the list belongs to another schematic).
     */
    private static List<MaterialListEntry> decrementMissing(ImmutableList<MaterialListEntry> entries,
                                                            BlockState targetState,
                                                            boolean wasMismatched)
    {
        Object2IntOpenHashMap<ItemType> required = getRequiredItems(targetState);

        if (required.isEmpty())
        {
            return null;
        }

        List<MaterialListEntry> updated = new ArrayList<>(entries.size());
        boolean changed = false;

        for (MaterialListEntry entry : entries)
        {
            ItemType itemType = new ItemType(entry.getStack(), true, false);
            int amount = required.getInt(itemType);

            if (amount <= 0)
            {
                updated.add(entry);
                continue;
            }

            int missing = Math.max(0, entry.getCountMissing() - amount);
            int mismatched = wasMismatched ? Math.max(0, entry.getCountMismatched() - amount) : entry.getCountMismatched();

            updated.add(new MaterialListEntry(entry.getStack(), entry.getCountTotal(), missing, mismatched,
                                              entry.getCountAvailable()));
            changed = true;
        }

        return changed ? updated : null;
    }

    /**
     * Mirrors Litematica's own {@code MaterialListUtils.convertStatesToStacks()} mapping so the
     * counts we decrement are exactly the ones that were added when the list was counted.
     */
    private static Object2IntOpenHashMap<ItemType> getRequiredItems(BlockState state)
    {
        Object2IntOpenHashMap<ItemType> items = new Object2IntOpenHashMap<>();
        MaterialCache cache = MaterialCache.getInstance();
        boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED) &&
                              state.getValue(BlockStateProperties.WATERLOGGED);
        BlockState stateToConvert = waterlogged ? state.setValue(BlockStateProperties.WATERLOGGED, false) : state;

        if (waterlogged)
        {
            items.addTo(new ItemType(new ItemStack(Items.WATER_BUCKET), true, false), 1);
        }

        if (cache.requiresMultipleItems(stateToConvert))
        {
            for (ItemStack stack : cache.getItems(stateToConvert))
            {
                if (stack.isEmpty() == false)
                {
                    items.addTo(new ItemType(stack, true, false), stack.getCount());
                }
            }
        }
        else
        {
            ItemStack stack = cache.getRequiredBuildItemForState(stateToConvert);

            if (stack.isEmpty() == false)
            {
                items.addTo(new ItemType(stack, true, false), stack.getCount());
            }
        }

        return items;
    }
}
