package me.aleksilassila.litematica.printer.actions;

import me.aleksilassila.litematica.printer.Printer;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import fi.dy.masa.litematica.util.InventoryUtils;

public class PrepareAction extends Action {
    public final PrinterPlacementContext context;
    public boolean modifyYaw = true;
    public boolean modifyPitch = true;
    public float yaw = 0;
    public float pitch = 0;

    public PrepareAction(PrinterPlacementContext context) {
        this.context = context;
        Direction lookDirection = context.lookDirection;

        if (lookDirection != null && lookDirection.getAxis().isHorizontal()) {
            this.yaw = lookDirection.toYRot();
        } else {
            this.modifyYaw = false;
        }

        if (lookDirection == Direction.UP) {
            this.pitch = -90;
        } else if (lookDirection == Direction.DOWN) {
            this.pitch = 90;
        } else if (lookDirection != null) {
            this.pitch = 0;
        } else {
            this.modifyPitch = false;
        }
    }

    public PrepareAction(PrinterPlacementContext context, float yaw, float pitch) {
        this.context = context;

        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void send(Minecraft client, LocalPlayer player) {
        ItemStack itemStack = context.getItemInHand();
        int slot = context.requiredItemSlot;

        if (itemStack != null && !itemStack.isEmpty() && client.gameMode != null) {
            Printer.printDebug("PrepareAction#send(): slot [{}] // itemStack [{}]", slot, itemStack.toString());
            // This thing is straight from MinecraftClient#doItemPick()
            Inventory inventory = player.getInventory();

            if (player.getAbilities().instabuild) {
                this.addPickBlock(inventory, itemStack);
                client.gameMode.handleCreativeModeItemAdd(player.getItemInHand(InteractionHand.MAIN_HAND), 36 + inventory.getSelectedSlot());
            } else if (slot != -1) {
                if (Inventory.isHotbarSlot(slot)) {
                    inventory.setSelectedSlot(slot);
                } else {
                    // TODO --> test this (pickFromInventory has been REMOVED)
                    //client.interactionManager.pickFromInventory(slot);
                    InventoryUtils.setPickedItemToHand(slot, itemStack, client);
                }
            }
        }

        if (Configs.ROTATE.getBooleanValue()) {
            if (modifyPitch || modifyYaw) {
                float yaw = modifyYaw ? this.yaw : player.getYRot();
                float pitch = modifyPitch ? this.pitch : player.getXRot();

                ServerboundMovePlayerPacket packet = new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), yaw,
                        pitch, player.onGround(), player.horizontalCollision);

                player.connection.send(packet);
            }
        }

        if (context.shouldSneak) {
            player.input.keyPresses = new Input(player.input.keyPresses.forward(), player.input.keyPresses.backward(), player.input.keyPresses.left(), player.input.keyPresses.right(), player.input.keyPresses.jump(), true, player.input.keyPresses.sprint());
            player.connection.send(new ServerboundPlayerInputPacket(player.input.keyPresses));
        } else {
            player.input.keyPresses = new Input(player.input.keyPresses.forward(), player.input.keyPresses.backward(), player.input.keyPresses.left(), player.input.keyPresses.right(), player.input.keyPresses.jump(), false, player.input.keyPresses.sprint());
            player.connection.send(new ServerboundPlayerInputPacket(player.input.keyPresses));
        }
    }

    private void addPickBlock(Inventory inv, ItemStack stack) {
        int slot = inv.findSlotMatchingItem(stack);

        if (Inventory.isHotbarSlot(slot)) {
            inv.setSelectedSlot(slot);
        } else {
            if (slot == -1) {
                inv.setSelectedSlot(inv.getSuitableHotbarSlot());

                if (!inv.getNonEquipmentItems().get(inv.getSelectedSlot()).isEmpty()) {
                    int empty = inv.getFreeSlot();

                    if (empty != -1) {
                        inv.getNonEquipmentItems().set(empty, inv.getNonEquipmentItems().get(inv.getSelectedSlot()));
                    }
                }
                inv.getNonEquipmentItems().set(inv.getSelectedSlot(), stack);
            } else {
                inv.pickSlot(slot);
            }
        }
    }

    @Override
    public String toString() {
        return "PrepareAction{" +
                "context=" + context +
                '}';
    }
}
