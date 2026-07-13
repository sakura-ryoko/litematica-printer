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

        if (!actionHandler.acceptsActions()) {
            return false;
        }

        if (worldSchematic == null) {
            return false;
        }

        if (!Configs.PRINT_MODE.getBooleanValue() && !Hotkeys.PRINT.getKeybind().isPressed()) {
            return false;
        }

        Abilities abilities = player.getAbilities();
        if (!abilities.mayBuild) {
            return false;
        }

        List<BlockPos> positions = getReachablePositions();
        findBlock:
        for (BlockPos position : positions) {
            SchematicBlockState state = new SchematicBlockState(player.level(), worldSchematic, position);
            if (!state.targetState.isAir()) {
                printDebug("Considering {} target={} current={}", position, state.targetState, state.currentState);
            }
            if (state.targetState.equals(state.currentState) || state.targetState.isAir()) {
                continue;
            }

            Guide[] guides = interactionGuides.getInteractionGuides(state);

            BlockHitResult result = RayTraceUtils.traceToSchematicWorld(player, 10, true, true);
            boolean isCurrentlyLookingSchematic = result != null && result.getBlockPos().equals(position);

            for (Guide guide : guides) {
                // Add INTERACT_BLOCKS pull by DarkReaper231
                if (guide.canExecute(player) && Configs.INTERACT_BLOCKS.getBooleanValue()) {
                    printDebug("PLACING {} at {} via {}", state.targetState.getBlock(), position, guide.getClass().getSimpleName());
                    printDebug("  targetState={} currentState={} isAir={}", state.targetState, state.currentState, state.targetState.isAir());
                    List<Action> actions = guide.execute(player);
                    actionHandler.addActions(actions.toArray(Action[]::new));
                    return true;
                }
                if (guide.skipOtherGuides()) {
                    continue findBlock;
                }
            }
        }

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
