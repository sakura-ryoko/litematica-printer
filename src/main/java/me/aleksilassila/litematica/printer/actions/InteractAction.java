package me.aleksilassila.litematica.printer.actions;

import me.aleksilassila.litematica.printer.Printer;
import me.aleksilassila.litematica.printer.PlacementDebug;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

abstract public class InteractAction extends Action {
    public final PrinterPlacementContext context;

    public InteractAction(PrinterPlacementContext context) {
        this.context = context;
    }

    protected abstract void interact(Minecraft client, LocalPlayer player, InteractionHand hand, BlockHitResult hitResult);

    @Override
    public void send(Minecraft client, LocalPlayer player) {
        PlacementDebug.log("interact send begin pos={} hitBlock={} side={} hit={} hand={} selectedSlot={} selectedItem={} beforeState={}",
                PlacementDebug.pos(context.getClickedPos()), PlacementDebug.pos(context.hitResult.getBlockPos()),
                context.hitResult.getDirection(), context.hitResult.getLocation(), InteractionHand.MAIN_HAND,
                player.getInventory().getSelectedSlot(), PlacementDebug.stack(player.getMainHandItem()),
                player.level().getBlockState(context.getClickedPos()));
        interact(client, player, InteractionHand.MAIN_HAND, context.hitResult);
        PlacementDebug.log("interact send after pos={} afterState={}",
                PlacementDebug.pos(context.getClickedPos()), player.level().getBlockState(context.getClickedPos()));
        Printer.printDebug("InteractAction.send: Blockpos: {} Side: {} HitPos: {}", context.getClickedPos(), context.getClickedFace(), context.getClickLocation());
    }

    @Override
    public String toString() {
        return "InteractAction{" +
                "context=" + context +
                '}';
    }
}
