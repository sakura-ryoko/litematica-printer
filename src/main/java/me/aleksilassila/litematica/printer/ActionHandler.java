package me.aleksilassila.litematica.printer;

import me.aleksilassila.litematica.printer.actions.Action;
import me.aleksilassila.litematica.printer.actions.InteractAction;
import me.aleksilassila.litematica.printer.actions.PrepareAction;
import me.aleksilassila.litematica.printer.actions.SneakControl;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class ActionHandler {
    /**
     * {@link #onGameTick()} is called this many times per game tick, see
     * {@code MixinClientPlayerEntity}. The placement clock below counts calls, so the two
     * have to stay in sync.
     */
    public static final int CALLS_PER_TICK = 10;

    private static final int TICKS_PER_SECOND = 20;

    /**
     * How many clock periods to wait for the item that was put into the hand to actually show up
     * there (client side) before the interaction is dropped. One period is the placement interval,
     * so this is several seconds worth of server round trips on a normal connection.
     */
    private static final int MAX_HAND_CONFIRM_RETRIES = 10;

    private final Minecraft client;
    private final LocalPlayer player;
    private final Deque<Action> actionQueue = new LinkedList<>();
    public PrepareAction lookAction = null;

    public ActionHandler(Minecraft client, LocalPlayer player) {
        this.client = client;
        this.player = player;
    }

    private int tick = 0;
    private int handConfirmRetries = 0;

    public void onGameTick() {
        int callsPerPlacement = getCallsPerPlacement();
        tick = tick % callsPerPlacement == callsPerPlacement - 1 ? 0 : tick + 1;

        if (tick % callsPerPlacement != 0) {
            return;
        }

        Action nextAction = actionQueue.peek();

        if (nextAction == null) {
            lookAction = null;
            handConfirmRetries = 0;
            // Nothing left to send, hand the movement input back to the player.
            SneakControl.releaseIfActive(player);
            return;
        }

        if (nextAction instanceof InteractAction interactAction) {
            if (isHoldingRequiredItem(interactAction) == false) {
                if (handConfirmRetries++ < MAX_HAND_CONFIRM_RETRIES) {
                    Printer.printDebug("Holding the wrong item, waiting before {}", interactAction);
                    return;
                }

                Printer.printDebug("Dropping interaction, the required item never reached the hand: {}",
                                   interactAction);
                actionQueue.poll();
                handConfirmRetries = 0;
                return;
            }

            actionQueue.poll();
            handConfirmRetries = 0;
            send(interactAction);
            return;
        }

        actionQueue.poll();
        send(nextAction);

        if (nextAction instanceof PrepareAction prepareAction && needsHandConfirmation(prepareAction)) {
            // The item outside the hotbar is moved through the inventory container, and the client
            // only sees the result after the server acknowledged the click. Sending the interaction
            // in the same tick would click with whatever is still in the hand, so leave it queued
            // for the next interval.
            return;
        }

        // A Prepare action and the interaction that follows it belong to the same placement, so they
        // are sent together instead of one interval apart. The packets still arrive in order on the
        // server, this only removes the idle interval between preparing the hand and clicking.
        Action followUp = actionQueue.peek();

        while (followUp instanceof InteractAction interactAction) {
            if (isHoldingRequiredItem(interactAction) == false) {
                return;
            }

            actionQueue.poll();
            send(interactAction);
            followUp = actionQueue.peek();
        }
    }

    private void send(Action action) {
        Printer.printDebug("Sending action {}", action);
        action.send(client, player);
    }

    /**
     * Items outside the hotbar are put into the hand with a container click, which needs a server
     * round trip, and a local prediction is not guaranteed either - so the click has to wait one
     * interval in both cases.
     */
    private boolean needsHandConfirmation(PrepareAction prepareAction) {
        PrinterPlacementContext context = prepareAction.context;
        ItemStack required = context.getItemInHand();

        if (context.requiredItemSlot < 0 || required.isEmpty()) {
            return false;
        }

        if (Inventory.isHotbarSlot(context.requiredItemSlot) == false) {
            return true;
        }

        return isHolding(required) == false;
    }

    private boolean isHoldingRequiredItem(InteractAction interactAction) {
        return isHolding(interactAction.context.getItemInHand());
    }

    private boolean isHolding(ItemStack required) {
        if (required == null || required.isEmpty()) {
            return true;
        }

        ItemStack held = player.getMainHandItem();

        return held.isEmpty() == false && ItemStack.isSameItem(held, required);
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
        if (acceptsActions() == false) {
            return;
        }

        handConfirmRetries = 0;

        for (Action action : actions) {
            if (action instanceof PrepareAction prepareAction) {
                lookAction = prepareAction;
            }
        }

        actionQueue.addAll(List.of(actions));
    }
}
