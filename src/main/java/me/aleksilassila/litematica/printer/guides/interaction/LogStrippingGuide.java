package me.aleksilassila.litematica.printer.guides.interaction;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import me.aleksilassila.litematica.printer.BlockHelper;
import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.config.Configs;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class LogStrippingGuide extends InteractionGuide
{
	public LogStrippingGuide(SchematicBlockState state)
	{
		super(state);
	}

	@Override
	public boolean canExecute(LocalPlayer player)
	{
		if (!Configs.STRIP_LOGS.getBooleanValue())
		{
			return false;
		}

		if (!super.canExecute(player))
		{
			return false;
		}

		Block strippingResult = BlockHelper.STRIPPED_BLOCKS.get(currentState.getBlock());
		return strippingResult == targetState.getBlock();
	}

	@Override
	protected @Nonnull List<ItemStack> getRequiredItems()
	{
		return Arrays.stream(BlockHelper.AXE_ITEMS).map(ItemStack::new).toList();
	}
}
