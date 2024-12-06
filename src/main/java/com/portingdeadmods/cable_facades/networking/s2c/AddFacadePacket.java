package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.ClientFacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Supplier;

public record AddFacadePacket(BlockPos facadePos, Block block) {
    public AddFacadePacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), Registry.BLOCK.get(buf.readResourceLocation()));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(facadePos);
        buf.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)));
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientFacadeManager.FACADED_BLOCKS.put(facadePos, block);
            ClientFacadeUtils.updateBlocks(this.facadePos);
        });
        return true;
    }
}
