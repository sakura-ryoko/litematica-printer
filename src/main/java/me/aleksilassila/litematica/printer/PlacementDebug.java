package me.aleksilassila.litematica.printer;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class PlacementDebug {
    private static final boolean ENABLED = Boolean.getBoolean("litematica_printer.debugPlacement");
    private static final int MAX_LINES_PER_SECOND = 80;
    private static long windowStartMillis = System.currentTimeMillis();
    private static int windowLineCount = 0;
    private static int suppressedLineCount = 0;

    private PlacementDebug() {
    }

    public static boolean enabled() {
        return ENABLED;
    }

    public static void log(String message, Object... args) {
        if (ENABLED) {
            long now = System.currentTimeMillis();
            if (now - windowStartMillis >= 1000) {
                if (suppressedLineCount > 0) {
                    Printer.logger.info("[placement] suppressed {} debug lines in previous second", suppressedLineCount);
                }
                windowStartMillis = now;
                windowLineCount = 0;
                suppressedLineCount = 0;
            }
            if (windowLineCount++ >= MAX_LINES_PER_SECOND) {
                suppressedLineCount++;
                return;
            }
            Printer.logger.info("[placement] " + message, args);
        }
    }

    public static String stack(ItemStack stack) {
        if (stack == null) {
            return "null";
        }
        if (stack.isEmpty()) {
            return "empty";
        }
        return stack.getCount() + "x" + stack.getItem();
    }

    public static String inventoryLocation(LocalPlayer player, ItemStack stack) {
        if (player.getAbilities().instabuild) {
            return "creative";
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getNonEquipmentItems().size(); ++i) {
            ItemStack inventoryStack = inventory.getNonEquipmentItems().get(i);
            if (!inventoryStack.isEmpty() && ItemStack.isSameItem(inventoryStack, stack)) {
                return Inventory.isHotbarSlot(i) ? "hotbar:" + i : "inventory:" + i;
            }
        }
        return "missing";
    }

    public static String pos(BlockPos pos) {
        return pos == null ? "null" : pos.toShortString();
    }
}
