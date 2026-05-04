package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.Printer;
import me.aleksilassila.litematica.printer.PlacementDebug;
import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * An old school guide where there are defined specific conditions
 * for player state depending on the block being placed.
 */
public class GeneralPlacementGuide extends PlacementGuide {
    public GeneralPlacementGuide(SchematicBlockState state) {
        super(state);
    }

    protected List<Direction> getPossibleSides() {
        return Arrays.asList(Direction.values());
    }

    protected Optional<Direction> getLookDirection() {
        return Optional.empty();
    }

    protected boolean getRequiresSupport() {
        return false;
    }

    protected boolean getRequiresExplicitShift() {
        return false;
    }

    protected Vec3 getHitModifier(Direction validSide) {
        return new Vec3(0, 0, 0);
    }

    private Optional<Direction> getValidSide(SchematicBlockState state) {
        boolean printInAir = Configs.PRINT_IN_AIR.getBooleanValue();

        List<Direction> sides = getPossibleSides();

        if (sides.isEmpty()) {
            PlacementDebug.log("placement validSide none reason=block-specific-condition-fail-no-sides guide={} target={} pos={}",
                    getClass().getSimpleName(), targetState, PlacementDebug.pos(state.blockPos));
            return Optional.empty();
        }

        if (printInAir && !getRequiresSupport()) {
            // When printInAir is enabled, we can place directly without support
            // But we should still respect the intended orientation for directional blocks
            // Check if we have specific sides defined by the subclass (like for logs)
            if (!sides.isEmpty()) {
                // Use the first available side from the specific sides (e.g., axis-specific for logs)
                return Optional.of(sides.get(0));
            } else {
                // Fallback to UP if no specific sides are defined
                return Optional.of(Direction.UP);
            }
        }

        List<Direction> validSides = new ArrayList<>();
        for (Direction side : sides) {
            SchematicBlockState neighborState = state.offset(side);

            if (getProperty(neighborState.currentState, SlabBlock.TYPE).orElse(null) == SlabType.DOUBLE) {
                validSides.add(side);
                continue;
            }

            if (canBeClicked(neighborState.world, neighborState.blockPos) && // Handle unclickable grass for example
                    !neighborState.currentState.canBeReplaced())
                validSides.add(side);
        }

        for (Direction validSide : validSides) {
            if (!isInteractive(state.offset(validSide).currentState.getBlock())) {
                PlacementDebug.log("placement validSide guide={} target={} pos={} side={} validSides={}",
                        getClass().getSimpleName(), targetState, PlacementDebug.pos(state.blockPos), validSide, validSides);
                return Optional.of(validSide);
            }
        }

        if (validSides.isEmpty()) {
            PlacementDebug.log("placement validSide none reason=no-support-block guide={} target={} pos={} printInAir={} requiresSupport={} possibleSides={}",
                    getClass().getSimpleName(), targetState, PlacementDebug.pos(state.blockPos), printInAir, getRequiresSupport(), sides);
        } else {
            PlacementDebug.log("placement validSide interactive guide={} target={} pos={} side={} validSides={}",
                    getClass().getSimpleName(), targetState, PlacementDebug.pos(state.blockPos), validSides.getFirst(), validSides);
        }
        return validSides.isEmpty() ? Optional.empty() : Optional.of(validSides.getFirst());
    }

    protected boolean getUseShift(SchematicBlockState state) {
        if (getRequiresExplicitShift())
            return true;

        Direction clickSide = getValidSide(state).orElse(null);
        if (clickSide == null)
            return false;
        return isInteractive(state.offset(clickSide).currentState.getBlock());
    }

