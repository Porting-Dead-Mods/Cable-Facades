package com.portingdeadmods.cable_facades.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccess {
    @Invoker
    void callRenderHitOutline(
            PoseStack matrixStack, VertexConsumer buffer, Entity entity, double x, double y, double z,
            BlockPos blockPos, BlockState blockState
    );
}
