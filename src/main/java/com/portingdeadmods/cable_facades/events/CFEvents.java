package com.portingdeadmods.cable_facades.events;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.networking.CamouflagedBlocksS2CPacket;
import com.portingdeadmods.cable_facades.networking.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CFMain.MODID)
public class CFEvents {
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            CableFacadeSavedData data = CableFacadeSavedData.get(level);
           if(!data.getCamouflagedBlocks().isEmpty()) {
               ModMessages.sendToPlayer(new CamouflagedBlocksS2CPacket(data.getCamouflagedBlocks()), player);
           }
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        //TODO: fix this (clown)
        if (event.getLevel().isClientSide) {
            return;
        }
        if (event.getEntity().getMainHandItem().getTag() != null &&
                event.getEntity().getMainHandItem().getTag().contains("forge:wrenches") &&
                event.getEntity().isShiftKeyDown()) {

            ServerLevel level = (ServerLevel) event.getLevel();
            BlockPos pos = event.getPos();
            CableFacadeSavedData savedData = CableFacadeSavedData.get(level);

            if (savedData.contains(pos)) {
                Block facadeBlock = savedData.getCamouflagedBlocks().get(pos);
                savedData.remove(pos);
                Block.popResource(level, pos, new ItemStack(facadeBlock));
            }
        }
    }
}
