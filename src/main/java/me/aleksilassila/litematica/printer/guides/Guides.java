package me.aleksilassila.litematica.printer.guides;

import java.util.ArrayList;
import me.aleksilassila.litematica.printer.SchematicBlockState;
import me.aleksilassila.litematica.printer.guides.interaction.*;
import me.aleksilassila.litematica.printer.guides.placement.*;

import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.*;

public class Guides {
    protected final static ArrayList<Tuple<Class<? extends Guide>, Class<? extends Block>[]>> guides = new ArrayList<>();

    @SafeVarargs
    protected static void registerGuide(Class<? extends Guide> guideClass, Class<? extends Block>... blocks) {
        guides.add(new Tuple<>(guideClass, blocks));
    }

    static {
        // registerGuide(SkipGuide.class, AbstractSignBlock.class, SkullBlock.class, BannerBlock.class);

        registerGuide(RotatingBlockGuide.class, AbstractSkullBlock.class, SignBlock.class, AbstractBannerBlock.class);
        registerGuide(SlabGuide.class, SlabBlock.class);
        registerGuide(TorchGuide.class, TorchBlock.class);
        registerGuide(FarmlandGuide.class, FarmlandBlock.class);
        registerGuide(TillingGuide.class, FarmlandBlock.class);
        registerGuide(RailGuesserGuide.class, BaseRailBlock.class);
        registerGuide(ChestGuide.class, ChestBlock.class);
        registerGuide(FlowerPotGuide.class, FlowerPotBlock.class);
        registerGuide(FlowerPotFillGuide.class, FlowerPotBlock.class);

        registerGuide(PropertySpecificGuesserGuide.class,
                RepeaterBlock.class, ComparatorBlock.class, RedStoneWireBlock.class, RedstoneTorchBlock.class,
                BambooStalkBlock.class, CactusBlock.class, SaplingBlock.class, ScaffoldingBlock.class,
                PointedDripstoneBlock.class,
                CrossCollisionBlock.class, DoorBlock.class, TrapDoorBlock.class, FenceGateBlock.class,
                ChestBlock.class,
                SnowLayerBlock.class, SeaPickleBlock.class, CandleBlock.class, LeverBlock.class, EndPortalFrameBlock.class,
                NoteBlock.class, CampfireBlock.class, PoweredRailBlock.class, LeavesBlock.class,
                TripWireHookBlock.class);
        registerGuide(FallingBlockGuide.class, FallingBlock.class);
        registerGuide(BlockIndifferentGuesserGuide.class, BambooStalkBlock.class, BigDripleafStemBlock.class,
                BigDripleafBlock.class,
                TwistingVinesPlantBlock.class, TripWireBlock.class);

        registerGuide(CampfireExtinguishGuide.class, CampfireBlock.class);
        registerGuide(LightCandleGuide.class, AbstractCandleBlock.class);
        registerGuide(EnderEyeGuide.class, EndPortalFrameBlock.class);
        registerGuide(CycleStateGuide.class,
                DoorBlock.class, FenceGateBlock.class, TrapDoorBlock.class,
                LeverBlock.class,
                RepeaterBlock.class, ComparatorBlock.class, NoteBlock.class);
        registerGuide(BlockReplacementGuide.class, SnowLayerBlock.class, SeaPickleBlock.class, CandleBlock.class,
                SlabBlock.class);
        registerGuide(LogGuide.class);
        registerGuide(LogStrippingGuide.class);
        registerGuide(GuesserGuide.class);
    }

    public ArrayList<Tuple<Class<? extends Guide>, Class<? extends Block>[]>> getGuides() {
        return guides;
    }

    public Guide[] getInteractionGuides(SchematicBlockState state) {
        ArrayList<Tuple<Class<? extends Guide>, Class<? extends Block>[]>> guides = getGuides();

        ArrayList<Guide> applicableGuides = new ArrayList<>();
        for (Tuple<Class<? extends Guide>, Class<? extends Block>[]> guidePair : guides) {
            try {
                if (guidePair.getB().length == 0) {
                    applicableGuides
                            .add(guidePair.getA().getConstructor(SchematicBlockState.class).newInstance(state));
                    continue;
                }

                for (Class<? extends Block> clazz : guidePair.getB()) {
                    if (clazz.isInstance(state.targetState.getBlock())) {
                        applicableGuides
                                .add(guidePair.getA().getConstructor(SchematicBlockState.class).newInstance(state));
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return applicableGuides.toArray(Guide[]::new);
    }
}
