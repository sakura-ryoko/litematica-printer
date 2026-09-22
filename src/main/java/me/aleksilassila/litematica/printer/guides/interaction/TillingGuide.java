package me.aleksilassila.litematica.printer.guides.interaction;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import me.aleksilassila.litematica.printer.BlockHelper;
import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.guides.placement.FarmlandGuide;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public class TillingGuide extends InteractionGuide
{
	public TillingGuide(SchematicBlockState state)
	{
		super(state);
	}

	@Override
	public boolean canExecute(LocalPlayer player)
	{
        if (!super.canExecute(player))
        {
            return false;
        }

		return Arrays.stream(FarmlandGuide.TILLABLE_BLOCKS).anyMatch(b -> b == currentState.getBlock());
	}

	@Override
	protected @Nonnull List<ItemStack> getRequiredItems()
	{
		return Arrays.stream(BlockHelper.HOE_ITEMS).map(ItemStack::new).toList();
	}
}
