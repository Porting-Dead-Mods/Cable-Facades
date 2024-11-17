package com.portingdeadmods.cable_facades.events;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.networking.s2c.SyncFacadedBlocksS2C;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CFMain.MODID)
public final class GameEvents {
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncFacades(player);
        }
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncFacades(player);
        }
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncFacades(player);
        }
    }

    private static void syncFacades(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        CableFacadeSavedData data = CableFacadeSavedData.get(level);
        if (!data.getCamouflagedBlocks().isEmpty()) {
            ModMessages.sendToPlayer(new SyncFacadedBlocksS2C(data.getCamouflagedBlocks()), player);
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
            FacadeUtils.removeFacade(level, pos);

            if (!player.isCreative()) {
                ItemStack facadeStack = CFItems.FACADE.get().createFacade(facadeBlock);
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), facadeStack);
            } else {
                level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
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
