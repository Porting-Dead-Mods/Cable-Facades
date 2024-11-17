package com.portingdeadmods.cable_facades.events;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.data.helper.ChunkFacadeMap;
import com.portingdeadmods.cable_facades.networking.s2c.AddFacadedBlocksPacket;
import com.portingdeadmods.cable_facades.networking.CFMessages;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveFacadedBlocksPacket;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CFMain.MODID)
public final class GameEvents {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level level = event.getPlayer().level();
        BlockPos pos = event.getPos();
        Player player = event.getPlayer();

        if (!level.isClientSide()) {
            if (FacadeUtils.hasFacade(level, pos)) {
                Block facade = FacadeUtils.getFacade(level, pos);
                FacadeUtils.removeFacade(level, pos);
                if (!player.isCreative()) {
                    ItemStack facadeStack = CFItems.FACADE.get().createFacade(facade);
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), facadeStack);
                }
                FacadeUtils.updateBlocks(level, pos);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        Block facadeBlock = FacadeUtils.getFacade(level, pos);
        if (player.isShiftKeyDown()
                && player.getMainHandItem().is(CFItemTags.WRENCHES)
                && facadeBlock != null) {
            if (!level.isClientSide()) {
                FacadeUtils.removeFacade(level, pos);

                if (!player.isCreative()) {
                    ItemStack facadeStack = CFItems.FACADE.get().createFacade(facadeBlock);
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), facadeStack);
                } else {
                    level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }

                updateBlocks(level, pos);
            }
            player.swing(InteractionHand.MAIN_HAND);
            event.setCanceled(true);

        }

    }

    public static void updateBlocks(Level level, BlockPos pos) {
        level.getLightEngine().checkBlock(pos);
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
        level.updateNeighborsAt(pos, state.getBlock());
    }

    @SubscribeEvent
    public static void loadChunk(ChunkWatchEvent.Watch event) {
        LevelChunk chunk = event.getChunk();
        ChunkPos chunkPos = event.getPos();
        ServerPlayer serverPlayer = event.getPlayer();
        ServerLevel serverLevel = event.getLevel();

        ChunkFacadeMap facadeMapForChunk = CableFacadeSavedData.get(serverLevel).getFacadeMapForChunk(chunk.getPos());
        if (facadeMapForChunk != null) {
            CFMessages.sendToPlayer(new AddFacadedBlocksPacket(chunkPos, facadeMapForChunk.getChunkMap()), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void unloadChunk(ChunkWatchEvent.UnWatch event) {
        ChunkPos chunkPos = event.getPos();
        ServerPlayer serverPlayer = event.getPlayer();

        CFMessages.sendToPlayer(new RemoveFacadedBlocksPacket(chunkPos), serverPlayer);
    }
}
