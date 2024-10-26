package me.aleksilassila.litematica.printer.mixin;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.AxeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * This class apparently fixes an issue with Quilt.
 */
@Mixin(AxeItem.class)
public interface AxeItemAccessor
{
    @Accessor("STRIPPED_BLOCKS")
    static Map<Block, Block> getStrippedBlocks()
    {
        throw new AssertionError("Untransformed @Accessor");
    }
}