    private Optional<Vec3> getHitVector(SchematicBlockState state) {
        boolean printInAir = Configs.PRINT_IN_AIR.getBooleanValue();

        if (printInAir && !getRequiresSupport()) {
            // For air placement, target the center of the target block position
            return Optional.of(Vec3.atCenterOf(state.blockPos));
        }

        return getValidSide(state).map(side -> Vec3.atCenterOf(state.blockPos)
                .add(Vec3.atLowerCornerOf(side.getUnitVec3i()).scale(0.5))
                .add(getHitModifier(side)));
    }

    @Nullable
    public PrinterPlacementContext getPlacementContext(LocalPlayer player) {
        try {
            Optional<Direction> validSide = getValidSide(state);
            Optional<Vec3> hitVec = getHitVector(state);
            Optional<ItemStack> requiredItem = getRequiredItem(player);
            int requiredSlot = getRequiredItemStackSlot(player);

            if (validSide.isEmpty() || hitVec.isEmpty() || requiredItem.isEmpty() || requiredSlot == -1) {
                PlacementDebug.log("placement context null guide={} target={} pos={} validSide={} hitVecPresent={} requiredItemPresent={} requiredSlot={}",
                        getClass().getSimpleName(), targetState, PlacementDebug.pos(state.blockPos), validSide.orElse(null),
                        hitVec.isPresent(), requiredItem.isPresent(), requiredSlot);
                return null;
            }

            Optional<Direction> lookDirection = getLookDirection();
            boolean requiresShift = getUseShift(state);

            boolean printInAir = Configs.PRINT_IN_AIR.getBooleanValue();
            BlockHitResult blockHitResult;

            if (printInAir && !getRequiresSupport()) {
                // For air placement, target the block position directly
                // Use a hit side that allows the block to maintain its intended orientation
                // The specific side depends on the block type and its intended orientation
                Direction hitSide = validSide.get().getOpposite(); // Use the opposite of the valid side to maintain orientation

                // For pillar blocks like logs, we need to be more specific about the hit side
                if (targetState.hasProperty(RotatedPillarBlock.AXIS)) {
                    // For pillar blocks, use a side perpendicular to the intended axis
                    Direction.Axis axis = targetState.getValue(RotatedPillarBlock.AXIS);
                    if (axis == Direction.Axis.Y) {
                        hitSide = Direction.DOWN; // vertical log - hit from above
                    } else if (axis == Direction.Axis.X) {
                        hitSide = Direction.WEST; // horizontal log along X - hit from West/East side
                    } else { // Z axis
                        hitSide = Direction.NORTH; // horizontal log along Z - hit from North/South side
                    }
                } else {
                    // For non-pillars, use DOWN as default to place normally
                    hitSide = Direction.DOWN;
                }

                blockHitResult = new BlockHitResult(hitVec.get(), hitSide, state.blockPos, false);
            } else {
                blockHitResult = new BlockHitResult(hitVec.get(), validSide.get().getOpposite(),
                        state.blockPos.relative(validSide.get()), false);
            }

            PrinterPlacementContext context = new PrinterPlacementContext(player, blockHitResult, requiredItem.get(), requiredSlot,
                    lookDirection.orElse(null), requiresShift);
            PlacementDebug.log("placement context guide={} target={} pos={} hitBlock={} side={} hit={} requiredItem={} requiredSlot={} lookDirection={} sneak={} printInAir={}",
                    getClass().getSimpleName(), targetState, PlacementDebug.pos(state.blockPos),
                    PlacementDebug.pos(blockHitResult.getBlockPos()), blockHitResult.getDirection(), blockHitResult.getLocation(),
                    PlacementDebug.stack(requiredItem.get()), requiredSlot, lookDirection.orElse(null), requiresShift, printInAir);
            return context;
        } catch (Exception e) {
            Printer.logger.error("getPlacementContext(): Exception caught: {}", e.getMessage());
            PlacementDebug.log("placement context exception guide={} target={} pos={} error={}",
                    getClass().getSimpleName(), targetState, PlacementDebug.pos(state.blockPos), e.toString());
            //e.printStackTrace();
            return null;
        }
    }
}
