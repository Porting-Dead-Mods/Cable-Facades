package com.portingdeadmods.cable_facades;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@EventBusSubscriber(modid = CFMain.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CFConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_STRINGS = BUILDER.comment("List of blocks that are allowed to be covered. Supports '*' as a wildcard.")
            .defineListAllowEmpty("blocks", List.of("pipez:*_pipe", "mekanism:*_cable", "mekanism:*_conductor", "mekanism:*_pipe", "mekanism:*_tube", "mekanism:*_transporter", "mekanism_extras:*_cable", "mekanism_extras:*_conductor", "mekanism_extras:*_pipe", "mekanism_extras:*_tube", "mekanism_extras:*_transporter", "thermal:*_duct", "thermal:*_duct_windowed", "computercraft:cable", "powah:energy_cable_*", "create:fluid_pipe", "pneumaticcraft:*_tube", "ppfluids:fluid_pipe", "prettypipes:pipe", "laserio:laser_*", "cyclic:*_pipe", "embers:*_pipe", "embers:item_extractor", "elementalcraft:elementpipe*", "gtceu:*wire", "gtceu:*pipe","oritech:*_pipe", "oritech:superconductor","enderio:conduit"), () -> "", CFConfig::validateBlockName);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> NOT_ALLOWED_BLOCK_STRINGS = BUILDER.comment("List of blocks that are explicitly not allowed to be used as a cover. Supports '*' as a wildcard.")
            .defineListAllowEmpty("not_allowed_blocks", List.of(), () -> "", CFConfig::validateBlockName);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> Z_FIGHTING = BUILDER.comment("List of blocks that need z-fighting fixes. Supports '*' as a wildcard.")
            .defineListAllowEmpty("z_fighting", List.of("ae2:cable_bus"), () -> "", CFConfig::validateBlockName);
    private static final ModConfigSpec.BooleanValue CONSUME_FACADE = BUILDER.comment("Whether the facade should be consumed when placed.")
            .define("consumeFacade", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static final Map<Block, Boolean> allowedBlocks = new HashMap<>();
    private static final Map<Block, Boolean> disallowedBlocks = new HashMap<>();
    private static final Map<Block, Boolean> zFightingBlocks = new HashMap<>();
    private static final List<Pattern> blockPatterns = new ArrayList<>();
    private static final List<Pattern> notAllowedBlockPatterns = new ArrayList<>();
    private static final List<Pattern> zFightingPatterns = new ArrayList<>();
    public static boolean consumeFacade;

    private static boolean validateBlockName(final Object obj) {
        if (obj instanceof String blockName) {
            if (blockName.contains("*")) {
                return true;
            }
            return BuiltInRegistries.BLOCK.containsKey(ResourceLocation.parse(blockName));
        }
        return false;
    }

    public static List<String> downloadListFromGithub(String listType) {
        String githubBaseUrl = "https://raw.githubusercontent.com/Porting-Dead-Mods/Cable-Facades/refs/heads/1.21.1/configs/";
        String filename = switch (listType.toLowerCase()) {
            case "whitelist" -> "whitelist.txt";
            case "blacklist" -> "blacklist.txt";
            case "zfighting" -> "zfighting.txt";
            default -> throw new IllegalArgumentException("Invalid list type: " + listType);
        };
        String githubUrl = githubBaseUrl + filename;

        List<String> downloadedList = new ArrayList<>();

        try {
            URL url = new URL(githubUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isBlank() && !line.startsWith("#")) {
                            downloadedList.add(line.trim());
                        }
                    }
                }
            } else {
                CFMain.LOGGER.error("Failed to download {}. HTTP code: {}", listType, connection.getResponseCode());
            }
        } catch (Exception e) {
            CFMain.LOGGER.error("Error downloading {}: {}", listType, e.getMessage());
        }

        CFMain.LOGGER.info("Downloaded {} {} blocks from GitHub", downloadedList.size(), listType);
        return downloadedList;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        consumeFacade = CONSUME_FACADE.get();

        allowedBlocks.clear();
        disallowedBlocks.clear();
        zFightingBlocks.clear();
        blockPatterns.clear();
        notAllowedBlockPatterns.clear();
        zFightingPatterns.clear();

        // Download and parse allowed blocks
        List<String> downloadedBlockStrings = downloadListFromGithub("whitelist");
        List<String> combinedBlockStrings = new ArrayList<>(BLOCK_STRINGS.get());
        combinedBlockStrings.addAll(downloadedBlockStrings);

        for (String blockName : combinedBlockStrings) {
            if (blockName.contains("*")) {
                String regex = blockName.replace("*", ".*");
                blockPatterns.add(Pattern.compile(regex));
            } else {
                Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockName));
                if (block != null) {
                    allowedBlocks.put(block, true);
                }
            }
        }

        // Download and parse disallowed blocks
        List<String> downloadedNotAllowedBlockStrings = downloadListFromGithub("blacklist");
        List<String> combinedNotAllowedBlockStrings = new ArrayList<>(NOT_ALLOWED_BLOCK_STRINGS.get());
        combinedNotAllowedBlockStrings.addAll(downloadedNotAllowedBlockStrings);

        for (String blockName : combinedNotAllowedBlockStrings) {
            if (blockName.contains("*")) {
                String regex = blockName.replace("*", ".*");
                notAllowedBlockPatterns.add(Pattern.compile(regex));
            } else {
                Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockName));
                if (block != null) {
                    disallowedBlocks.put(block, true);
                }
            }
        }

        // Download and parse z-fighting blocks
        List<String> downloadedZFightingStrings = downloadListFromGithub("zfighting");
        List<String> combinedZFightingStrings = new ArrayList<>(Z_FIGHTING.get());
        combinedZFightingStrings.addAll(downloadedZFightingStrings);

        for (String blockName : combinedZFightingStrings) {
            if (blockName.contains("*")) {
                String regex = blockName.replace("*", ".*");
                zFightingPatterns.add(Pattern.compile(regex));
            } else {
                Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockName));
                if (block != null) {
                    zFightingBlocks.put(block, true);
                }
            }
        }
    }

    public static boolean isBlockAllowed(Block targetBlock) {
        Boolean cached = allowedBlocks.get(targetBlock);
        if (cached != null) {
            return cached;
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(targetBlock);
        if (blockId != null) {
            String blockIdString = blockId.toString();
            for (Pattern pattern : blockPatterns) {
                if (pattern.matcher(blockIdString).matches()) {
                    allowedBlocks.put(targetBlock, true);
                    return true;
                }
            }
        }
        allowedBlocks.put(targetBlock, false);
        return false;
    }

    public static boolean isBlockDisallowed(Block targetBlock) {
        Boolean cached = disallowedBlocks.get(targetBlock);
        if (cached != null) {
            return cached;
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(targetBlock);
        if (blockId != null) {
            String blockIdString = blockId.toString();
            for (Pattern pattern : notAllowedBlockPatterns) {
                if (pattern.matcher(blockIdString).matches()) {
                    disallowedBlocks.put(targetBlock, true);
                    return true;
                }
            }
        }
        disallowedBlocks.put(targetBlock, false);
        return false;
    }

    public static boolean canPatchZFighting(Block targetBlock) {
        Boolean cached = zFightingBlocks.get(targetBlock);
        if (cached != null) {
            return cached;
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(targetBlock);
        if (blockId != null) {
            String blockIdString = blockId.toString();
            for (Pattern pattern : zFightingPatterns) {
                if (pattern.matcher(blockIdString).matches()) {
                    zFightingBlocks.put(targetBlock, true);
                    return true;
                }
            }
        }
        zFightingBlocks.put(targetBlock, false);
        return false;
    }
}