package com.portingdeadmods.cable_facades.events.client;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeType;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.client.renderer.item.FacadeItemRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientRegisterEvents {

    public static final FacadeItemRenderer FACADE_ITEM_RENDERER = new FacadeItemRenderer();
    public static final ModelResourceLocation FACADE_OUTLINE =
            new ModelResourceLocation(new ResourceLocation(CFMain.MODID, "facade_outline"), "inventory");

    private ClientRegisterEvents() {}

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        Set<ResourceLocation> registered = new HashSet<>();
        event.register(FACADE_OUTLINE);
        registered.add(FACADE_OUTLINE);
        for (FacadeType type : FacadeTypes.all()) {
            ResourceLocation outline = type.outlineModel();
            if (outline == null) continue;
            ModelResourceLocation key = new ModelResourceLocation(outline, "inventory");
            if (registered.add(key)) {
                event.register(key);
            }
        }
    }
}
