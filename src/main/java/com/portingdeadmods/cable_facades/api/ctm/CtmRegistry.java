package com.portingdeadmods.cable_facades.api.ctm;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry of {@link CtmProvider}s. Look-ups walk the list in registration
 * order and return the first hit. Providers whose mod is not loaded are
 * filtered out at registration time so the lookup hot path stays cheap.
 */
public final class CtmRegistry {

    private static final List<CtmProvider> PROVIDERS = new ArrayList<>();

    private CtmRegistry() {}

    public static void register(CtmProvider provider) {
        if (provider == null) return;
        String required = provider.requiredModId();
        if (required != null && !required.isEmpty() && !ModList.get().isLoaded(required)) {
            return;
        }
        PROVIDERS.add(provider);
    }

    public static List<CtmProvider> all() {
        return Collections.unmodifiableList(PROVIDERS);
    }

    public static BakedModel resolveModel(BakedModel originalModel, BlockState facadeState,
                                          BlockAndTintGetter level, BlockPos facadePos) {
        for (CtmProvider provider : PROVIDERS) {
            if (provider.handles(facadeState)) {
                BakedModel resolved = provider.resolveModel(originalModel, facadeState, level, facadePos);
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return originalModel;
    }
}
