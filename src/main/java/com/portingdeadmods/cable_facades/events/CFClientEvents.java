package com.portingdeadmods.cable_facades.events;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.client.renderer.item.FacadeItemRenderer;
import com.portingdeadmods.cable_facades.registries.CFItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class CFClientEvents {
    public static final FacadeItemRenderer FACADE_ITEM_RENDERER = new FacadeItemRenderer();
    public static final ModelResourceLocation FACADE_OUTLINE = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "item/facade_outline"));

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(FACADE_OUTLINE);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return FACADE_ITEM_RENDERER;
            }
        }, CFItems.FACADE);
    }
}
