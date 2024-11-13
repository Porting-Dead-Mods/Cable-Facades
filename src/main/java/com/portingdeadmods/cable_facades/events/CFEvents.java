package com.portingdeadmods.cable_facades.events;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.networking.CamouflagedBlocksS2CPacket;
import com.portingdeadmods.cable_facades.networking.ModMessages;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;

@Mod.EventBusSubscriber(modid = CFMain.MODID)
public class CFEvents {
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            CableFacadeSavedData data = CableFacadeSavedData.get(level);
            if (!data.getCamouflagedBlocks().isEmpty()) {
                ModMessages.sendToPlayer(new CamouflagedBlocksS2CPacket(data.getCamouflagedBlocks()), player);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        if (player.isShiftKeyDown()
                && player.getMainHandItem().getTags().toList().contains(CFItemTags.WRENCHES)
                && FacadeUtils.hasFacade(level, pos)) {
            if (level instanceof ServerLevel serverLevel) {
                CableFacadeSavedData savedData = CableFacadeSavedData.get(serverLevel);
                Block facadeBlock = savedData.getCamouflagedBlocks().get(pos);
                savedData.remove(pos);
                ItemStack facadeStack = new ItemStack(CFItems.FACADE.get());
                CompoundTag nbtData = new CompoundTag();
                nbtData.putString("facade_block",BuiltInRegistries.BLOCK.getKey(facadeBlock).toString());
                facadeStack.setTag(nbtData);

                ItemHandlerHelper.giveItemToPlayer(player, facadeStack);
            } else {
                CFClientEvents.CAMOUFLAGED_BLOCKS.remove(pos);
            }
            player.swing(event.getHand());
            event.setCanceled(true);
        }

    }
}
