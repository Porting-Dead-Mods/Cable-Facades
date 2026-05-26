package com.portingdeadmods.cable_facades.mixins;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(LevelRenderer.class)
public interface LevelRendererAccess {
    @Invoker
    void callRenderHitOutline(
            PoseStack matrixStackIn, VertexConsumer bufferIn, Entity entityIn, double xIn, double yIn, double zIn,
            BlockPos blockPosIn, BlockState blockStateIn
    );

    @Invoker
    static void callRenderShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape,
                                double x, double y, double z,
                                float red, float green, float blue, float alpha) {
        throw new AssertionError();
    }
}