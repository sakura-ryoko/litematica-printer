package me.aleksilassila.litematica.printer.guides.interaction;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import me.aleksilassila.litematica.printer.BlockHelper;
import me.aleksilassila.litematica.printer.SchematicBlockState;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;

public class CampfireExtinguishGuide extends InteractionGuide
{
	boolean shouldBeLit;
	boolean isLit;

	public CampfireExtinguishGuide(SchematicBlockState state)
	{
		super(state);

		shouldBeLit = getProperty(targetState, CampfireBlock.LIT).orElse(false);
		isLit = getProperty(currentState, CampfireBlock.LIT).orElse(false);
	}

	@Override
	public boolean canExecute(LocalPlayer player)
	{
		if (!super.canExecute(player))
		{
			return false;
		}

		return (currentState.getBlock() instanceof CampfireBlock) && !shouldBeLit && isLit;
	}

	@Override
	protected @Nonnull List<ItemStack> getRequiredItems()
	{
		return Arrays.stream(BlockHelper.SHOVEL_ITEMS).map(ItemStack::new).toList();
	}
}
