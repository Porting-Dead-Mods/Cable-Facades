package com.portingdeadmods.cable_facades.events;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.networking.CamouflagedBlocksS2CPacket;
import com.portingdeadmods.cable_facades.networking.ModMessages;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CFMain.MODID)
public class CFEvents {
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            CableFacadeSavedData data = CableFacadeSavedData.get(level);
            ModMessages.sendToPlayer(new CamouflagedBlocksS2CPacket(data.getCamouflagedBlocks()), player);
        }
    }
}
