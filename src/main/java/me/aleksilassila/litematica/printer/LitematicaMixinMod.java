package me.aleksilassila.litematica.printer;

import me.aleksilassila.litematica.printer.event.KeyCallbacks;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;

public class LitematicaMixinMod implements ModInitializer {
    public static Printer printer;

    @Override
    public void onInitialize() {
        KeyCallbacks.init(Minecraft.getInstance());
        Printer.LOGGER.info("{} initialized.", PrinterReference.MOD_STRING);
    }
}
