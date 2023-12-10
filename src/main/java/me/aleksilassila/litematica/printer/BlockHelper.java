package me.aleksilassila.litematica.printer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.CakeBlock;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

abstract public class BlockHelper {
        public static List<Class<?>> interactiveBlocks = new ArrayList<>(Arrays.asList(
                        AbstractChestBlock.class, AbstractFurnaceBlock.class, CraftingTableBlock.class,
                        LeverBlock.class,
                        DoorBlock.class, TrapdoorBlock.class, BedBlock.class, RedstoneWireBlock.class,
                        ScaffoldingBlock.class,
                        HopperBlock.class, EnchantingTableBlock.class, NoteBlock.class, JukeboxBlock.class,
                        CakeBlock.class,
                        FenceGateBlock.class, BrewingStandBlock.class, DragonEggBlock.class, CommandBlock.class,
                        BeaconBlock.class, AnvilBlock.class, ComparatorBlock.class, RepeaterBlock.class,
                        DropperBlock.class, DispenserBlock.class, ShulkerBoxBlock.class, LecternBlock.class,
                        FlowerPotBlock.class, BarrelBlock.class, BellBlock.class, SmithingTableBlock.class,
                        LoomBlock.class, CartographyTableBlock.class, GrindstoneBlock.class,
                        StonecutterBlock.class, AbstractSignBlock.class, AbstractCandleBlock.class));

        public static final Item[] SHOVEL_ITEMS = new Item[] {
                        Items.NETHERITE_SHOVEL,
                        Items.DIAMOND_SHOVEL,
                        Items.GOLDEN_SHOVEL,
                        Items.IRON_SHOVEL,
                        Items.STONE_SHOVEL,
                        Items.WOODEN_SHOVEL
        };
}
