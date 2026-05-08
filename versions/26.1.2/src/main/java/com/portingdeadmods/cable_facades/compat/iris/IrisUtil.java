package com.portingdeadmods.cable_facades.compat.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.CFMain;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

public final class IrisUtil {
    private static final int ALPHA_MASK = 0x88FFFFFF;

    private IrisUtil() {
    }

    public static boolean areShadersEnabled() {
        return CFMain.isIrisLoaded() && IrisLoadedCompat.areShadersEnabled();
    }

    public static VertexConsumer wrapAlpha(VertexConsumer consumer) {
        return CFMain.isIrisLoaded() ? IrisLoadedCompat.wrapAlpha(consumer) : new AlphaWrapper(consumer);
    }

    public static void beginBlock(VertexConsumer buffer, BlockState state, BlockPos pos) {
        if (CFMain.isIrisLoaded()) {
            IrisLoadedCompat.beginBlock(buffer, state, pos);
        }
    }

    public static void endBlock(VertexConsumer buffer) {
        if (CFMain.isIrisLoaded()) {
            IrisLoadedCompat.endBlock(buffer);
        }
    }

    private static final class AlphaWrapper extends VertexConsumerWrapper {
        private AlphaWrapper(VertexConsumer consumer) {
            super(consumer);
        }

        @Override
        public VertexConsumer setColor(int color) {
            super.setColor(color & ALPHA_MASK);
            return this;
        }
    }
}
