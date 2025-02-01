package com.portingdeadmods.cable_facades.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.mixins.LevelRendererAccess;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.registries.CFRenderTypes;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT)
public final class GameClientEvents {

    public static final ThreadLocal<Boolean> RENDERING_FACADE = ThreadLocal.withInitial(() -> false);
    private static float facadeTransparency = 1;
    private static final RenderType FACADE_RENDER_TYPE = new RenderType(
            CFMain.MODID + ":facades",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            131072,
            true,
            true,
            () -> {
                RenderType.translucent().setupRenderState();
                RenderSystem.setShaderColor(1, 1, 1, facadeTransparency);
            },
            () -> {
                RenderType.translucent().clearRenderState();
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
    ) {
    };
    private static final RandomSource RANDOM = RandomSource.create();

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        Map<BlockPos, @Nullable BlockState> chunkFacades = ClientFacadeManager.FACADED_BLOCKS;
        if (chunkFacades == null || chunkFacades.isEmpty()) {
            return;
        }

        Frustum frustum = event.getFrustum();
        List<Map.Entry<BlockPos, BlockState>> visibleFacades = chunkFacades.entrySet().stream()
                .filter(entry -> {
                    if (entry == null) return false;
                    BlockPos pos = entry.getKey();
                    if (pos == null) return false;
                    AABB boundingBox = new AABB(pos);
                    return frustum.isVisible(boundingBox);
                })
                .toList();

        if (visibleFacades.isEmpty()) {
            return;
        }

        facadeTransparency = mc.player.getMainHandItem().is(CFItems.WRENCH.get()) ? 0.5f : 1;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(FACADE_RENDER_TYPE);

        ClientLevel level = mc.level;
        Vec3 cameraPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (Map.Entry<BlockPos, BlockState> entry : visibleFacades) {
            BlockPos pos = entry.getKey();
            BlockState facadeState = entry.getValue();
            if (pos == null || facadeState == null) {
                continue;
            }

            RENDERING_FACADE.set(true);
            BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
            BakedModel facadeModel = blockRenderer.getBlockModel(facadeState);
            ModelData modelData = facadeModel.getModelData(level, pos, facadeState, ModelData.EMPTY);

            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

            blockRenderer.renderBatched(facadeState, pos, level, poseStack, buffer, true, RANDOM, modelData, null);

            poseStack.popPose();
            RENDERING_FACADE.set(false);
        }

        poseStack.popPose();
        bufferSource.endBatch(FACADE_RENDER_TYPE);
    }

    @SubscribeEvent
    public static void renderOutline(RenderHighlightEvent.Block event) {
        if (event.getCamera().getEntity() instanceof LivingEntity living) {
            Level world = living.level();
            BlockHitResult rtr = event.getTarget();
            BlockPos pos = rtr.getBlockPos();
            Vec3 renderView = event.getCamera().getPosition();

            BlockState targetBlock = world.getBlockState(rtr.getBlockPos());
            if (FacadeUtils.hasFacade(world, pos)) {
                ((LevelRendererAccess) event.getLevelRenderer()).callRenderHitOutline(
                        event.getPoseStack(), event.getMultiBufferSource().getBuffer(CFRenderTypes.LINES_NONTRANSLUCENT),
                        living, renderView.x, renderView.y, renderView.z,
                        pos, targetBlock
                );
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ClientFacadeManager.FACADED_BLOCKS.clear();
    }
}