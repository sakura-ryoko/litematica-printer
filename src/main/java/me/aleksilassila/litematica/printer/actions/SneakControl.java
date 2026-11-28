package me.aleksilassila.litematica.printer.actions;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;

/**
 * Owns the fake sneaking state the printer imposes on the player while it clicks
 * blocks (needed so that e.g. a chest is not opened instead of a block being
 * placed next to it).
 *
 * <p>The vanilla client rebuilds {@link Input} from the key bindings every tick
 * but only sends an input packet when the rebuilt state differs from the last one
 * it sent. Writing into the input and sending a single packet therefore leaves
 * the server believing the player is still sneaking once printing stops. This
 * class remembers whether the printer forced sneaking and releases it when the
 * action queue runs dry, instead of spending a whole action on the release.
 */
public final class SneakControl {
    private static boolean forced;

    private SneakControl() {
    }

    public static void set(LocalPlayer player, boolean sneak) {
        if (player == null) {
            return;
        }

        Input input = player.input.keyPresses;

        player.input.keyPresses = new Input(
                input.forward(),
                input.backward(),
                input.left(),
                input.right(),
                input.jump(),
                sneak,
                input.sprint()
        );
        player.connection.send(new ServerboundPlayerInputPacket(player.input.keyPresses));

        forced = sneak;
    }

    /**
     * Sends the release packet if the printer is the reason the player is sneaking.
     */
    public static void releaseIfActive(LocalPlayer player) {
        if (forced) {
            set(player, false);
        }
    }

    public static void reset() {
        forced = false;
    }
}
