package me.aleksilassila.litematica.printer.actions;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Keeps track of the placements that have already been sent to the server but
 * have not been confirmed yet by a block state change coming back to the client.
 *
 * <p>Without this the printer keeps re-selecting the same position on every scan
 * until the client world catches up with the server, which on any server with
 * some latency takes several ticks. Those duplicates never place anything new,
 * they only burn actions and make it look like the printer is stuck on a block.
 */
public final class PendingPlacements {
    /**
     * How long to wait for a confirmation before the position is allowed to be
     * attempted again. At the default interval the printer sends roughly one
     * action per game tick, so this is a generous round trip allowance rather
     * than a printing speed limit.
     */
    private static final int TIMEOUT_TICKS = 30;

    private static final Map<BlockPos, Entry> PENDING = new HashMap<>();

    private static Level trackedLevel;
    private static int tick;

    private PendingPlacements() {
    }

    /**
     * Called right after the actions for one placement have been queued.
     *
     * @param previousState the state the position had when the placement was planned
     * @param targetState   the state the schematic wants at this position
     */
    public static void markQueued(Level level, BlockPos pos, BlockState previousState, BlockState targetState) {
        if (level == null || pos == null) {
            return;
        }

        if (level != trackedLevel) {
            PENDING.clear();
            trackedLevel = level;
        }

        PENDING.put(pos.immutable(), new Entry(previousState, targetState, tick));
    }

    /**
     * @return true while a placement for this position is still in flight and
     * should not be planned a second time.
     */
    public static boolean isUnconfirmed(Level level, BlockPos pos) {
        Entry entry = PENDING.get(pos);

        if (entry == null) {
            return false;
        }

        if (isFinished(level, pos, entry)) {
            PENDING.remove(pos);
            return false;
        }

        return true;
    }

    public static void clientTick(LocalPlayer player) {
        tick++;

        if (player == null) {
            reset();
            return;
        }

        Level level = player.level();

        if (trackedLevel != null && trackedLevel != level) {
            reset();
            return;
        }

        if (PENDING.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<BlockPos, Entry>> iterator = PENDING.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Entry> entry = iterator.next();

            if (isFinished(level, entry.getKey(), entry.getValue())) {
                iterator.remove();
            }
        }
    }

    public static void reset() {
        PENDING.clear();
        trackedLevel = null;
    }

    private static boolean isFinished(Level level, BlockPos pos, Entry entry) {
        BlockState currentState = level.getBlockState(pos);

        // The server applied the placement, the usual success case.
        if (currentState == entry.targetState()) {
            return true;
        }

        // The world changed, so the server did something with this position. This
        // also covers placements that happen in several steps (snow layers, candles,
        // sea pickles, ...) where the intermediate state is not the target state yet.
        if (currentState != entry.previousState()) {
            return true;
        }

        // Nothing came back, treat it as a failed attempt and allow a retry.
        return tick - entry.sendTick() > TIMEOUT_TICKS;
    }

    private record Entry(BlockState previousState, BlockState targetState, int sendTick) {
    }
}
