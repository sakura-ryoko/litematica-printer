package me.aleksilassila.litematica.printer.implementation.actions;

import me.aleksilassila.litematica.printer.PlacementDebug;
import me.aleksilassila.litematica.printer.actions.InteractAction;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class InteractActionImpl extends InteractAction {
    public InteractActionImpl(PrinterPlacementContext context) {
        super(context);
    }

    @Override
    protected void interact(Minecraft client, LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        if (client.gameMode != null) {
            InteractionResult useItemOnResult = client.gameMode.useItemOn(player, hand, hitResult);
            PlacementDebug.log("interact useItemOn result={} hand={} hitBlock={} side={} hit={} selectedItem={}",
                    useItemOnResult, hand, PlacementDebug.pos(hitResult.getBlockPos()), hitResult.getDirection(),
                    hitResult.getLocation(), PlacementDebug.stack(player.getItemInHand(hand)));
            InteractionResult useItemResult = client.gameMode.useItem(player, hand);
            PlacementDebug.log("interact useItem result={} hand={} selectedItem={}",
                    useItemResult, hand, PlacementDebug.stack(player.getItemInHand(hand)));
        } else {
            PlacementDebug.log("interact skipped reason=gameMode-null");
        }
    }
}
