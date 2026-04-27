package com.portingdeadmods.cable_facades.events.network;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.networking.CFMessages;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = CFMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class NetworkingEvents {
    private NetworkingEvents() {}

    @SubscribeEvent
    public static void registerPayloads(FMLCommonSetupEvent event) {
        event.enqueueWork(CFMessages::register);
    }
}
