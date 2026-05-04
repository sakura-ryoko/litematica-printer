package me.aleksilassila.litematica.printer.guides;

import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.PlacementDebug;
import me.aleksilassila.litematica.printer.actions.Action;
import me.aleksilassila.litematica.printer.implementation.BlockHelperImpl;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CoralPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

abstract public class Guide extends BlockHelperImpl {
    protected final SchematicBlockState state;
    protected final BlockState currentState;
    protected final BlockState targetState;

    public Guide(SchematicBlockState state) {
        this.state = state;
        this.currentState = state.currentState;
        this.targetState = state.targetState;
    }

    protected boolean playerHasRightItem(LocalPlayer player) {
        int slot = getRequiredItemStackSlot(player);
        PlacementDebug.log("guide required target={} guide={} slot={} hasItem={}",
                targetState, getClass().getSimpleName(), slot, slot != -1);
        return slot != -1;
    }

    protected int getSlotWithItem(LocalPlayer player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getNonEquipmentItems().size(); ++i) {
            if (itemStack.isEmpty() && inventory.getNonEquipmentItems().get(i).is(itemStack.getItem())) {
                return i;
            }
            if (!inventory.getNonEquipmentItems().get(i).isEmpty() && ItemStack.isSameItem(inventory.getNonEquipmentItems().get(i), itemStack)) {
                return i;
            }
        }

        return -1;
    }

    protected int getRequiredItemStackSlot(LocalPlayer player) {
        if (player.getAbilities().instabuild) {
            return player.getInventory().getSelectedSlot();
        }

        Optional<ItemStack> requiredItem = getRequiredItem(player);
        return requiredItem.map(itemStack -> getSlotWithItem(player, itemStack)).orElse(-1);
    }

    public boolean canExecute(LocalPlayer player) {
        if (!playerHasRightItem(player)) {
            PlacementDebug.log("guide canExecute false reason=missing-item guide={} target={} current={}",
                    getClass().getSimpleName(), targetState, currentState);
            return false;
        }

        BlockState targetState = state.targetState;
        BlockState currentState = state.currentState;

        boolean mismatch = !statesEqual(targetState, currentState);
        if (!mismatch) {
            PlacementDebug.log("guide canExecute false reason=wrong-current-block-or-state-already-matches guide={} target={} current={}",
                    getClass().getSimpleName(), targetState, currentState);
        }
        return mismatch;
    }

    abstract public @Nonnull List<Action> execute(LocalPlayer player);

    abstract protected @Nonnull List<ItemStack> getRequiredItems();

    /**
     * Returns the first required item that the player has access to,
     * or empty if the items are inaccessible.
     */
    protected Optional<ItemStack> getRequiredItem(LocalPlayer player) {
        List<ItemStack> requiredItems = getRequiredItems();
        if (PlacementDebug.enabled()) {
            for (ItemStack requiredItem : requiredItems) {
                PlacementDebug.log("guide requiredItem target={} guide={} item={} location={}",
                        targetState, getClass().getSimpleName(), PlacementDebug.stack(requiredItem),
                        PlacementDebug.inventoryLocation(player, requiredItem));
            }
        }

        for (ItemStack requiredItem : requiredItems) {
            if (player.getAbilities().instabuild) {
                return Optional.of(requiredItem);
            }

            int slot = getSlotWithItem(player, requiredItem);
            if (slot > -1) {
                return Optional.of(requiredItem);
            }
        }

        return Optional.empty();
    }

    protected boolean statesEqualIgnoreProperties(BlockState state1, BlockState state2,
                                                  Property<?>... propertiesToIgnore) {
        if (state1.getBlock() != state2.getBlock()) {
            return false;
        }

        loop:
        for (Property<?> property : state1.getProperties()) {
            if (property == BlockStateProperties.WATERLOGGED && !(state1.getBlock() instanceof CoralPlantBlock)) {
                continue;
            }

            for (Property<?> ignoredProperty : propertiesToIgnore) {
                if (property == ignoredProperty) {
                    continue loop;
                }
            }

            try {
                if (!state1.getValue(property).equals(state2.getValue(property))) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    protected static <T extends Comparable<T>> Optional<T> getProperty(BlockState blockState, Property<T> property) {
        if (blockState.hasProperty(property)) {
            return Optional.of(blockState.getValue(property));
        }
        return Optional.empty();
    }

    /**
     * Returns true if
     */
    protected boolean statesEqual(BlockState state1, BlockState state2) {
        return statesEqualIgnoreProperties(state1, state2);
    }

    public boolean skipOtherGuides() {
        return false;
    }
}
