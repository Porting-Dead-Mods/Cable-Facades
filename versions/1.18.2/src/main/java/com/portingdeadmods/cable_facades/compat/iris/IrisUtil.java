package com.portingdeadmods.cable_facades.compat.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class IrisUtil {

    private IrisUtil() {}

    public static boolean areShadersEnabled() {
        try {
            return IrisApi.getInstance().isShaderPackInUse();
        } catch (Throwable t) {
            return false;
        }
    }

    public static void beginBlock(VertexConsumer buffer, BlockState state, BlockPos pos) {
        if (!(buffer instanceof BlockSensitiveBufferBuilder irisBuffer)) {
            return;
        }
        Object2IntMap<BlockState> ids = BlockRenderingSettings.INSTANCE.getBlockStateIds();
        if (ids == null) {
            return;
        }
        int id = ids.getInt(state);
        irisBuffer.beginBlock((short) id, (short) 0, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void endBlock(VertexConsumer buffer) {
        if (buffer instanceof BlockSensitiveBufferBuilder irisBuffer) {
            irisBuffer.endBlock();
        }
    }
}
