package me.aleksilassila.litematica.printer;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.config.Hotkeys;
import net.fabricmc.api.ModInitializer;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBooleanConfigWithMessage;

public class LitematicaMixinMod implements ModInitializer
{
        public static Printer printer;

        @Override
        public void onInitialize()
        {
                Hotkeys.TOGGLE_PRINTING_MODE.getKeybind()
                                .setCallback(new KeyCallbackToggleBooleanConfigWithMessage(Configs.PRINT_MODE));

                Printer.logger.info("{} initialized.", PrinterReference.MOD_STRING);
        }
}
