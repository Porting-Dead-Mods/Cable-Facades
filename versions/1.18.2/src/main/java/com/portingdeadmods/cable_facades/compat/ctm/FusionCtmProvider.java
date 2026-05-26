package com.portingdeadmods.cable_facades.compat.ctm;

import com.portingdeadmods.cable_facades.api.ctm.CtmProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fusion (Connected Textures) compat. Fusion's {@code ConnectingBakedModel}
 * reads the block position from the {@link net.minecraftforge.client.model.data.IModelData}
 * passed to {@code getQuads}. We already feed the facade pos through
 * {@code getModelData(level, facadePos, state, EmptyModelData.INSTANCE)} in
 * {@code ChunkFacadeBaker}, so Fusion picks up the right neighbour set
 * without us needing to swap the model — this provider exists mainly to
 * advertise the integration and to give a hook point should Fusion ever
 * need a per-state filter.
 */
public final class FusionCtmProvider implements CtmProvider {

    private static final String MOD_ID = "fusion";
    private static final String CONNECTING_MODEL_CLASS =
            "com.supermartijn642.fusion.model.types.connecting.ConnectingBakedModel";

    @Override
    public String requiredModId() {
        return MOD_ID;
    }

    @Override
    public boolean handles(BlockState facadeState) {
        return true;
    }

    @Override
    public BakedModel resolveModel(BakedModel originalModel, BlockState facadeState,
                                   BlockAndTintGetter level, BlockPos facadePos) {
        return originalModel;
    }

    public static boolean isFusionModel(BakedModel model) {
        if (model == null) return false;
        Class<?> c = model.getClass();
        while (c != null && c != Object.class) {
            if (c.getName().equals(CONNECTING_MODEL_CLASS)) return true;
            c = c.getSuperclass();
        }
        return false;
    }
}
