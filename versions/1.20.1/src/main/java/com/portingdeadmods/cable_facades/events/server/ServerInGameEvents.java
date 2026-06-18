package com.portingdeadmods.cable_facades.events.server;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.networking.CFMessages;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveFacadedBlocksPacket;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;

@Mod.EventBusSubscriber(modid = CFMain.MODID)
public final class ServerInGameEvents {
    private static final Duration NEW_WORLD_GRACE_PERIOD = Duration.ofMinutes(10);
    private static final long NEW_WORLD_FALLBACK_GAME_TIME = 20L * 60L * 10L;

    private static final String PLAYER_TAG_ROOT = "cable_facades";
    private static final String PLAYER_KEY_SEEN_MESSAGE = "seen_directional_facade_message";

    private ServerInGameEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CompoundTag root = serverPlayer.getPersistentData().getCompound(PLAYER_TAG_ROOT);
        if (root.getBoolean(PLAYER_KEY_SEEN_MESSAGE)) {
            return;
        }
        ServerLevel overworld = serverPlayer.getServer() != null ? serverPlayer.getServer().getLevel(Level.OVERWORLD) : null;
        if (overworld == null) {
            return;
        }
        if (shouldSuppressDirectionalFacadeUpdateMessage(overworld)) {
            markSeen(serverPlayer);
            return;
        }
        serverPlayer.sendSystemMessage(Component.translatable("cable_facades.message.directional_facade_update").withStyle(ChatFormatting.YELLOW));
        markSeen(serverPlayer);
    }

    private static void markSeen(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.getCompound(PLAYER_TAG_ROOT);
        root.putBoolean(PLAYER_KEY_SEEN_MESSAGE, true);
        persistent.put(PLAYER_TAG_ROOT, root);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level level = event.getLevel() instanceof Level lv ? lv : event.getPlayer().level();
        BlockPos pos = event.getPos();
        Player player = event.getPlayer();

        if (!level.isClientSide()) {
            FacadeData facadeData = FacadeUtils.getFacadeData(level, pos);
            if (facadeData != null) {
                if (facadeData.isFullBlock()) {
                    FacadeItem fullItem = FacadeTypes.fullItemFor(facadeData.facadeType());
                    if (fullItem == null) fullItem = CFItems.FACADE.get();
                    FacadeUtils.removeFacade(level, pos);
                    if (!player.isCreative() && CFConfig.consumeFacade) {
                        ItemStack facadeStack = fullItem.createFacade(facadeData.getFullBlock().getBlock());
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), facadeStack);
                    }
                    event.setCanceled(true);
                } else if (facadeData.isDirectional()) {
                    Direction hitFace = getPlayerLookingFace(player, pos);
                    if (facadeData.hasFace(hitFace)) {
                        BlockState faceState = facadeData.getFace(hitFace);
                        DirectionalFacadeItem directionalItem = FacadeTypes.directionalItemFor(facadeData.facadeType());
                        if (directionalItem == null) directionalItem = CFItems.DIRECTIONAL_FACADE.get();
                        FacadeUtils.removeDirectionalFacade(level, pos, hitFace);
                        if (!player.isCreative() && CFConfig.consumeFacade) {
                            ItemStack facadeStack = directionalItem.createFacade(faceState.getBlock());
                            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), facadeStack);
                        }
                        event.setCanceled(true);
                    }
                }
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

        if (player.isShiftKeyDown() && hand == InteractionHand.OFF_HAND && player.getItemInHand(InteractionHand.MAIN_HAND).is(CFItemTags.WRENCHES)) {
            event.setCanceled(true);
        }

        FacadeData facadeData = FacadeUtils.getFacadeData(level, pos);
        if (facadeData == null || !player.getItemInHand(hand).is(CFItemTags.WRENCHES)) return;

        if (facadeData.isFullBlock()) {
            BlockState facadeState = facadeData.getFullBlock();
            handleWrenchOnFullBlock(player, level, pos, facadeState);
        } else if (facadeData.isDirectional()) {
            Direction hitFace = event.getHitVec() != null ? event.getHitVec().getDirection() : getPlayerLookingFace(player, pos);
            if (facadeData.hasFace(hitFace)) {
                BlockState facadeState = facadeData.getFace(hitFace);
                handleWrenchOnDirectionalFace(player, level, pos, hitFace, facadeState);
            }
        }

        player.swing(hand);
        FacadeUtils.updateBlocks(level, pos);
        event.setCanceled(true);
    }

    private static void handleWrenchOnFullBlock(Player player, Level level, BlockPos pos, BlockState facadeState) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                FacadeData existing = FacadeUtils.getFacadeData(level, pos);
                FacadeItem fullItem = existing != null ? FacadeTypes.fullItemFor(existing.facadeType()) : null;
                if (fullItem == null) fullItem = CFItems.FACADE.get();
                FacadeUtils.removeFacade(level, pos);
                if (!player.isCreative() && CFConfig.consumeFacade) {
                    ItemStack facadeStack = fullItem.createFacade(facadeState.getBlock());
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), facadeStack);
                } else {
                    playPickupSound(level, player);
                }
            }
        } else {
            if (!level.isClientSide()) {
                BlockState newState = rotateFacadeState(facadeState);
                if (newState != facadeState) {
                    FacadeUtils.removeFacade(level, pos);
                    FacadeUtils.addFacade(level, pos, newState);
                }
            }
        }
    }

    private static void handleWrenchOnDirectionalFace(Player player, Level level, BlockPos pos, Direction face, BlockState facadeState) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                FacadeData existing = FacadeUtils.getFacadeData(level, pos);
                DirectionalFacadeItem directionalItem = existing != null ? FacadeTypes.directionalItemFor(existing.facadeType()) : null;
                if (directionalItem == null) directionalItem = CFItems.DIRECTIONAL_FACADE.get();
                FacadeUtils.removeDirectionalFacade(level, pos, face);
                if (!player.isCreative() && CFConfig.consumeFacade) {
                    ItemStack facadeStack = directionalItem.createFacade(facadeState.getBlock());
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), facadeStack);
                } else {
                    playPickupSound(level, player);
                }
            }
        } else {
            if (!level.isClientSide()) {
                BlockState newState = rotateFacadeState(facadeState);
                if (newState != facadeState) {
                    FacadeUtils.removeDirectionalFacade(level, pos, face);
                    FacadeUtils.addDirectionalFacade(level, pos, face, newState);
                }
            }
        }
    }

    private static BlockState rotateFacadeState(BlockState facadeState) {
        if (facadeState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            Direction direction = facadeState.getValue(HorizontalDirectionalBlock.FACING);
            BlockState newState = facadeState;
            if (facadeState.hasProperty(BlockStateProperties.HALF) && direction == Direction.WEST) {
                Half half = facadeState.getValue(BlockStateProperties.HALF);
                newState = newState.setValue(BlockStateProperties.HALF, half == Half.BOTTOM ? Half.TOP : Half.BOTTOM);
            }
            return newState.setValue(HorizontalDirectionalBlock.FACING, direction.getClockWise());
        } else if (facadeState.hasProperty(DirectionalBlock.FACING)) {
            Direction direction = facadeState.getValue(DirectionalBlock.FACING);
            return facadeState.setValue(DirectionalBlock.FACING, rotate(direction));
        } else if (facadeState.hasProperty(BlockStateProperties.SLAB_TYPE)) {
            SlabType slab = facadeState.getValue(BlockStateProperties.SLAB_TYPE);
            return facadeState.setValue(BlockStateProperties.SLAB_TYPE, nextSlab(slab));
        }
        return facadeState;
    }

    private static void playPickupSound(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
    }

    private static Direction getPlayerLookingFace(Player player, BlockPos pos) {
        HitResult hit = player.pick(5.0, 0.0F, false);
        if (hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(pos)) {
            return blockHit.getDirection();
        }
        return Direction.UP;
    }

    @SubscribeEvent
    public static void unloadChunk(ChunkWatchEvent.UnWatch event) {
        ChunkPos chunkPos = event.getPos();
        ServerPlayer serverPlayer = event.getPlayer();
        CFMessages.sendToPlayer(new RemoveFacadedBlocksPacket(chunkPos), serverPlayer);
    }

    private static Direction rotate(Direction direction) {
        return switch (direction) {
            case DOWN -> Direction.NORTH;
            case EAST -> Direction.SOUTH;
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case UP -> Direction.DOWN;
            case WEST -> Direction.UP;
        };
    }

    private static SlabType nextSlab(SlabType slab) {
        return switch (slab) {
            case BOTTOM -> SlabType.TOP;
            case TOP -> SlabType.DOUBLE;
            case DOUBLE -> SlabType.BOTTOM;
        };
    }

    private static boolean shouldSuppressDirectionalFacadeUpdateMessage(ServerLevel level) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(level.getServer().getWorldPath(LevelResource.LEVEL_DATA_FILE), BasicFileAttributes.class);
            return attributes.creationTime().toInstant().isAfter(Instant.now().minus(NEW_WORLD_GRACE_PERIOD));
        } catch (IOException exception) {
            CFMain.LOGGER.debug("Falling back to game-time check for update message gating", exception);
            return level.getGameTime() <= NEW_WORLD_FALLBACK_GAME_TIME && CableFacadeSavedData.get(level).isEmpty();
        }
    }
}
