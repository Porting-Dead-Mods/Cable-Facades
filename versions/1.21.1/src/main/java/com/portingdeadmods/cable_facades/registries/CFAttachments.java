package com.portingdeadmods.cable_facades.registries;

import com.mojang.serialization.Codec;
import com.portingdeadmods.cable_facades.CFMain;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class CFAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, CFMain.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SEEN_DIRECTIONAL_FACADE_UPDATE_MESSAGE = ATTACHMENTS.register(
            "seen_directional_facade_update_message",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SUPPRESS_DIRECTIONAL_FACADE_UPDATE_MESSAGE = ATTACHMENTS.register(
            "suppress_directional_facade_update_message",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );

    private CFAttachments() {
    }
}
