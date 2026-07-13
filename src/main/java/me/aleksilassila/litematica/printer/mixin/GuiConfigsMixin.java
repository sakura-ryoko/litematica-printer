package me.aleksilassila.litematica.printer.mixin;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.gui.GuiConfigs;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.config.Hotkeys;
import org.objectweb.asm.Opcodes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = GuiConfigs.class, remap = false)
public class GuiConfigsMixin {
    @Redirect(method = "getConfigs", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Configs$Generic;OPTIONS:Lcom/google/common/collect/ImmutableList;", opcode = Opcodes.GETSTATIC))
    private ImmutableList<IConfigBase> moreOptions() {
        return Configs.getConfigList();
    }

    @Redirect(method = "getAllConfigs", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Configs$Generic;OPTIONS:Lcom/google/common/collect/ImmutableList;", opcode = Opcodes.GETSTATIC))
    private ImmutableList<IConfigBase> moreOptionsAll() {
        return Configs.getConfigList();
    }

    @Redirect(method = "getConfigs", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Hotkeys;HOTKEY_LIST:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    private List<ConfigHotkey> moreHotkeys() {
        return Hotkeys.getHotkeyList();
    }

    @Redirect(method = "getAllConfigs", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Hotkeys;HOTKEY_LIST:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    private List<ConfigHotkey> moreHotkeysAll() {
        return Hotkeys.getHotkeyList();
    }
}
