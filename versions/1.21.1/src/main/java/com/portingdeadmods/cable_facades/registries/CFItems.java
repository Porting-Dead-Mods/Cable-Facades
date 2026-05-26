package com.portingdeadmods.cable_facades.registries;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.compat.iris.IrisUtil;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.events.GameClientEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;

public class CFItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CFMain.MODID);

    public static final DeferredItem<FacadeItem> FACADE = ITEMS.register("facade", () -> new FacadeItem(new Item.Properties()
            .component(CFDataComponents.FACADE_BLOCK, Optional.empty())
            .component(CFDataComponents.HAS_FACADE_REMAINDER, false)));

    public static final DeferredItem<DirectionalFacadeItem> DIRECTIONAL_FACADE = ITEMS.register("directional_facade", () -> new DirectionalFacadeItem(new Item.Properties()
            .component(CFDataComponents.FACADE_BLOCK, Optional.empty())
            .component(CFDataComponents.HAS_FACADE_REMAINDER, false)));

    public static final DeferredItem<Item> WRENCH = ITEMS.register("facade_wrench", () -> new Item(new Item.Properties().stacksTo(1)) {
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
            HitResult hitResult = player.pick(3D, 0.0F, false);
            if (level.isClientSide && hitResult.getType().equals(HitResult.Type.MISS) && !player.isShiftKeyDown()) {
                GameClientEvents.facadeTransparency = !GameClientEvents.facadeTransparency;
                ChatFormatting messageColor = GameClientEvents.facadeTransparency ? ChatFormatting.GREEN : ChatFormatting.RED;
                Component message = Component.translatable("cable_facades.message.transparency_enabled").append(Component.translatable(GameClientEvents.facadeTransparency ? "cable_facades.tooltip.enabled" : "cable_facades.tooltip.disabled").withStyle(messageColor));
                if(CFMain.isIrisLoaded()){
                    if(IrisUtil.areShadersEnabled()){
                        player.displayClientMessage(Component.translatable("cable_facades.message.shaders_warning").withStyle(ChatFormatting.YELLOW).append(Component.translatable("cable_facades.message.shaders_detected").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)).append(Component.translatable("cable_facades.message.transparency_may_not_work").withStyle(ChatFormatting.GRAY)), true);
                    } else {
                        player.displayClientMessage(message, true);
                    }
                } else {
                    player.displayClientMessage(message, true);
                }
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.4f, GameClientEvents.facadeTransparency ? 0.01f : 0.09f);
            }

            return super.use(level, player, usedHand);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
            tooltipComponents.add(Component.translatable("cable_facades.tooltip.toggle_transparency").withStyle(ChatFormatting.GRAY));

            ChatFormatting stateColor = GameClientEvents.facadeTransparency ? ChatFormatting.GREEN : ChatFormatting.RED;
            tooltipComponents.add(Component.translatable("cable_facades.tooltip.current_state").append(Component.translatable(GameClientEvents.facadeTransparency ? "cable_facades.tooltip.enabled" : "cable_facades.tooltip.disabled").withStyle(stateColor)));

            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        }
    });
}
