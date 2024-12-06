package com.portingdeadmods.cable_facades.registries;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CFCreativeTabs {

    public static final CreativeModeTab CF_TAB = (new CreativeModeTab("cable_facades")
    {
        @Override
        public ItemStack makeIcon () {
            return new ItemStack(CFItems.WRENCH.get());
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("Cable Facades");
        }
    });

}
