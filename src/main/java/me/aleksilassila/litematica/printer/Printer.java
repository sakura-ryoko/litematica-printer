package me.aleksilassila.litematica.printer;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.actions.Action;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.config.Hotkeys;
import me.aleksilassila.litematica.printer.guides.Guide;
import me.aleksilassila.litematica.printer.guides.Guides;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Printer {
    public static final Logger logger = LogManager.getLogger(PrinterReference.MOD_ID);
    @Nonnull
    public final LocalPlayer player;
    public final ActionHandler actionHandler;
    private final Guides interactionGuides = new Guides();

    public Printer(@Nonnull Minecraft client, @Nonnull LocalPlayer player) {
        this.player = player;
        this.actionHandler = new ActionHandler(client, player);
    }

    public boolean onGameTick() {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
        boolean printMode = Configs.PRINT_MODE.getBooleanValue();
        boolean printPressed = Hotkeys.PRINT.getKeybind().isPressed();
        boolean acceptsActions = actionHandler.acceptsActions();
        boolean mayBuild = player.getAbilities().mayBuild;
        boolean interactBlocks = Configs.INTERACT_BLOCKS.getBooleanValue();

        int reachableCount = 0;
        int candidateCount = 0;
        int mismatchCount = 0;
        int guidesCount = 0;
        int canExecuteTrueCount = 0;
        int skippedByGuideCount = 0;
        int actionBatchesQueued = 0;

        if (!acceptsActions) {
            PlacementDebug.log("tick gated acceptsActions=false printMode={} printPressed={} mayBuild={} worldSchematicNull={} queueSize={}",
                    printMode, printPressed, mayBuild, worldSchematic == null, actionHandler.getQueueSize());
            return false;
        }

        if (worldSchematic == null) {
            PlacementDebug.log("tick gated worldSchematicNull=true printMode={} printPressed={} mayBuild={} acceptsActions={}",
                    printMode, printPressed, mayBuild, acceptsActions);
            return false;
        }

        if (!printMode && !printPressed) {
            PlacementDebug.log("tick gated printDisabled printMode={} printPressed={} mayBuild={} acceptsActions={} worldSchematicNull=false",
                    printMode, printPressed, mayBuild, acceptsActions);
            return false;
        }

        Abilities abilities = player.getAbilities();
        if (!abilities.mayBuild) {
            PlacementDebug.log("tick gated mayBuild=false printMode={} printPressed={} acceptsActions={} worldSchematicNull=false",
                    printMode, printPressed, acceptsActions);
            return false;
        }

        List<BlockPos> positions = getReachablePositions();
        reachableCount = positions.size();
        findBlock:
        for (BlockPos position : positions) {
            candidateCount++;
            SchematicBlockState state = new SchematicBlockState(player.level(), worldSchematic, position);
            if (state.targetState.equals(state.currentState) || state.targetState.isAir()) {
                continue;
            }
            mismatchCount++;
            PlacementDebug.log("candidate pos={} target={} current={}",
                    PlacementDebug.pos(position), state.targetState, state.currentState);

            Guide[] guides = interactionGuides.getInteractionGuides(state);
            guidesCount += guides.length;
            PlacementDebug.log("candidate guides pos={} count={}", PlacementDebug.pos(position), guides.length);

            BlockHitResult result = RayTraceUtils.traceToSchematicWorld(player, 10, true, true);
            boolean isCurrentlyLookingSchematic = result != null && result.getBlockPos().equals(position);
            PlacementDebug.log("candidate look pos={} lookingSchematic={} tracePos={}",
                    PlacementDebug.pos(position), isCurrentlyLookingSchematic,
                    result == null ? "null" : PlacementDebug.pos(result.getBlockPos()));

            for (Guide guide : guides) {
                // Add INTERACT_BLOCKS pull by DarkReaper231
                boolean canExecute = guide.canExecute(player);
                if (canExecute) {
                    canExecuteTrueCount++;
                }
                PlacementDebug.log("guide decision pos={} guide={} canExecute={} interactBlocks={}",
                        PlacementDebug.pos(position), guide.getClass().getSimpleName(), canExecute, interactBlocks);
                if (canExecute && interactBlocks) {
                    printDebug("Executing {} for {}", guide, state);
                    List<Action> actions = guide.execute(player);
                    PlacementDebug.log("guide actions pos={} guide={} actionCount={}",
                            PlacementDebug.pos(position), guide.getClass().getSimpleName(), actions.size());
                    actionHandler.addActions(actions.toArray(Action[]::new));
                    actionBatchesQueued++;
                    PlacementDebug.log("tick queued reachable={} checked={} mismatches={} guides={} canExecuteTrue={} skippedByGuide={} actionBatches={} queueSize={}",
                            reachableCount, candidateCount, mismatchCount, guidesCount, canExecuteTrueCount, skippedByGuideCount,
                            actionBatchesQueued, actionHandler.getQueueSize());
                    return true;
                }
                if (canExecute && !interactBlocks) {
                    PlacementDebug.log("guide blockedByConfig pos={} guide={} reason=INTERACT_BLOCKS false",
                            PlacementDebug.pos(position), guide.getClass().getSimpleName());
                }
                if (guide.skipOtherGuides()) {
                    skippedByGuideCount++;
                    PlacementDebug.log("guide skipOtherGuides pos={} guide={}",
                            PlacementDebug.pos(position), guide.getClass().getSimpleName());
                    continue findBlock;
                }
            }
        }

        PlacementDebug.log("tick done reachable={} checked={} mismatches={} guides={} canExecuteTrue={} skippedByGuide={} actionBatches={} queueSize={} printMode={} printPressed={} mayBuild={} acceptsActions={} worldSchematicNull=false",
                reachableCount, candidateCount, mismatchCount, guidesCount, canExecuteTrueCount, skippedByGuideCount,
                actionBatchesQueued, actionHandler.getQueueSize(), printMode, printPressed, mayBuild, acceptsActions);
        return false;
    }

    private List<BlockPos> getReachablePositions() {
        int maxReach = (int) Math.ceil(Configs.PRINTING_RANGE.getDoubleValue());
        double maxReachSquared = Mth.square(Configs.PRINTING_RANGE.getDoubleValue());

        ArrayList<BlockPos> positions = new ArrayList<>();

        for (int y = -maxReach; y < maxReach + 1; y++) {
            for (int x = -maxReach; x < maxReach + 1; x++) {
                for (int z = -maxReach; z < maxReach + 1; z++) {
                    BlockPos blockPos = player.blockPosition().north(x).west(z).above(y);

                    if (!DataManager.getRenderLayerRange().isPositionWithinRange(blockPos)) {
                        continue;
                    }
                    if (this.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(blockPos)) > maxReachSquared) {
                        continue;
                    }

                    positions.add(blockPos);
                }
            }
        }

        return positions.stream()
                .filter(p ->
                {
                    Vec3 vec = Vec3.atCenterOf(p);
                    return this.player.position().distanceToSqr(vec) > 1
                            && this.player.getEyePosition().distanceToSqr(vec) > 1;
                })
                .sorted((a, b) ->
                {
                    double aDistance = this.player.position().distanceToSqr(Vec3.atCenterOf(a));
                    double bDistance = this.player.position().distanceToSqr(Vec3.atCenterOf(b));
                    return Double.compare(aDistance, bDistance);
                }).toList();
    }

    public static void printDebug(String key, Object... args) {
        if (Configs.PRINT_DEBUG.getBooleanValue()) {
            logger.info(key, args);
        }
    }
}
