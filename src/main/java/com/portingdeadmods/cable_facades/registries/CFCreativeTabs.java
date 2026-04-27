package com.portingdeadmods.cable_facades.registries;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CFCreativeTabs {

    public static final CreativeModeTab CF_TAB = new CreativeModeTab("cable_facades") {
        @Override
        public ItemStack makeIcon() {
            return CFItems.WRENCH.get().getDefaultInstance();
        }
    };

    private CFCreativeTabs() {}
}
