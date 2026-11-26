package me.aleksilassila.litematica.printer;

import me.aleksilassila.litematica.printer.actions.Action;
import me.aleksilassila.litematica.printer.actions.InteractAction;
import me.aleksilassila.litematica.printer.actions.PrepareAction;
import me.aleksilassila.litematica.printer.actions.SneakControl;
import me.aleksilassila.litematica.printer.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ActionHandler {
    private final Minecraft client;
    private final LocalPlayer player;
    private final Queue<Action> actionQueue = new LinkedList<>();
    public PrepareAction lookAction = null;

    public ActionHandler(Minecraft client, LocalPlayer player) {
        this.client = client;
        this.player = player;
    }

    private int tick = 0;

    public void onGameTick() {
        int tickRate = Configs.PRINTING_INTERVAL.getIntegerValue();
        tick = tick % tickRate == tickRate - 1 ? 0 : tick + 1;

        if (tick % tickRate != 0) {
            return;
        }

        Action nextAction = actionQueue.poll();

        if (nextAction == null) {
            lookAction = null;
            // Nothing left to send, hand the movement input back to the player.
            SneakControl.releaseIfActive(player);
            return;
        }

        send(nextAction);

        // A Prepare action and the interaction that follows it belong to the same
        // placement, so they are sent together instead of one interval apart. The
        // packets still arrive in order on the server, this only removes the idle
        // interval between preparing the hand and actually clicking.
        Action followUp = actionQueue.peek();

        while (followUp instanceof InteractAction) {
            actionQueue.poll();
            send(followUp);
            followUp = actionQueue.peek();
        }
    }

    private void send(Action action) {
        Printer.printDebug("Sending action {}", action);
        action.send(client, player);
    }

    public boolean acceptsActions() {
        return actionQueue.isEmpty();
    }

    public void addActions(Action... actions) {
        if (!acceptsActions()) {
            return;
        }

        for (Action action : actions) {
            if (action instanceof PrepareAction) {
                lookAction = (PrepareAction) action;
            }
        }

        actionQueue.addAll(List.of(actions));
    }
}
