package me.aleksilassila.litematica.printer.actions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;
import me.aleksilassila.litematica.printer.PlacementDebug;

public class ReleaseShiftAction extends Action {
    @Override
    public void send(Minecraft client, LocalPlayer player) {
        player.input.keyPresses = new Input(player.input.keyPresses.forward(), player.input.keyPresses.backward(), player.input.keyPresses.left(), player.input.keyPresses.right(), player.input.keyPresses.jump(), false, player.input.keyPresses.sprint());
        player.connection.send(new ServerboundPlayerInputPacket(player.input.keyPresses));
        PlacementDebug.log("releaseShift sent");
    }
}
