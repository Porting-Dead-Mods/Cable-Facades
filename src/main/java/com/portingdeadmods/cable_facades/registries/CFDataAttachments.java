package com.portingdeadmods.cable_facades.registries;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.ChunkFacadeMap;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class CFDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CFMain.MODID);

    public static final Supplier<AttachmentType<ChunkFacadeMap>> FACADES = ATTACHMENT_TYPES.register("facades", () -> AttachmentType.builder(() -> new ChunkFacadeMap())
            .serialize(ChunkFacadeMap.CODEC).build());
}
