package me.aleksilassila.litematica.printer;

import net.minecraft.MinecraftVersion;

public class PrinterReference
{
    public static final String MOD_ID = "litematica-printer";
    public static final String MOD_NAME = "Litematica Printer";
    public static final String MOD_VERSION = Printer.getModVersionString(MOD_ID);
    public static final String MC_VERSION = MinecraftVersion.CURRENT.getName();
    public static final String MOD_TYPE = "fabric";
    public static final String MOD_STRING = MOD_ID+"-"+MOD_TYPE+"-"+MC_VERSION+"-"+MOD_VERSION;
}
