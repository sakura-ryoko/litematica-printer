package me.aleksilassila.litematica.printer.implementation;

import java.util.Arrays;

import me.aleksilassila.litematica.printer.BlockHelper;
import net.minecraft.block.ButtonBlock;

public class BlockHelperImpl extends BlockHelper {
    static {
        interactiveBlocks.addAll(Arrays.asList(
                ButtonBlock.class));
    }
}
