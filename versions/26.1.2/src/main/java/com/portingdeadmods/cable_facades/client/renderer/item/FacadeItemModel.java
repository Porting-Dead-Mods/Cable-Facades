package com.portingdeadmods.cable_facades.client.renderer.item;

import com.mojang.serialization.MapCodec;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeType;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.events.client.ClientRegisterEvents;
import com.portingdeadmods.cable_facades.mixins.ItemStackRenderStateAccess;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class FacadeItemModel implements ItemModel {

    private static final float OUTLINE_SCALE_EPSILON = 0.001F;
    private static final float FLAT_THICKNESS_SCALE = 1.0F / 16.0F;

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, @Nullable ClientLevel level,
                       @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);

        Item item = stack.getItem();
        boolean isDirectional = item instanceof DirectionalFacadeItem;
        Optional<Block> facadeBlock = stack.getOrDefault(CFDataComponents.FACADE_BLOCK, Optional.empty());

        if (facadeBlock.isPresent() && facadeBlock.get().asItem() instanceof BlockItem) {
            FacadeItemSpecialRenderer.Mode mode = isDirectional
                    ? FacadeItemSpecialRenderer.Mode.DIRECTIONAL
                    : FacadeItemSpecialRenderer.Mode.FULL_BLOCK;
            ItemStackRenderState.LayerRenderState contentLayer = output.newLayer();
            contentLayer.setupSpecialModel(new FacadeItemSpecialRenderer(mode), facadeBlock.get());
        }

        appendOutlineLayer(output, stack, resolver, displayContext, level, owner, seed, isDirectional);
    }

    private static void appendOutlineLayer(ItemStackRenderState output, ItemStack stack,
                                           ItemModelResolver resolver, ItemDisplayContext displayContext,
                                           @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed,
                                           boolean isDirectional) {
        Identifier outlineId = resolveOutlineModel(stack);
        ItemModel outlineModel = Minecraft.getInstance().getModelManager().getItemModel(outlineId);

        ItemStackRenderStateAccess access = (ItemStackRenderStateAccess) output;
        int layerCountBefore = access.cableFacades$getActiveLayerCount();
        outlineModel.update(output, stack, resolver, displayContext, level, owner, seed);
        int layerCountAfter = access.cableFacades$getActiveLayerCount();

        Matrix4f transform = outlineTransform(isDirectional);
        ItemStackRenderState.LayerRenderState[] layers = access.cableFacades$getLayers();
        for (int i = layerCountBefore; i < layerCountAfter && i < layers.length; i++) {
            layers[i].setLocalTransform(transform);
        }
    }

    private static Matrix4f outlineTransform(boolean isDirectional) {
        Matrix4f matrix = new Matrix4f();
        if (isDirectional) {
            matrix.translate(0.5F, 0.5F, 0.5F);
            matrix.scale(1.0F + OUTLINE_SCALE_EPSILON, 1.0F + OUTLINE_SCALE_EPSILON, FLAT_THICKNESS_SCALE + OUTLINE_SCALE_EPSILON);
            matrix.translate(-0.5F, -0.5F, -0.5F);
        } else {
            matrix.translate(-(OUTLINE_SCALE_EPSILON / 2.0F), -(OUTLINE_SCALE_EPSILON / 2.0F), -(OUTLINE_SCALE_EPSILON / 2.0F));
            matrix.scale(1.0F + OUTLINE_SCALE_EPSILON, 1.0F + OUTLINE_SCALE_EPSILON, 1.0F + OUTLINE_SCALE_EPSILON);
        }
        return matrix;
    }

    private static Identifier resolveOutlineModel(ItemStack stack) {
        Item item = stack.getItem();
        FacadeType type = null;
        if (item instanceof FacadeItem facade) {
            type = facade.getFacadeType();
        } else if (item instanceof DirectionalFacadeItem directional) {
            type = directional.getFacadeType();
        }
        if (type == null) {
            type = FacadeTypes.defaultType();
        }
        Identifier outline = type != null ? type.outlineModel() : null;
        return outline != null ? outline : ClientRegisterEvents.FACADE_OUTLINE_ID;
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return new FacadeItemModel();
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
        }
    }
}
