package me.aleksilassila.litematica.printer.guides.placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import me.aleksilassila.litematica.printer.BlockHelper;
import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.config.Configs;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;

public class LogGuide extends GeneralPlacementGuide
{
	public LogGuide(SchematicBlockState state)
	{
		super(state);
	}

	@Override
	protected List<Direction> getPossibleSides()
	{
		if (targetState.hasProperty(RotatedPillarBlock.AXIS))
		{
			Direction.Axis axis = targetState.getValue(RotatedPillarBlock.AXIS);
			return Arrays.stream(Direction.values()).filter(d -> d.getAxis() == axis).toList();
		}

		return new ArrayList<>();
	}

	@Override
	protected @Nonnull List<ItemStack> getRequiredItems()
	{
		for (Block log : BlockHelper.STRIPPED_BLOCKS.keySet())
		{
			if (targetState.getBlock() == BlockHelper.STRIPPED_BLOCKS.get(log))
			{
				return Collections.singletonList(new ItemStack(log));
			}
		}

		return super.getRequiredItems();
	}

	@Override
	public boolean canExecute(LocalPlayer player)
	{
        if (!Configs.STRIP_LOGS.getBooleanValue())
        {
            return false;
        }

		if (BlockHelper.STRIPPED_BLOCKS.containsValue(targetState.getBlock()))
		{
			return super.canExecute(player);
		}

		return false;
	}
}
