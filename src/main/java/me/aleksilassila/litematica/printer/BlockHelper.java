package me.aleksilassila.litematica.printer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.common.collect.ImmutableMap;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;

abstract public class BlockHelper
{
	public static List<Class<?>> interactiveBlocks = new ArrayList<>(Arrays.asList(
			AbstractChestBlock.class, AbstractFurnaceBlock.class, CraftingTableBlock.class,
			LeverBlock.class,
			DoorBlock.class, TrapDoorBlock.class, BedBlock.class, RedstoneWireBlock.class,
			ScaffoldingBlock.class,
			HopperBlock.class, EnchantingTableBlock.class, NoteBlock.class, JukeboxBlock.class,
			CakeBlock.class,
			FenceGateBlock.class, BrewingStandBlock.class, DragonEggBlock.class, CommandBlock.class,
			BeaconBlock.class, AnvilBlock.class, ComparatorBlock.class, RepeaterBlock.class,
			DropperBlock.class, DispenserBlock.class, ShulkerBoxBlock.class, LecternBlock.class,
			FlowerPotBlock.class, BarrelBlock.class, BellBlock.class, SmithingTableBlock.class,
			LoomBlock.class, CartographyTableBlock.class, GrindstoneBlock.class,
			StonecutterBlock.class, SignBlock.class, AbstractCandleBlock.class));

	public static final Item[] SHOVEL_ITEMS = new Item[]{
			Items.NETHERITE_SHOVEL,
			Items.DIAMOND_SHOVEL,
			Items.GOLDEN_SHOVEL,
			Items.IRON_SHOVEL,
			Items.COPPER_SHOVEL,
			Items.STONE_SHOVEL,
			Items.WOODEN_SHOVEL
	};

	public static final Item[] AXE_ITEMS = new Item[]{
			Items.NETHERITE_AXE,
			Items.DIAMOND_AXE,
			Items.GOLDEN_AXE,
			Items.IRON_AXE,
			Items.COPPER_AXE,
			Items.STONE_AXE,
			Items.WOODEN_AXE
	};

	public static final Item[] HOE_ITEMS = new Item[]{
			Items.NETHERITE_HOE,
			Items.DIAMOND_HOE,
			Items.GOLDEN_HOE,
			Items.IRON_HOE,
			Items.COPPER_HOE,
			Items.STONE_HOE,
			Items.WOODEN_HOE
	};

	public static final ImmutableMap<Block, Block> STRIPPED_BLOCKS = buildStrippedBlocks();

	// See BlockTransformer.BlockTransformData axeStrippables()
	private static ImmutableMap<Block, Block> buildStrippedBlocks()
	{
		ImmutableMap.Builder<Block, Block> builder = new ImmutableMap.Builder<>();

		builder.put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD);
		builder.put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG);
		builder.put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD);
		builder.put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG);
		builder.put(Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD);
		builder.put(Blocks.PALE_OAK_LOG, Blocks.STRIPPED_PALE_OAK_LOG);
		builder.put(Blocks.POPLAR_WOOD, Blocks.STRIPPED_POPLAR_WOOD);
		builder.put(Blocks.POPLAR_LOG, Blocks.STRIPPED_POPLAR_LOG);
		builder.put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD);
		builder.put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG);
		builder.put(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD);
		builder.put(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG);
		builder.put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD);
		builder.put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG);
		builder.put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD);
		builder.put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG);
		builder.put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD);
		builder.put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG);
		builder.put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM);
		builder.put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE);
		builder.put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM);
		builder.put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE);
		builder.put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD);
		builder.put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG);
		builder.put(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK);

		return builder.build();
	}

	public static final Block[] TILLABLE_BLOCKS = new Block[]{
			Blocks.DIRT,
			Blocks.GRASS_BLOCK,
			Blocks.COARSE_DIRT,
			Blocks.ROOTED_DIRT,
			Blocks.DIRT_PATH,
			};
}
