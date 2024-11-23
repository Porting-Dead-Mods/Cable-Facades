package com.portingdeadmods.cable_facades.events;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.data.helper.ChunkFacadeMap;
import com.portingdeadmods.cable_facades.networking.s2c.AddFacadedBlocksPayload;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveFacadedBlocksPayload;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CFMain.MODID)
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
                event.setCanceled(true);
            }
        }
        FacadeUtils.updateBlocks(level, pos);
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

            }
            player.swing(InteractionHand.MAIN_HAND);

            updateBlocks(level, pos);
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
    public static void loadChunk(ChunkWatchEvent.Sent event) {
        ChunkPos chunkPos = event.getPos();
        ServerPlayer serverPlayer = event.getPlayer();
        ServerLevel serverLevel = event.getLevel();

        CableFacadeSavedData data = CableFacadeSavedData.get(serverLevel);
        ChunkFacadeMap facadeMapForChunk = data.getFacadeMapForChunk(chunkPos);
        if (facadeMapForChunk != null) {
            CFMain.LOGGER.debug("Server Facaded Blocks: {}", facadeMapForChunk.getChunkMap());
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, chunkPos, new AddFacadedBlocksPayload(chunkPos, facadeMapForChunk.getChunkMap()));
        }
    }

    @SubscribeEvent
    public static void unloadChunk(ChunkWatchEvent.UnWatch event) {
        ChunkPos chunkPos = event.getPos();
        ServerPlayer serverPlayer = event.getPlayer();
        PacketDistributor.sendToPlayer(serverPlayer, new RemoveFacadedBlocksPayload(chunkPos));
    }
}
