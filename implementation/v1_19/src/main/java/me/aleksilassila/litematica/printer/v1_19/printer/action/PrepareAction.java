package me.aleksilassila.litematica.printer.v1_19.printer.action;

import me.aleksilassila.litematica.printer.v1_19.implementations.Implementation;
import me.aleksilassila.litematica.printer.v1_19.printer.PrinterPlacementContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class PrepareAction extends AbstractAction {
//    public final Direction lookDirection;
//    public final boolean requireSneaking;
//    public final Item item;

//    public PrepareAction(Direction lookDirection, boolean requireSneaking, Item item) {
//        this.lookDirection = lookDirection;
//        this.requireSneaking = requireSneaking;
//        this.item = item;
//    }
//
//    public PrepareAction(Direction lookDirection, boolean requireSneaking, BlockState requiredState) {
//        this(lookDirection, requireSneaking, requiredState.getBlock().asItem());
//    }

    public final PrinterPlacementContext context;

    public PrepareAction(PrinterPlacementContext context) {
        this.context = context;
    }

    @Override
    public Direction lockedLookDirection() {
        return context.lookDirection;
    }

    @Override
    public void send(MinecraftClient client, ClientPlayerEntity player) {
        ItemStack itemStack = context.getStack();
        if (itemStack != null) {
            PlayerInventory inventory = Implementation.getInventory(player);
            int i = inventory.getSlotWithStack(itemStack);

            // This thing is straight from MinecraftClient#doItemPick()
            if (Implementation.getAbilities(player).creativeMode) {
                inventory.addPickBlock(itemStack);
                client.interactionManager.clickCreativeStack(player.getStackInHand(Hand.MAIN_HAND), 36 + inventory.selectedSlot);
            } else if (i != -1) {
                if (PlayerInventory.isValidHotbarIndex(i)) {
                    inventory.selectedSlot = i;
                } else {
                    client.interactionManager.pickFromInventory(i);
                }
            }

            //            if (Implementation.getAbilities(player).creativeMode) {
////                player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(Implementation.getInventory(player).selectedSlot, context.getStack()));
//                Implementation.getInventory(player);
//
//            } else {
//                int slot = getItemSlot(player, context.getStack().getItem());
//                if (slot >= 0)
//                    player.networkHandler.sendPacket(new PickFromInventoryC2SPacket(slot));
//            }
        }

        if (context.lookDirection != null) {
            Implementation.sendLookPacket(player, context.lookDirection);
        }

        if (context.requiresSneaking) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
    }

    @Override
    public String toString() {
        return "PrepareAction{" +
                "context=" + context +
                '}';
    }
}
