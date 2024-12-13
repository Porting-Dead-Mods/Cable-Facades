package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.ClientFacadeUtils;
import com.portingdeadmods.cable_facades.utils.NetworkingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AddFacadePacket(BlockPos facadePos, BlockState block) {
    public AddFacadePacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), NetworkingUtils.readBlockState(buf));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(facadePos);
        NetworkingUtils.writeBlockState(buf, block);
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
