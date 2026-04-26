package com.portingdeadmods.cable_facades.client.render;

import com.mojang.blaze3d.vertex.*;
import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.client.CFRenderTypes;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FacadePreviewRenderer {
    private static final Direction[] DIRECTIONS = Direction.values();

    public static boolean renderPlacementPreview(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Level world, LivingEntity living, Camera camera, BlockHitResult hit) {
        if (!(living instanceof Player player)) {
            return false;
        }

        PlacementPreview preview = getPlacementPreview(player, hit);
        if (preview == null) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();

        Vec3 cameraPos = camera.position();

        poseStack.pushPose();
        poseStack.translate(
                preview.pos().getX() - cameraPos.x,
                preview.pos().getY() - cameraPos.y,
                preview.pos().getZ() - cameraPos.z
        );

        Block facadedBlock = world.getBlockState(preview.pos()).getBlock();
        if (CFConfig.canPatchZFighting(facadedBlock)) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(FacadeRenderConstants.ZFIGHTING_SCALE, FacadeRenderConstants.ZFIGHTING_SCALE, FacadeRenderConstants.ZFIGHTING_SCALE);
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        if (CFConfig.isScaleUpBlock(facadedBlock)) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(FacadeRenderConstants.SCALE_UP_FACTOR, FacadeRenderConstants.SCALE_UP_FACTOR, FacadeRenderConstants.SCALE_UP_FACTOR);
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        if (preview.directional()) {
            //renderDirectionalPreview(event, world, preview.pos(), preview.face(), preview.facadeState(), poseStack);
        } else {
            VertexConsumer buffer = new PreviewAlphaWrapper(bufferSource.getBuffer(CFRenderTypes.TRANSLUCENT_FACADE));
            renderFullBlockPreview(buffer, mc.level, preview.pos(), preview.facadeState(), poseStack);
        }
        poseStack.popPose();

        return false;
    }

    static class PreviewAlphaWrapper extends VertexConsumerWrapper {
        public PreviewAlphaWrapper(VertexConsumer consumer) {
            super(consumer);
        }

        @Override
        public @NonNull VertexConsumer setColor(int color) {
            super.setColor((color & 0x00FFFFFF) | FacadeRenderConstants.PREVIEW_ALPHA);
            return this;
        }
    }

    private static void renderFullBlockPreview(VertexConsumer buffer, ClientLevel level, BlockPos pos,
                                               BlockState facadeState, PoseStack poseStack) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        mc.getModelManager().getBlockStateModelSet().get(facadeState).collectParts(level, pos, facadeState, RandomSource.create(facadeState.getSeed(pos)), parts);
        QuadInstance quadInstance = new QuadInstance();
        VertexConsumer outlineBuffer = null;
        BlockModelLighter blockModelLighter = new BlockModelLighter();

        quadInstance.setLightCoords(blockModelLighter.getLightCoords(facadeState, level, BlockPos.ZERO));
        quadInstance.setOverlayCoords(OverlayTexture.NO_OVERLAY);

        for (BlockStateModelPart part : parts) {
            putPartQuads(part, poseStack.last(), quadInstance, new int[0], buffer, outlineBuffer);
        }

        //        RandomSource random = RandomSource.create(FACADE_RENDER_SEED);
//        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getModelManager();
//        BakedModel facadeModel = blockRenderer.getBlockModel(facadeState);
//        ModelData modelData = facadeModel.getModelData(world, pos, facadeState, ModelData.EMPTY);
//
//        for (RenderType renderType : facadeModel.getRenderTypes(facadeState, random, ModelData.EMPTY)) {
//            VertexConsumer buffer = new PreviewAlphaWrapper(event.getMultiBufferSource().getBuffer(RenderType.translucent()));
//            blockRenderer.renderBatched(facadeState, pos, world, poseStack, buffer, true, random, modelData, renderType);
//        }
    }

    private static void putPartQuads(
            BlockStateModelPart part,
            PoseStack.Pose pose,
            QuadInstance quadInstance,
            int[] tintLayers,
            VertexConsumer buffer,
            @Nullable VertexConsumer outlineBuffer
    ) {
        for (Direction direction : DIRECTIONS) {
            for (BakedQuad quad : part.getQuads(direction)) {
                putQuad(pose, quad, quadInstance, tintLayers, buffer, outlineBuffer);
            }
        }

        for (BakedQuad quad : part.getQuads(null)) {
            putQuad(pose, quad, quadInstance, tintLayers, buffer, outlineBuffer);
        }
    }

    private static void putQuad(
            PoseStack.Pose pose, BakedQuad quad, QuadInstance instance, int[] tintLayers, VertexConsumer buffer, @Nullable VertexConsumer outlineBuffer
    ) {
        int tintIndex = quad.materialInfo().tintIndex();
        boolean tintColor = tintIndex != -1 && tintIndex < tintLayers.length;
        instance.setColor(tintColor ? tintLayers[tintIndex] : -1);
        buffer.putBakedQuad(pose, quad, instance);
        if (outlineBuffer != null) {
            outlineBuffer.putBakedQuad(pose, quad, instance);
        }
    }

    private static void renderDirectionalPreview(ExtractBlockOutlineRenderStateEvent event, Level world, BlockPos pos,
                                                 Direction face, BlockState facadeState, PoseStack poseStack) {
//        RandomSource random = RandomSource.create(FACADE_RENDER_SEED);
//        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
//        BakedModel facadeModel = blockRenderer.getBlockModel(facadeState);
//        ModelData modelData = facadeModel.getModelData(world, pos, facadeState, ModelData.EMPTY);
//        boolean useAo = Minecraft.useAmbientOcclusion()
//                && facadeState.getLightEmission() == 0
//                && facadeModel.useAmbientOcclusion();
//        List<BakedQuad> slicedQuads = CoverQuadRenderer.sliceQuads(facadeState, pos, facadeModel, face, modelData);
//
//        for (RenderType renderType : facadeModel.getRenderTypes(facadeState, random, ModelData.EMPTY)) {
//            VertexConsumer buffer = new PreviewAlphaWrapper(event.getMultiBufferSource().getBuffer(RenderType.translucent()));
//            for (BakedQuad quad : slicedQuads) {
//                float r = 1f;
//                float g = 1f;
//                float b = 1f;
//                if (quad.getTintIndex() != -1) {
//                    int color = Minecraft.getInstance().getBlockColors().getColor(facadeState, world, pos, quad.getTintIndex());
//                    r = (color >> 16 & 0xFF) / 255f;
//                    g = (color >> 8 & 0xFF) / 255f;
//                    b = (color & 0xFF) / 255f;
//                }
//                FacadeQuadLighter.renderQuad(world, facadeState, pos, poseStack.last(), buffer, quad, r, g, b,
//                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, useAo);
//            }
//        }
    }

    private static PlacementPreview getPlacementPreview(Player player, BlockHitResult hit) {
        PlacementPreview preview = getPlacementPreview(player, InteractionHand.MAIN_HAND, hit);
        return preview != null ? preview : getPlacementPreview(player, InteractionHand.OFF_HAND, hit);
    }

    private static PlacementPreview getPlacementPreview(Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        boolean directional = stack.getItem() instanceof DirectionalFacadeItem;
        if (!(stack.getItem() instanceof FacadeItem) && !directional) {
            return null;
        }

        Level level = player.level();
        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();
        BlockState existingFullFacade = FacadeUtils.getFacade(level, pos);
        BlockState existingDirectionalFace = FacadeUtils.getDirectionalFacade(level, pos, face);

        if (!directional && FacadeUtils.hasFacade(level, pos)) {
            return null;
        }
        if (directional && (existingFullFacade != null || existingDirectionalFace != null)) {
            return null;
        }

        Block facadeBlock = resolveFacadeBlock(player, hand, stack);
        if (facadeBlock == null || !(facadeBlock.asItem() instanceof BlockItem)) {
            return null;
        }

        Block targetBlock = level.getBlockState(pos).getBlock();
        boolean noFacadeTag = level.getBlockState(pos).tags()
                .noneMatch(blockTagKey -> blockTagKey.equals(CFItemTags.SUPPORTS_FACADE));

        if (!CFConfig.isBlockAllowed(targetBlock) && noFacadeTag) {
            return null;
        }
        if (targetBlock == facadeBlock || CFConfig.isBlockDisallowed(facadeBlock)) {
            return null;
        }

        BlockState previewState = facadeBlock.getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, hand, hit)));
        if (previewState == null) {
            previewState = facadeBlock.defaultBlockState();
        }

        return new PlacementPreview(pos, face, previewState, directional);
    }

    private static Block resolveFacadeBlock(Player player, InteractionHand hand, ItemStack stack) {
        Optional<Block> block = stack.get(CFDataComponents.FACADE_BLOCK);
        if (block != null && block.isPresent()) {
            return block.get();
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return null;
        }

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock();
        }
        return null;
    }

    private record PlacementPreview(BlockPos pos, Direction face, BlockState facadeState, boolean directional) {
    }
}
