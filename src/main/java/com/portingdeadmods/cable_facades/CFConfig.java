package com.portingdeadmods.cable_facades;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = CFMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CFConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_STRINGS = BUILDER.comment("A list of blocks that are allowed to be framed with a facade.").defineListAllowEmpty("blocks", List.of("pipez:item_pipe"), CFConfig::validateBlockName);

    static final ForgeConfigSpec SPEC = BUILDER.build();
    public static Set<Block> blocks;

    private static boolean validateBlockName(final Object obj) {
        return obj instanceof final String blockName && ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(blockName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        blocks = BLOCK_STRINGS.get().stream().map(blockName -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName))).collect(Collectors.toSet());
    }
}
