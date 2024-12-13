package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.ClientFacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RemoveFacadePacket(BlockPos facadePos) {
    public RemoveFacadePacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.facadePos);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientFacadeManager.FACADED_BLOCKS.remove(this.facadePos);
            ClientFacadeUtils.updateBlocks(this.facadePos);
        });
    }
}