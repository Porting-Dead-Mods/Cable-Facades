package com.portingdeadmods.cable_facades;

import com.portingdeadmods.cable_facades.api.CableFacadesAPI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@EventBusSubscriber(modid = CFMain.MODID)
public final class CFConfig {

    private static final String GITHUB_CONFIG_BASE_URL =
            "https://raw.githubusercontent.com/Porting-Dead-Mods/Cable-Facades/refs/heads/1.21.1/configs/";

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue CONFIG_VERSION = BUILDER.comment("Config version. Increment to apply changes.")
            .defineInRange("configVersion", 1, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue LAST_CONFIG_VERSION = BUILDER.comment("Previous config version. Do not modify manually.")
            .defineInRange("lastConfigVersion", 0, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue AUTO_UPDATE_CONFIG = BUILDER.comment("Whether new blocks for the config should be fetched from the network.")
            .define("auto_update_config", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_STRINGS = BUILDER.comment("List of blocks that are allowed to be covered. Supports '*' as a wildcard.")
            .defineListAllowEmpty("blocks", List.of("pipez:*_pipe", "mekanism:*_cable", "mekanism:*_conductor", "mekanism:*_pipe", "mekanism:*_tube", "mekanism:*_transporter", "mekanism_extras:*_cable", "mekanism_extras:*_conductor", "mekanism_extras:*_pipe", "mekanism_extras:*_tube", "mekanism_extras:*_transporter", "thermal:*_duct", "thermal:*_duct_windowed", "computercraft:cable", "powah:energy_cable_*", "create:fluid_pipe", "pneumaticcraft:*_tube", "ppfluids:fluid_pipe", "prettypipes:pipe", "laserio:laser_*", "cyclic:*_pipe", "embers:*_pipe", "embers:item_extractor", "elementalcraft:elementpipe*", "gtceu:*wire", "gtceu:*pipe", "oritech:*_pipe", "oritech:superconductor", "enderio:conduit", "ae2:cable_bus"), () -> "", CFConfig::validateBlockName);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> ADDED_BLOCK_STRINGS = BUILDER.comment("List of additional blocks added in this version.")
            .defineListAllowEmpty("added_blocks", List.of(
                    "refinedstorage:cable",
                    "refinedstorage:importer",
                    "refinedstorage:exporter",
                    "toms_storage:inventory_cable",
                    "industrialforegoingsouls:soul_network_pipe",
                    "industrialforegoingsouls:soul_surge",
                    "modern_industrialization:pipe"
            ), () -> "", CFConfig::validateBlockName);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> LAST_VERSION_BLOCKS = BUILDER.comment("List of blocks from the previous version. Do not modify manually.")
            .defineListAllowEmpty("last_version_blocks", new ArrayList<>(), () -> "", CFConfig::validateBlockName);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> NOT_ALLOWED_BLOCK_STRINGS = BUILDER.comment("List of blocks that are explicitly not allowed to be used as a cover. Supports '*' as a wildcard.")
            .defineListAllowEmpty("not_allowed_blocks", List.of(), () -> "", CFConfig::validateBlockName);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> Z_FIGHTING = BUILDER.comment("List of blocks that need z-fighting fixes. Supports '*' as a wildcard.")
            .defineListAllowEmpty("z_fighting", List.of("ae2:cable_bus"), () -> "", CFConfig::validateBlockName);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_WHEN_FACADED = BUILDER.comment("List of blocks that should not render when covered by a facade. Supports '*' as a wildcard.")
            .defineListAllowEmpty("hidden_when_facaded", List.of(), () -> "", CFConfig::validateBlockName);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> SCALE_UP_BLOCKS = BUILDER.comment("List of blocks that need a slight scale-up when facaded (e.g. Create mod blocks). Supports '*' as a wildcard.")
            .defineListAllowEmpty("scale_up_blocks", List.of("*create*"), () -> "", CFConfig::validateBlockName);

    private static final ModConfigSpec.BooleanValue CONSUME_FACADE = BUILDER.comment("Whether the facade should be consumed when placed.")
            .define("consumeFacade", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static final BlockRuleSet ALLOWED_RULES = new BlockRuleSet();
    private static final BlockRuleSet DISALLOWED_RULES = new BlockRuleSet();
    private static final BlockRuleSet Z_FIGHTING_RULES = new BlockRuleSet();
    private static final BlockRuleSet HIDDEN_RULES = new BlockRuleSet();
    private static final BlockRuleSet SCALE_UP_RULES = new BlockRuleSet();

    public static boolean consumeFacade = true;
    public static int configVersion = 1;
    private static int lastConfigVersion = 0;
    private static boolean autoUpdateConfig = true;

    private CFConfig() {
    }

    private static boolean validateBlockName(final Object obj) {
        if (!(obj instanceof String blockName)) {
            return false;
        }
        if (blockName.isBlank()) {
            return false;
        }

        if (blockName.contains("*")) {
            if (blockName.equals("*")) {
                return true;
            }

            String sanitized = blockName.replace('*', 'a');
            if (sanitized.contains(":")) {
                try {
                    Identifier.parse(sanitized);
                    return true;
                } catch (Exception ignored) {
                    return false;
                }
            }

            return !sanitized.isBlank();
        }

        String[] parts = blockName.split(":", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            return false;
        }

        try {
            Identifier.parse(blockName.replace('*', 'a'));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static List<String> downloadListFromGithub(String listType) {
        String filename = switch (listType.toLowerCase()) {
            case "whitelist" -> "whitelist.txt";
            case "blacklist" -> "blacklist.txt";
            case "zfighting" -> "zfighting.txt";
            default -> throw new IllegalArgumentException("Invalid list type: " + listType);
        };

        List<String> downloaded = new ArrayList<>();

        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(GITHUB_CONFIG_BASE_URL + filename).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept", "text/plain");
            connection.setRequestProperty("User-Agent", CFMain.MODID + "/" + CFMain.MODID);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                CFMain.LOGGER.warn("Failed to download {}. HTTP code: {}", listType, responseCode);
                return downloaded;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        downloaded.add(trimmed);
                    }
                }
            }

            CFMain.LOGGER.info("Downloaded {} {} blocks from GitHub", downloaded.size(), listType);
        } catch (Exception e) {
            CFMain.LOGGER.warn("Error downloading {}: {}. Using local config only.", listType, e.getMessage());
        }

        return downloaded;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }

        consumeFacade = CONSUME_FACADE.get();
        configVersion = CONFIG_VERSION.get();
        lastConfigVersion = LAST_CONFIG_VERSION.get();
        autoUpdateConfig = AUTO_UPDATE_CONFIG.get();

        List<String> configuredAllowedBlocks = mergeConfiguredAllowedBlocks();

        ALLOWED_RULES.reload(
                configuredAllowedBlocks,
                autoUpdateConfig ? downloadListFromGithub("whitelist") : List.of(),
                CableFacadesAPI::getAdditionalAllowedBlocks
        );
        DISALLOWED_RULES.reload(
                copyStrings(NOT_ALLOWED_BLOCK_STRINGS.get()),
                autoUpdateConfig ? downloadListFromGithub("blacklist") : List.of(),
                CableFacadesAPI::getAdditionalDisallowedBlocks
        );
        Z_FIGHTING_RULES.reload(
                copyStrings(Z_FIGHTING.get()),
                autoUpdateConfig ? downloadListFromGithub("zfighting") : List.of(),
                CableFacadesAPI::getAdditionalZFightingBlocks
        );
        HIDDEN_RULES.reload(
                copyStrings(HIDDEN_WHEN_FACADED.get()),
                List.of(),
                CableFacadesAPI::getAdditionalHiddenBlocks
        );
        SCALE_UP_RULES.reload(
                copyStrings(SCALE_UP_BLOCKS.get()),
                List.of(),
                () -> List.of()
        );
    }

    private static List<String> mergeConfiguredAllowedBlocks() {
        List<String> currentBlocks = copyStrings(BLOCK_STRINGS.get());
        List<String> addedBlocks = copyStrings(ADDED_BLOCK_STRINGS.get());

        if (!autoUpdateConfig || configVersion <= lastConfigVersion) {
            return currentBlocks;
        }

        CFMain.LOGGER.info("Config version changed from {} to {}. Merging changes...", lastConfigVersion, configVersion);

        if (lastConfigVersion > 0) {
            LAST_VERSION_BLOCKS.set(new ArrayList<>(currentBlocks));
        }

        int mergedCount = 0;
        for (String block : addedBlocks) {
            if (!currentBlocks.contains(block)) {
                currentBlocks.add(block);
                mergedCount++;
            }
        }

        BLOCK_STRINGS.set(currentBlocks);
        ADDED_BLOCK_STRINGS.set(new ArrayList<>());
        LAST_CONFIG_VERSION.set(configVersion);
        CFMain.LOGGER.info("Merged {} new blocks into config", mergedCount);
        return currentBlocks;
    }

    private static List<String> copyStrings(List<? extends String> source) {
        return new ArrayList<>(source);
    }

    public static boolean isBlockAllowed(Block targetBlock) {
        return ALLOWED_RULES.matches(targetBlock);
    }

    public static boolean isBlockDisallowed(Block targetBlock) {
        return DISALLOWED_RULES.matches(targetBlock);
    }

    public static boolean canPatchZFighting(Block targetBlock) {
        return Z_FIGHTING_RULES.matches(targetBlock);
    }

    public static boolean shouldHideWhenFacaded(Block targetBlock) {
        return HIDDEN_RULES.matches(targetBlock);
    }

    public static boolean isScaleUpBlock(Block targetBlock) {
        return SCALE_UP_RULES.matches(targetBlock);
    }

    private static final class BlockRuleSet {
        private final Map<Block, Boolean> cache = new HashMap<>();
        private final List<Pattern> patterns = new ArrayList<>();

        private void reload(List<String> configuredEntries, List<String> downloadedEntries, Supplier<List<String>> apiEntries) {
            cache.clear();
            patterns.clear();

            List<String> combinedEntries = new ArrayList<>(configuredEntries);
            combinedEntries.addAll(downloadedEntries);
            combinedEntries.addAll(apiEntries.get());

            for (String entry : combinedEntries) {
                register(entry);
            }
        }

        private void register(String blockName) {
            if (blockName.contains("*")) {
                patterns.add(Pattern.compile(blockName.replace("*", ".*")));
                return;
            }

            try {
                BuiltInRegistries.BLOCK.get(Identifier.parse(blockName))
                        .ifPresent(holder -> cache.put(holder.value(), true));
            } catch (Exception e) {
                CFMain.LOGGER.warn("Ignoring invalid block config entry '{}': {}", blockName, e.getMessage());
            }
        }

        private boolean matches(Block block) {
            Boolean cached = cache.get(block);
            if (cached != null) {
                return cached;
            }

            Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
            if (blockId != null) {
                String blockIdString = blockId.toString();
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(blockIdString).matches()) {
                        cache.put(block, true);
                        return true;
                    }
                }
            }

            cache.put(block, false);
            return false;
        }
    }
}
