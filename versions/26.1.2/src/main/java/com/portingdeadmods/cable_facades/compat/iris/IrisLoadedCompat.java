package com.portingdeadmods.cable_facades.compat.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

final class IrisLoadedCompat {
    private IrisLoadedCompat() {
    }

    static boolean areShadersEnabled() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    static VertexConsumer wrapAlpha(VertexConsumer consumer) {
        return new AlphaWrapperIris(consumer);
    }

    static void beginBlock(VertexConsumer buffer, BlockState state, BlockPos pos) {
        if (!(buffer instanceof BlockSensitiveBufferBuilder irisBuffer)) {
            return;
        }
        if (WorldRenderingSettings.INSTANCE.getBlockStateIds() == null) {
            return;
        }
        irisBuffer.beginBlock(
                WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(state),
                (byte) 0,
                (byte) state.getLightEmission(),
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
    }

    static void endBlock(VertexConsumer buffer) {
        if (buffer instanceof BlockSensitiveBufferBuilder irisBuffer) {
            irisBuffer.endBlock();
        }
    }
}
