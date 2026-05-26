package com.portingdeadmods.cable_facades.api.ctm;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hook for connected-textures mods so facades can render with their CTM
 * applied. Implementations are registered via
 * {@link CtmRegistry#register(CtmProvider)}.
 *
 * <p>When a facade quad is being baked, the registry walks providers in
 * registration order and uses the first one that {@link #handles(BlockState)}
 * the facade state. The provider is then asked to {@link #resolveModel} a
 * {@link BakedModel} suitable for that facade's position. The position is the
 * facade pos so connecting predicates see neighbours of the facade rather
 * than the underlying cable.</p>
 */
public interface CtmProvider {

    /**
     * @return the mod id this provider depends on. Used to skip registration
     *         when the mod is not loaded.
     */
    String requiredModId();

    /**
     * @return whether this provider should produce a CTM model for the given
     *         facade block state.
     */
    boolean handles(BlockState facadeState);

    /**
     * Returns the model that should be used when rendering the facade.
     *
     * @param originalModel the vanilla model {@link net.minecraft.client.renderer.block.BlockRenderDispatcher#getBlockModel}
     *                      returned for {@code facadeState}
     * @param facadeState   the block state the facade is displaying
     * @param level         the chunk-section view (do not query the full client world)
     * @param facadePos     the facade's world position (use this for CTM neighbour lookups)
     * @return the model to render, never {@code null}; may return {@code originalModel}
     */
    BakedModel resolveModel(BakedModel originalModel, BlockState facadeState,
                            BlockAndTintGetter level, BlockPos facadePos);
}
