package com.portingdeadmods.cable_facades.client.renderer.item;

import com.mojang.serialization.MapCodec;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.events.client.ClientRegisterEvents;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FacadeItemModel implements ItemModel {

    private static final Identifier FACADE_BASE_MODEL = Identifier.fromNamespaceAndPath(CFMain.MODID, "item/facade");
    private static final Identifier DIRECTIONAL_BASE_MODEL = Identifier.fromNamespaceAndPath(CFMain.MODID, "item/directional_facade");

    private final ModelRenderProperties facadeProperties;
    private final ModelRenderProperties directionalProperties;
    private final Matrix4fc transformation;
    private final List<BakedQuad> outlineQuads;

    public FacadeItemModel(ModelRenderProperties facadeProperties, ModelRenderProperties directionalProperties,
                           Matrix4fc transformation, List<BakedQuad> outlineQuads) {
        this.facadeProperties = facadeProperties;
        this.directionalProperties = directionalProperties;
        this.transformation = transformation;
        this.outlineQuads = outlineQuads;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, @Nullable ClientLevel level,
                       @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);

        Item item = stack.getItem();
        boolean isDirectional = item instanceof DirectionalFacadeItem;
        Optional<Block> facadeBlock = stack.getOrDefault(CFDataComponents.FACADE_BLOCK, Optional.empty());
        output.appendModelIdentityElement(facadeBlock.orElse(null));

        FacadeItemSpecialRenderer.Mode mode = isDirectional
                ? FacadeItemSpecialRenderer.Mode.DIRECTIONAL
                : FacadeItemSpecialRenderer.Mode.FULL_BLOCK;
        Block contained = facadeBlock.filter(b -> b.asItem() instanceof BlockItem).orElse(null);

        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        layer.setupSpecialModel(new FacadeItemSpecialRenderer(mode, outlineQuads), contained);
        layer.setLocalTransform(this.transformation);
        ModelRenderProperties properties = isDirectional ? this.directionalProperties : this.facadeProperties;
        properties.applyToLayer(layer, displayContext);
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return new FacadeItemModel(
                    modelProperties(context, FACADE_BASE_MODEL),
                    modelProperties(context, DIRECTIONAL_BASE_MODEL),
                    transformation,
                    bakeOutlineQuads(context, ClientRegisterEvents.FACADE_OUTLINE_ID)
            );
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(FACADE_BASE_MODEL);
            resolver.markDependency(DIRECTIONAL_BASE_MODEL);
            resolver.markDependency(ClientRegisterEvents.FACADE_OUTLINE_ID);
        }

        private static ModelRenderProperties modelProperties(ItemModel.BakingContext context, Identifier modelId) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(modelId);
            TextureSlots textureSlots = model.getTopTextureSlots();
            return ModelRenderProperties.fromResolvedModel(baker, model, textureSlots);
        }

        private static List<BakedQuad> bakeOutlineQuads(ItemModel.BakingContext context, Identifier modelId) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(modelId);
            TextureSlots slots = model.getTopTextureSlots();
            ModelState state = BlockModelRotation.IDENTITY;
            QuadCollection quads = model.bakeTopGeometry(slots, baker, state);

            List<BakedQuad> result = new ArrayList<>(6);
            result.addAll(quads.getQuads(null));
            for (Direction dir : Direction.values()) {
                result.addAll(quads.getQuads(dir));
            }
            return result;
        }
    }
}
