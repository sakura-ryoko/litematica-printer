package me.aleksilassila.litematica.printer.guides.placement;

import me.aleksilassila.litematica.printer.Printer;
import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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

    protected Vec3d getHitModifier(Direction validSide) {
        return new Vec3d(0, 0, 0);
    }

    private Optional<Direction> getValidSide(SchematicBlockState state) {
        boolean printInAir = Configs.PRINT_IN_AIR.getBooleanValue();

        List<Direction> sides = getPossibleSides();

        if (sides.isEmpty()) {
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
                    !neighborState.currentState.isReplaceable())
                validSides.add(side);
        }

        for (Direction validSide : validSides) {
            if (!isInteractive(state.offset(validSide).currentState.getBlock())) {
                return Optional.of(validSide);
            }
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

    private Optional<Vec3d> getHitVector(SchematicBlockState state) {
        boolean printInAir = Configs.PRINT_IN_AIR.getBooleanValue();

        if (printInAir && !getRequiresSupport()) {
            // For air placement, target the center of the target block position
            return Optional.of(Vec3d.ofCenter(state.blockPos));
        }

        return getValidSide(state).map(side -> Vec3d.ofCenter(state.blockPos)
                .add(Vec3d.of(side.getVector()).multiply(0.5))
                .add(getHitModifier(side)));
    }

    @Nullable
    public PrinterPlacementContext getPlacementContext(ClientPlayerEntity player) {
        try {
            Optional<Direction> validSide = getValidSide(state);
            Optional<Vec3d> hitVec = getHitVector(state);
            Optional<ItemStack> requiredItem = getRequiredItem(player);
            int requiredSlot = getRequiredItemStackSlot(player);

            if (validSide.isEmpty() || hitVec.isEmpty() || requiredItem.isEmpty() || requiredSlot == -1)
                return null;

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
                if (targetState.contains(net.minecraft.block.PillarBlock.AXIS)) {
                    // For pillar blocks, use a side perpendicular to the intended axis
                    Direction.Axis axis = targetState.get(net.minecraft.block.PillarBlock.AXIS);
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
                        state.blockPos.offset(validSide.get()), false);
            }

            return new PrinterPlacementContext(player, blockHitResult, requiredItem.get(), requiredSlot,
                    lookDirection.orElse(null), requiresShift);
        } catch (Exception e) {
            Printer.logger.error("getPlacementContext(): Exception caught: {}", e.getMessage());
            //e.printStackTrace();
            return null;
        }
    }
}
