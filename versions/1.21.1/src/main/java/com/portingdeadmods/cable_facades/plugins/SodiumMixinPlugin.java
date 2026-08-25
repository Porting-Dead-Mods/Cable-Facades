package com.portingdeadmods.cable_facades.plugins;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class SodiumMixinPlugin implements IMixinConfigPlugin {
    private static final boolean SODIUM_0_8 = isSodium08Loaded();

    private static boolean isSodium08Loaded() {
        IModFileInfo modFile = LoadingModList.get().getModFileById("sodium");
        if (modFile == null) {
            return false;
        }
        for (IModInfo mod : modFile.getMods()) {
            if (mod.getModId().equals("sodium")) {
                return isAtLeast08(mod.getVersion().toString());
            }
        }
        return false;
    }

    private static boolean isAtLeast08(String version) {
        String[] parts = version.split("[-+]", 2)[0].split("\\.");
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return major > 0 || minor >= 8;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return SODIUM_0_8;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
