package com.portingdeadmods.cable_facades.events.client;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeType;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.client.renderer.item.FacadeItemRenderer;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.registries.CFItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientRegisterEvents {
    public static final FacadeItemRenderer FACADE_ITEM_RENDERER = new FacadeItemRenderer();
    public static final ModelResourceLocation FACADE_OUTLINE = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "item/facade_outline"));

    private static final List<DeferredItem<? extends Item>> FACADE_ITEM_HOLDERS = new CopyOnWriteArrayList<>();

    /**
     * Called by downstream mods to register extra facade items with CF's BEWLR.
     */
    public static void registerFacadeItem(DeferredItem<? extends Item> item) {
        FACADE_ITEM_HOLDERS.add(item);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        Set<ResourceLocation> registered = new HashSet<>();
        event.register(FACADE_OUTLINE);
        registered.add(FACADE_OUTLINE.id());
        for (FacadeType type : FacadeTypes.all()) {
            if (type.outlineModel() == null) continue;
            if (registered.add(type.outlineModel())) {
                event.register(ModelResourceLocation.standalone(type.outlineModel()));
            }
        }
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions facadeExtension = new IClientItemExtensions() {
            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return FACADE_ITEM_RENDERER;
            }
        };
        event.registerItem(facadeExtension, CFItems.FACADE);
        event.registerItem(facadeExtension, CFItems.DIRECTIONAL_FACADE);
        for (DeferredItem<? extends Item> holder : FACADE_ITEM_HOLDERS) {
            Item item = holder.get();
            if (item instanceof FacadeItem || item instanceof DirectionalFacadeItem) {
                event.registerItem(facadeExtension, holder);
            }
        }
    }
}
