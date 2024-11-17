package com.portingdeadmods.cable_facades.content.items;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import com.portingdeadmods.cable_facades.events.ClientStuff;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class FacadeItem extends Item {
    public static final String FACADE_BLOCK = "facade_block";

    public FacadeItem(Properties properties) {
        super(properties);
    }

    // DEBUGGING CODE
    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        if (p_41432_.isClientSide()) {
            CFMain.LOGGER.debug("Facades: {}", ClientFacadeManager.FACADED_BLOCKS);
            CFMain.LOGGER.debug("Loaded: {}", ClientFacadeManager.LOADED_BLOCKS);
        }
        return super.use(p_41432_, p_41433_, p_41434_);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();

        if (itemStack.hasTag()) {
            CompoundTag tag = itemStack.getTag();
            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(tag.getString(FACADE_BLOCK)));
            Block targetBlock = context.getLevel().getBlockState(pos).getBlock();

            if (!CFConfig.isBlockAllowed(targetBlock)
                    && context.getLevel().getBlockState(pos).getTags().noneMatch(blockTagKey -> blockTagKey.equals(CFItemTags.SUPPORTS_FACADE))){
                return InteractionResult.FAIL;
            }

            // Prevent block from being facaded with itself
            if (targetBlock == block) {
                return InteractionResult.FAIL;
            }

            // Ensure there is not facade yet
            if (FacadeUtils.hasFacade(level, pos)) {
                return InteractionResult.FAIL;
            }

            FacadeUtils.addFacade(level, pos, block);

            if (!context.getPlayer().isCreative() && CFConfig.consumeFacade) {
                itemStack.shrink(1);
            }

            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, 3);
            level.updateNeighborsAt(pos, state.getBlock());
            // Update self and surrounding
            level.getLightEngine().checkBlock(pos);

            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            CompoundTag tag = itemStack.getTag();
            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(tag.getString(FACADE_BLOCK)));
            BlockItem blockItem = (BlockItem) block.asItem();
            return Component.literal("Facade - " + blockItem.getDescription().getString());
        }
        return Component.literal("Facade - Empty");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ClientStuff.FACADE_ITEM_RENDERER;
            }
        });
    }

    public ItemStack createFacade(Block block) {
        ItemStack facadeStack = new ItemStack(CFItems.FACADE.get());
        CompoundTag nbtData = new CompoundTag();
        nbtData.putString(FacadeItem.FACADE_BLOCK, BuiltInRegistries.BLOCK.getKey(block).toString());
        facadeStack.setTag(nbtData);
        return facadeStack;
    }
}
