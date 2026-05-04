package me.aleksilassila.litematica.printer;

import me.aleksilassila.litematica.printer.actions.Action;
import me.aleksilassila.litematica.printer.actions.PrepareAction;
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

        if (nextAction != null) {
            PlacementDebug.log("action tick send action={} queueSizeAfterPoll={}", nextAction.getClass().getSimpleName(), actionQueue.size());
            Printer.printDebug("Sending action {}", nextAction);
            nextAction.send(client, player);
        } else {
            PlacementDebug.log("action tick empty queueSize=0");
            lookAction = null;
        }
    }

    public boolean acceptsActions() {
        return actionQueue.isEmpty();
    }

    public int getQueueSize() {
        return actionQueue.size();
    }

    public void addActions(Action... actions) {
        PlacementDebug.log("actions addActions called count={} acceptsActions={} queueSizeBefore={}",
                actions.length, acceptsActions(), actionQueue.size());
        if (!acceptsActions()) {
            PlacementDebug.log("actions addActions ignored reason=queue-not-empty queueSize={}", actionQueue.size());
            return;
        }

        for (Action action : actions) {
            if (action instanceof PrepareAction) {
                lookAction = (PrepareAction) action;
            }
        }

        actionQueue.addAll(List.of(actions));
        PlacementDebug.log("actions addActions queued count={} queueSizeAfter={}", actions.length, actionQueue.size());
    }
}
