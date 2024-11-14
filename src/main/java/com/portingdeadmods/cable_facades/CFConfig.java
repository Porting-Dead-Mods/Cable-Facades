package com.portingdeadmods.cable_facades;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = CFMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CFConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_STRINGS = BUILDER.comment("List of blocks that are allowed to be covered. Supports '*' as a wildcard.")
            .defineListAllowEmpty("blocks", List.of("pipez:*_pipe","mekanism:*_cable","mekanism:*_conductor","mekanism:*_pipe","mekanism:*_tube","mekanism:*_transporter","thermal:*_duct","integrateddynamics:cable","computercraft:cable","powah:energy_cable_*"), CFConfig::validateBlockName);

    private static final ForgeConfigSpec.BooleanValue CONSUME_FACADE = BUILDER.comment("Whether the facade should be consumed when placed.")
            .define("consumeFacade", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();
    public static Set<Block> exactBlocks;
    public static List<Pattern> blockPatterns;
    public static boolean consumeFacade;

    private static boolean validateBlockName(final Object obj) {
        if (obj instanceof String blockName) {
            if (blockName.contains("*")) {
                return true;
            }
            return ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(blockName));
        }
        return false;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        consumeFacade = CONSUME_FACADE.get();

        exactBlocks = new HashSet<>();
        blockPatterns = new ArrayList<>();

        for (String blockName : BLOCK_STRINGS.get()) {
            if (blockName.contains("*")) {
                String regex = blockName.replace("*", ".*");
                blockPatterns.add(Pattern.compile(regex));
            } else {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
                if (block != null) {
                    exactBlocks.add(block);
                }
            }
        }
    }

    public static boolean isBlockAllowed(Block targetBlock) {
        if (exactBlocks.contains(targetBlock)) {
            return true;
        }

        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(targetBlock);
        if (blockId != null) {
            String blockIdString = blockId.toString();
            for (Pattern pattern : blockPatterns) {
                if (pattern.matcher(blockIdString).matches()) {
                    return true;
                }
            }
        }

        return false;
    }
}

