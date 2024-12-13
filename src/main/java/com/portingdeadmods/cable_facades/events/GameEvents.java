package com.portingdeadmods.cable_facades.events;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.data.helper.ChunkFacadeMap;
import com.portingdeadmods.cable_facades.networking.CFMessages;
import com.portingdeadmods.cable_facades.networking.s2c.AddFacadedBlocksPacket;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveFacadedBlocksPacket;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CFMain.MODID)
public final class GameEvents {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        String msgReceived = CFMain.MODID + "_update_message_received";
        if (!player.getPersistentData().getBoolean(msgReceived)) {
            player.sendSystemMessage(Component.literal("*Cable-Facades Update*").withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.literal("This update will delete all previously placed facades because we changed the way facades are saved. We apologize for this inconvenience. This is the last time this will happen. On the bright side, the mod's overall performance should be a lot better now."));
            player.getPersistentData().putBoolean(msgReceived, true);
        }
    }

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

        }else if (player.getItemInHand(hand).is(CFItemTags.WRENCHES)
                && facadeState != null) {
            //Rotation!
            if (!level.isClientSide()) {

                //This is a very rudimentary rotation, and some further work will be required for things like stairs and slabs..
                //But this would work with a furnace, chest etc as-is.

                if (facadeState.hasProperty(HorizontalDirectionalBlock.FACING)) {
                    Direction direction = facadeState.getValue(HorizontalDirectionalBlock.FACING);
                    //Init variable here so we can adjust it as we see fit when checking things like stairs and slabs.
                    BlockState newFacadeState = facadeState;

                    //This, in vanilla, is technically explicit support for stairs - but should play nicely with modded blocks too.
                    if (facadeState.hasProperty(BlockStateProperties.HALF) && direction == Direction.WEST) {
                        Half half = facadeState.getValue(BlockStateProperties.HALF);
                        //Inverts the HALF.
                        newFacadeState = newFacadeState.setValue(BlockStateProperties.HALF, half == Half.BOTTOM ? Half.TOP : Half.BOTTOM);
                    }

                    //We only need to rotate the block horizontally here, so this works well.
                    newFacadeState = newFacadeState.setValue(HorizontalDirectionalBlock.FACING, direction.getClockWise());

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

                //Basically explicit slab support. But it works on anything utilising SLAB_TYPE
                else if (facadeState.hasProperty(BlockStateProperties.SLAB_TYPE)) {
                    SlabType slab = facadeState.getValue(BlockStateProperties.SLAB_TYPE);
                    //Inverts the SlabType.
                    BlockState newFacadeState = facadeState.setValue(BlockStateProperties.SLAB_TYPE, nextSlab(slab));
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
    public static void loadChunk(ChunkWatchEvent.Watch event) {
        LevelChunk chunk = event.getChunk();
        ChunkPos chunkPos = event.getPos();
        ServerPlayer serverPlayer = event.getPlayer();
        ServerLevel serverLevel = event.getLevel();

        ChunkFacadeMap facadeMapForChunk = CableFacadeSavedData.get(serverLevel).getFacadeMapForChunk(chunkPos);
        if (facadeMapForChunk != null) {
            CFMessages.sendToChunk(new AddFacadedBlocksPacket(chunkPos, facadeMapForChunk.getChunkMap()), chunk);
        }
    }

    @SubscribeEvent
    public static void unloadChunk(ChunkWatchEvent.UnWatch event) {
        ChunkPos chunkPos = event.getPos();
        ServerPlayer serverPlayer = event.getPlayer();
        CFMessages.sendToPlayer(new RemoveFacadedBlocksPacket(chunkPos), serverPlayer);
    }

    //TODO: Possibly move to another spot?
    //Used to rotate a direction more nicely, as the default order when allowing for up / down would feel clunky.
    private static Direction rotate(Direction direction) {
        return switch(direction) {
            case DOWN -> Direction.NORTH;
            case EAST -> Direction.SOUTH;
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case UP -> Direction.DOWN;
            case WEST -> Direction.UP;
            //Impossible to hit...
            default -> direction;
        };
    }

    private static SlabType nextSlab(SlabType slab) {
        return switch(slab) {
            case BOTTOM -> SlabType.TOP;
            case TOP -> SlabType.DOUBLE;
            case DOUBLE -> SlabType.BOTTOM;
        };
    }
}
