package me.aleksilassila.litematica.printer.v1_19.implementation;

import me.aleksilassila.litematica.printer.v1_19.BlockHelper;
import net.minecraft.block.AbstractButtonBlock;

import java.util.Arrays;

public class BlockHelperImpl extends BlockHelper {
    static {
        interactiveBlocks.addAll(Arrays.asList(
                AbstractButtonBlock.class
        ));
    }
}