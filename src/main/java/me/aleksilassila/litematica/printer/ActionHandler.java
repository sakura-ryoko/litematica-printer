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
    /**
     * {@link #onGameTick()} is called this many times per game tick, see
     * {@code MixinClientPlayerEntity}. The placement clock below counts calls, so the two
     * have to stay in sync.
     */
    public static final int CALLS_PER_TICK = 10;

    private static final int TICKS_PER_SECOND = 20;

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
        int callsPerPlacement = getCallsPerPlacement();
        tick = tick % callsPerPlacement == callsPerPlacement - 1 ? 0 : tick + 1;

        if (tick % callsPerPlacement != 0) {
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

    /**
     * Converts the configured placement rate (blocks per second) into the number of
     * {@link #onGameTick()} calls between two placements. One placement costs one call,
     * and the printer never places more than one block per game tick.
     */
    private static int getCallsPerPlacement() {
        int rate = Math.max(1, Configs.PLACEMENT_RATE.getIntegerValue());
        int calls = (int) Math.round((double) (TICKS_PER_SECOND * CALLS_PER_TICK) / rate);

        return Math.max(CALLS_PER_TICK, calls);
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
