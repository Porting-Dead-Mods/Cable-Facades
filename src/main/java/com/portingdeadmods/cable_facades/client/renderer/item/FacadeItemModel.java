package com.portingdeadmods.cable_facades.client.renderer.item;

import com.mojang.serialization.MapCodec;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.BundleSelectedItemSpecialRenderer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class FacadeItemModel implements ItemModel {
    private static final FacadeItemModel INSTANCE = new FacadeItemModel();

    @SuppressWarnings("OptionalIsPresent")
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);
        Optional<Block> selectedItem = item.getOrDefault(CFDataComponents.FACADE_BLOCK, Optional.empty());
        if (selectedItem.isPresent()) {
            resolver.appendItemLayers(output, new ItemStack(selectedItem.get().asItem()), displayContext, level, owner, seed);
        }

    }

    public enum Unbaked implements ItemModel.Unbaked {
        INSTANCE;

        public static final MapCodec<FacadeItemModel.Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        public MapCodec<FacadeItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return FacadeItemModel.INSTANCE;
        }

        public void resolveDependencies(ResolvableModel.Resolver resolver) {
        }
    }

}
