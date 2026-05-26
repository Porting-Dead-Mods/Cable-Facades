package com.portingdeadmods.cable_facades.registries;

import com.portingdeadmods.cable_facades.CFMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CFItemTags {
    public static final TagKey<Item> WRENCHES = cTag("wrenches");

    public static final TagKey<Block> SUPPORTS_FACADE = facadeTag("supports_facade");

    private static TagKey<Item> cTag(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
    }

    private static TagKey<Block> facadeTag(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(CFMain.MODID, name));
    }
}
