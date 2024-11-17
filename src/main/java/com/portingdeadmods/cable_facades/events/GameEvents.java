package com.portingdeadmods.cable_facades.events;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.networking.CamouflagedBlocksS2CPacket;
import com.portingdeadmods.cable_facades.networking.ModMessages;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CFMain.MODID)
public final class GameEvents {
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
    public static void onBlockBreak(BlockEvent.BreakEvent event){
        Level level = event.getPlayer().level();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (FacadeUtils.hasFacade(level, pos)) {
            if (level instanceof ServerLevel serverLevel) {
                CableFacadeSavedData data = CableFacadeSavedData.get(serverLevel);
                Block camoBlock = data.getCamouflagedBlocks().get(pos);
                ItemStack facadeStack = new ItemStack(CFItems.FACADE.get());
                CompoundTag nbtData = new CompoundTag();
                nbtData.putString("facade_block", BuiltInRegistries.BLOCK.getKey(camoBlock).toString());
                facadeStack.setTag(nbtData);
                data.remove(pos);
                ModMessages.sendToClients(new CamouflagedBlocksS2CPacket(data.getCamouflagedBlocks()));
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), facadeStack);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        if (player.isShiftKeyDown()
                && player.getMainHandItem().is(CFItemTags.WRENCHES)
                && FacadeUtils.hasFacade(level, pos)) {
            if (level instanceof ServerLevel serverLevel) {
                CableFacadeSavedData savedData = CableFacadeSavedData.get(serverLevel);
                Block facadeBlock = savedData.getCamouflagedBlocks().get(pos);
                savedData.remove(pos);
                ItemStack facadeStack = new ItemStack(CFItems.FACADE.get());
                CompoundTag nbtData = new CompoundTag();
                nbtData.putString("facade_block",BuiltInRegistries.BLOCK.getKey(facadeBlock).toString());
                facadeStack.setTag(nbtData);

                if (!player.isCreative()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), facadeStack);
                } else {
                    level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }
            } else {
                ClientCamoManager.CAMOUFLAGED_BLOCKS.remove(pos);
            }
            player.swing(event.getHand());

            level.getLightEngine().checkBlock(pos);
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, 3);
            level.updateNeighborsAt(pos, state.getBlock());
            event.setCanceled(true);
        }

    }
}
