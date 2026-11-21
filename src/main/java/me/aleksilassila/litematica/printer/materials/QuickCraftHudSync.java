package me.aleksilassila.litematica.printer.materials;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.state.BlockState;

import fi.dy.masa.litematica.materials.MaterialListBase;

import me.aleksilassila.litematica.printer.Printer;

/**
 * Keeps QuickCraft's material list HUD in sync with the blocks the printer places.
 *
 * <p>QuickCraft ({@code quickcraft}, com.yiyihehe.quickcraft) hooks Litematica's material list and
 * renders its HUD from its own snapshot of the counting task result: on every
 * {@code TaskCountBlocksPlacement} completion its {@code LitematicaProjectionCountFinishMixin} calls
 * {@code QuickLitematicaProjectionMaterials.prepareEditor(...)}, which stores a {@code Counts}
 * record (block state -> count maps) per material list, and {@code
 * LitematicaMaterialListHudRendererMixin} then rebuilds the HUD rows from that record instead of
 * from {@link fi.dy.masa.litematica.materials.MaterialListEntry}. Writing the Litematica entries
 * therefore has no visible effect on that HUD at all - which is why the material list gap keeps
 * growing while printing in a QuickCraft environment.
 *
 * <p>There is no public API for this, so the snapshot is adjusted reflectively: the count for the
 * placed block state is decremented and the HUD cache entry is dropped, so the next HUD frame
 * rebuilds from the updated snapshot. Everything is optional and fails soft: if QuickCraft is not
 * installed, or its internals changed, this class logs once and does nothing.
 */
public final class QuickCraftHudSync
{
    private static final String MOD_ID = "quickcraft";
    private static final String CLASS_NAME = "com.yiyihehe.quickcraft.litematica.QuickLitematicaProjectionMaterials";

    private static boolean initialised;
    private static boolean unavailable;
    private static Field countsField;
    private static Field hudCacheField;
    private static Method missingAccessor;

    private QuickCraftHudSync() { }

    /**
     * Decrements QuickCraft's HUD snapshot for the placed block state.
     *
     * @return true when a snapshot entry was updated
     */
    public static boolean onBlockPlaced(MaterialListBase materialList, BlockState state)
    {
        if (materialList == null || state == null || initialise() == false)
        {
            return false;
        }

        try
        {
            Map<?, ?> counts = (Map<?, ?>) countsField.get(null);
            Object snapshot = counts.get(materialList);

            if (snapshot == null)
            {
                // This list was never counted, so QuickCraft renders the Litematica entries directly.
                return false;
            }

            @SuppressWarnings("unchecked")
            Object2IntOpenHashMap<BlockState> missing =
                    (Object2IntOpenHashMap<BlockState>) missingAccessor.invoke(snapshot);
            int current = missing.getInt(state);

            if (current <= 0)
            {
                return false;
            }

            missing.put(state, current - 1);

            // The HUD cache is keyed on the snapshot instance, whose identity didn't change.
            Map<?, ?> hudCache = (Map<?, ?>) hudCacheField.get(null);
            hudCache.remove(materialList);

            return true;
        }
        catch (Throwable t)
        {
            disable(t);
            return false;
        }
    }

    private static boolean initialise()
    {
        if (initialised)
        {
            return unavailable == false;
        }

        initialised = true;

        if (FabricLoader.getInstance().isModLoaded(MOD_ID) == false)
        {
            unavailable = true;
            return false;
        }

        try
        {
            Class<?> target = Class.forName(CLASS_NAME);
            Class<?> countsType = Class.forName(CLASS_NAME + "$Counts");

            countsField = target.getDeclaredField("COUNTS");
            countsField.setAccessible(true);

            hudCacheField = target.getDeclaredField("HUD_CACHE");
            hudCacheField.setAccessible(true);

            missingAccessor = countsType.getMethod("missing");
            missingAccessor.setAccessible(true);

            Printer.printDebug("QuickCraftHudSync: QuickCraft found, material list HUD will be kept in sync");
            return true;
        }
        catch (Throwable t)
        {
            disable(t);
            return false;
        }
    }

    private static void disable(Throwable t)
    {
        unavailable = true;
        Printer.logger.warn("QuickCraftHudSync: QuickCraft's material list HUD internals don't match, " +
                            "its HUD will keep showing the counts of the last re-count", t);
    }
}
