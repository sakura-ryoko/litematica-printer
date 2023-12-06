package me.aleksilassila.litematica.printer.v1_20_2.guides;

import java.util.ArrayList;

import me.aleksilassila.litematica.printer.v1_20_2.SchematicBlockState;
import me.aleksilassila.litematica.printer.v1_20_2.guides.interaction.CampfireExtinguishGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.interaction.CycleStateGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.interaction.EnderEyeGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.interaction.FlowerPotFillGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.interaction.LightCandleGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.interaction.LogStrippingGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.interaction.TillingGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.BlockIndifferentGuesserGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.BlockReplacementGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.ChestGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.FallingBlockGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.FarmlandGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.FlowerPotGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.GuesserGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.LogGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.PropertySpecificGuesserGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.RailGuesserGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.RotatingBlockGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.SlabGuide;
import me.aleksilassila.litematica.printer.v1_20_2.guides.placement.TorchGuide;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BigDripleafBlock;
import net.minecraft.block.BigDripleafStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.TripwireBlock;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.block.TwistingVinesPlantBlock;
import net.minecraft.util.Pair;

public class Guides {
    protected final static ArrayList<Pair<Class<? extends Guide>, Class<? extends Block>[]>> guides = new ArrayList<>();

    @SafeVarargs
    protected static void registerGuide(Class<? extends Guide> guideClass, Class<? extends Block>... blocks) {
        guides.add(new Pair<>(guideClass, blocks));
    }

    static {
        // registerGuide(SkipGuide.class, AbstractSignBlock.class, SkullBlock.class,
        // BannerBlock.class);

        registerGuide(RotatingBlockGuide.class, AbstractSkullBlock.class, AbstractSignBlock.class,
                AbstractBannerBlock.class);
        registerGuide(SlabGuide.class, SlabBlock.class);
        registerGuide(TorchGuide.class, TorchBlock.class);
        registerGuide(FarmlandGuide.class, FarmlandBlock.class);
        registerGuide(TillingGuide.class, FarmlandBlock.class);
        registerGuide(RailGuesserGuide.class, AbstractRailBlock.class);
        registerGuide(ChestGuide.class, ChestBlock.class);
        registerGuide(FlowerPotGuide.class, FlowerPotBlock.class);
        registerGuide(FlowerPotFillGuide.class, FlowerPotBlock.class);

        registerGuide(PropertySpecificGuesserGuide.class,
                RepeaterBlock.class, ComparatorBlock.class, RedstoneWireBlock.class, RedstoneTorchBlock.class,
                BambooBlock.class, CactusBlock.class, SaplingBlock.class, ScaffoldingBlock.class,
                PointedDripstoneBlock.class,
                HorizontalConnectingBlock.class, DoorBlock.class, TrapdoorBlock.class, FenceGateBlock.class,
                ChestBlock.class,
                SnowBlock.class, SeaPickleBlock.class, CandleBlock.class, LeverBlock.class, EndPortalFrameBlock.class,
                NoteBlock.class, CampfireBlock.class, PoweredRailBlock.class, LeavesBlock.class,
                TripwireHookBlock.class);
        registerGuide(FallingBlockGuide.class, FallingBlock.class);
        registerGuide(BlockIndifferentGuesserGuide.class, BambooBlock.class, BigDripleafStemBlock.class,
                BigDripleafBlock.class,
                TwistingVinesPlantBlock.class, TripwireBlock.class);

        registerGuide(CampfireExtinguishGuide.class, CampfireBlock.class);
        registerGuide(LightCandleGuide.class, AbstractCandleBlock.class);
        registerGuide(EnderEyeGuide.class, EndPortalFrameBlock.class);
        registerGuide(CycleStateGuide.class,
                DoorBlock.class, FenceGateBlock.class, TrapdoorBlock.class,
                LeverBlock.class,
                RepeaterBlock.class, ComparatorBlock.class, NoteBlock.class);
        registerGuide(BlockReplacementGuide.class, SnowBlock.class, SeaPickleBlock.class, CandleBlock.class,
                SlabBlock.class);
        registerGuide(LogGuide.class);
        registerGuide(LogStrippingGuide.class);
        registerGuide(GuesserGuide.class);
    }

    public ArrayList<Pair<Class<? extends Guide>, Class<? extends Block>[]>> getGuides() {
        return guides;
    }

    public Guide[] getInteractionGuides(SchematicBlockState state) {
        ArrayList<Pair<Class<? extends Guide>, Class<? extends Block>[]>> guides = getGuides();

        ArrayList<Guide> applicableGuides = new ArrayList<>();
        for (Pair<Class<? extends Guide>, Class<? extends Block>[]> guidePair : guides) {
            try {
                if (guidePair.getRight().length == 0) {
                    applicableGuides
                            .add(guidePair.getLeft().getConstructor(SchematicBlockState.class).newInstance(state));
                    continue;
                }

                for (Class<? extends Block> clazz : guidePair.getRight()) {
                    if (clazz.isInstance(state.targetState.getBlock())) {
                        applicableGuides
                                .add(guidePair.getLeft().getConstructor(SchematicBlockState.class).newInstance(state));
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return applicableGuides.toArray(Guide[]::new);
    }
}
