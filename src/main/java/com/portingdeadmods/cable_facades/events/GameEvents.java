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
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
                BlockState facade = FacadeUtils.getFacade(level, pos);
                FacadeUtils.removeFacade(level, pos);
                if (!player.isCreative()) {
                    ItemStack facadeStack = CFItems.FACADE.get().createFacade(facade.getBlock());
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
        InteractionHand hand = event.getHand();

        BlockState facadeState = FacadeUtils.getFacade(level, pos);
        if (player.isShiftKeyDown()
                && player.getItemInHand(hand).is(CFItemTags.WRENCHES)
                && facadeState != null) {
            if (!level.isClientSide()) {
                FacadeUtils.removeFacade(level, pos);

                if (!player.isCreative()) {
                    ItemStack facadeStack = CFItems.FACADE.get().createFacade(facadeState.getBlock());
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), facadeStack);
                } else {
                    level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }

            }
            player.swing(hand);

            updateBlocks(level, pos);
            event.setCanceled(true);

        }
        else if (player.getItemInHand(hand).is(CFItemTags.WRENCHES)
            && facadeState != null) {
            //Rotation!
            if (!level.isClientSide()) {
                
                //This is a very rudimentary rotation, and some further work will be required for things like stairs and slabs..
                //But this would work with a furnace, chest etc as-is.

                if (facadeState.hasProperty(HorizontalDirectionalBlock.FACING)) {
                    Direction direction = facadeState.getValue(HorizontalDirectionalBlock.FACING);                   
                    //We only need to rotate the block horizontally here, so this works well.
                    BlockState newFacadeState = facadeState.setValue(HorizontalDirectionalBlock.FACING, direction.getClockWise());

                    //Readding the facade is a simple easy way to update it both in the chunkmap and for clients.
                    FacadeUtils.removeFacade(level, pos);
                    FacadeUtils.addFacade(level, pos, newFacadeState);
                }

                //The same as above, but for blocks that can face up/down too.
                else if (facadeState.hasProperty(DirectionalBlock.FACING)) {
                    Direction direction = facadeState.getValue(DirectionalBlock.FACING);

                    //Here however, we can't just go clockwise. We need to also account for up / down.
                    BlockState newFacadeState = facadeState.setValue(DirectionalBlock.FACING, rotate(direction));

                    //Readding the facade is a simple easy way to update it both in the chunkmap and for clients.
                    FacadeUtils.removeFacade(level, pos);
                    FacadeUtils.addFacade(level, pos, newFacadeState);
                }

            }
            player.swing(hand);

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


    //TODO: Possibly move to another spot?
    //Used to rotate a direction more nicely, as the default order when allowing for up / down would feel clunky.
    private static Direction rotate(Direction direction) {
        switch (direction) {
            case DOWN:
                return Direction.NORTH;
            case EAST:
                return Direction.SOUTH;
            case NORTH:
                return Direction.EAST;
            case SOUTH:
                return Direction.WEST;
            case UP:
                return Direction.DOWN;
            case WEST:
                return Direction.UP;            
        }
        //Impossible to hit..
        return direction;
    }
}
