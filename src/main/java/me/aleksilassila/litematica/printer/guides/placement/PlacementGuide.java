package me.aleksilassila.litematica.printer.guides.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.aleksilassila.litematica.printer.Printer;
import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.actions.Action;
import me.aleksilassila.litematica.printer.actions.PrepareAction;
import me.aleksilassila.litematica.printer.actions.ReleaseShiftAction;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.guides.Guide;
import me.aleksilassila.litematica.printer.implementation.PrinterPlacementContext;
import me.aleksilassila.litematica.printer.implementation.actions.InteractActionImpl;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import fi.dy.masa.litematica.util.ItemUtils;

/**
 * Guide that clicks its neighbors to create a placement in target position.
 */
abstract public class PlacementGuide extends Guide
{
	public PlacementGuide(SchematicBlockState state)
	{
		super(state);
	}

	protected ItemStack getBlockItem(BlockState state)
	{
		// Let's use the Litematica Pick Block Cache for this.
		return ItemUtils.getItemForBlock(this.state.world, this.state.blockPos, state, true);
	}

	protected Optional<Block> getRequiredItemAsBlock(LocalPlayer player)
	{
		Optional<ItemStack> requiredItem = getRequiredItem(player);

		if (requiredItem.isEmpty())
		{
			return Optional.empty();
		}
		else
		{
			ItemStack itemStack = requiredItem.get();

            if (itemStack.getItem() instanceof BlockItem)
            {
                return Optional.of(((BlockItem) itemStack.getItem()).getBlock());
            }
            else
            {
                return Optional.empty();
            }
		}
	}

	@Override
	protected @Nonnull List<ItemStack> getRequiredItems()
	{
		Printer.printDebug("PlacementGuide#getRequiredItems() - target state [{}]", state.targetState.toString());
		return Collections.singletonList(getBlockItem(state.targetState));
	}

	abstract protected boolean getUseShift(SchematicBlockState state);

	@Nullable
	abstract public PrinterPlacementContext getPlacementContext(LocalPlayer player);

	@Override
	public boolean canExecute(LocalPlayer player)
	{
        if (!super.canExecute(player))
        {
            return false;
        }

		List<ItemStack> requiredItems = getRequiredItems();
        if (requiredItems.isEmpty() || requiredItems.stream().allMatch(i -> i.is(Items.AIR)))
        {
            return false;
        }

		BlockPlaceContext ctx = getPlacementContext(player);
        if (ctx == null || !ctx.canPlace())
        {
            return false;
        }
//        if (!state.currentState.getMaterial().isReplaceable()) return false;
        if (!Configs.REPLACE_FLUIDS_SOURCE_BLOCKS.getBooleanValue()
                && getProperty(state.currentState, LiquidBlock.LEVEL).orElse(1) == 0)
        {
            return false;
        }

		BlockState resultState = getRequiredItemAsBlock(player)
				.orElse(targetState.getBlock())
				.getStateForPlacement(ctx);

		if (resultState != null)
		{
            if (!resultState.canSurvive(state.world, state.blockPos))
            {
                return false;
            }
			return !(currentState.getBlock() instanceof LiquidBlock) || canPlaceInWater(resultState);
		}
		else
		{
			return false;
		}
	}

	@Override
	public @Nonnull List<Action> execute(LocalPlayer player)
	{
		List<Action> actions = new ArrayList<>();
		PrinterPlacementContext ctx = getPlacementContext(player);

        if (ctx == null)
        {
            return actions;
        }
		actions.add(new PrepareAction(ctx));
		actions.add(new InteractActionImpl(ctx));
        if (ctx.shouldSneak)
        {
            actions.add(new ReleaseShiftAction());
        }

		return actions;
	}

	protected static boolean canBeClicked(Level world, BlockPos pos)
	{
		return getOutlineShape(world, pos) != Shapes.empty()
				&& !(world.getBlockState(pos).getBlock() instanceof SignBlock); // FIXME signs
	}

	private static VoxelShape getOutlineShape(Level world, BlockPos pos)
	{
		return world.getBlockState(pos).getShape(world, pos);
	}

	public boolean isInteractive(Block block)
	{
		for (Class<?> clazz : interactiveBlocks)
		{
			if (clazz.isInstance(block))
			{
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean canPlaceInWater(BlockState blockState)
	{
		Block block = blockState.getBlock();

		if (block instanceof LiquidBlockContainer)
		{
			return true;
		}
		else if (!(block instanceof DoorBlock) && !(blockState.getBlock() instanceof SignBlock)
				&& !blockState.is(Blocks.LADDER) && !blockState.is(Blocks.SUGAR_CANE)
				&& !blockState.is(Blocks.BUBBLE_COLUMN))
		{
//			Material material = blockState.getMaterial();
//			if (material != Material && material != Material.STRUCTURE_VOID && material != Material.UNDERWATER_PLANT && material != Material.REPLACEABLE_UNDERWATER_PLANT)
//			{
//				return material.blocksMovement();
//			}
//			else
//			{
//				return true;
//			}

            return block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING && blockState.isSolid();
		}

		return true;
	}
}
