package com.portingdeadmods.cable_facades.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CFItemTags {
    public static final TagKey<Item> WRENCHES = forgeTag("wrenches");

    private static TagKey<Item> forgeTag(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("forge", name));
    }
}
