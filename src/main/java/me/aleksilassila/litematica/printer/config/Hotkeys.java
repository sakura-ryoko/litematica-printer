package me.aleksilassila.litematica.printer.config;

import java.util.List;
import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;

public class Hotkeys
{
    // Hotkeys
    public static final ConfigHotkey PRINT = new ConfigHotkey("print", "V", KeybindSettings.PRESS_ALLOWEXTRA_EMPTY,
            "litematica-printer.config.hotkeys.comment.print",
            "litematica-printer.config.hotkeys.prettyName.print")
            .translatedName("litematica-printer.config.hotkeys.name.print");
    public static final ConfigHotkey TOGGLE_PRINTING_MODE = new ConfigHotkey("togglePrintingMode", "CAPS_LOCK",
            KeybindSettings.PRESS_ALLOWEXTRA_EMPTY,
            "litematica-printer.config.hotkeys.comment.togglePrintingMode",
            "litematica-printer.config.hotkeys.prettyName.togglePrintingMode")
            .translatedName("litematica-printer.config.hotkeys.name.togglePrintingMode");

    public static List<ConfigHotkey> getHotkeyList()
    {
        List<ConfigHotkey> list = new java.util.ArrayList<>(fi.dy.masa.litematica.config.Hotkeys.HOTKEY_LIST);
        list.add(PRINT);
        list.add(TOGGLE_PRINTING_MODE);

        return ImmutableList.copyOf(list);
    }
}
