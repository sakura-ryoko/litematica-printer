package me.aleksilassila.litematica.printer.config;

import java.util.List;
import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;

public class Configs
{
    // Configs settings
    public static final ConfigInteger PRINTING_INTERVAL = new ConfigInteger("printingInterval", 12, 1, 40,
            "litematica-printer.config.generic.comment.printingInterval")
            .translatedName("litematica-printer.config.generic.name.printingInterval");
    public static final ConfigDouble PRINTING_RANGE = new ConfigDouble("printingRange", 5, 2.5, 5,
            "litematica-printer.config.generic.comment.printingRange")
            .translatedName("litematica-printer.config.generic.name.printingRange");
    public static final ConfigBoolean PRINT_MODE = new ConfigBoolean("printingMode", false,
            "litematica-printer.config.generic.comment.printingMode",
            "litematica-printer.config.generic.prettyName.printingMode")
            .translatedName("litematica-printer.config.generic.name.printingMode");
    public static final ConfigBoolean PRINT_DEBUG = new ConfigBoolean("printingDebug", false,
            "litematica-printer.config.generic.comment.printingDebug")
            .translatedName("litematica-printer.config.generic.name.printingDebug");
    public static final ConfigBoolean REPLACE_FLUIDS_SOURCE_BLOCKS = new ConfigBoolean("replaceFluidSourceBlocks",
            true,
            "litematica-printer.config.generic.comment.replaceFluidSourceBlocks")
            .translatedName("litematica-printer.config.generic.name.replaceFluidSourceBlocks");
    public static final ConfigBoolean STRIP_LOGS = new ConfigBoolean("stripLogs", true,
            "litematica-printer.config.generic.comment.stripLogs")
            .translatedName("litematica-printer.config.generic.name.stripLogs");
    // Add INTERACT_BLOCKS pull by DarkReaper231
    public static final ConfigBoolean INTERACT_BLOCKS = new ConfigBoolean("interactBlocks", true,
            "litematica-printer.config.generic.comment.interactBlocks")
            .translatedName("litematica-printer.config.generic.name.interactBlocks");

    public static ImmutableList<IConfigBase> getConfigList()
    {
        List<IConfigBase> list = new java.util.ArrayList<>(fi.dy.masa.litematica.config.Configs.Generic.OPTIONS);
        list.add(PRINT_MODE);
        list.add(PRINT_DEBUG);
        list.add(PRINTING_INTERVAL);
        list.add(PRINTING_RANGE);
        list.add(REPLACE_FLUIDS_SOURCE_BLOCKS);
        list.add(STRIP_LOGS);
        list.add(INTERACT_BLOCKS);

        return ImmutableList.copyOf(list);
    }
}
