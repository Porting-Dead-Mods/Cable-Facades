package com.portingdeadmods.cable_facades.compat.embeddium;

import com.portingdeadmods.cable_facades.client.render.ChunkFacadeBaker;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import net.minecraft.core.SectionPos;
import org.embeddedt.embeddium.api.ChunkMeshEvent;
import org.embeddedt.embeddium.api.MeshAppender;

public final class EmbeddiumIntegration {

    private EmbeddiumIntegration() {}

    public static void register() {
        ChunkMeshEvent.BUS.addListener(EmbeddiumIntegration::onChunkMesh);
    }

    private static void onChunkMesh(ChunkMeshEvent event) {
        if (ClientFacadeManager.isEmpty()) return;

        SectionPos section = event.getSectionOrigin();
        boolean hasFacadeInSection = ClientFacadeManager.entryStream()
                .anyMatch(e -> SectionPos.of(e.getKey()).equals(section));
        if (!hasFacadeInSection) return;

        event.addMeshAppender(EmbeddiumIntegration::appendMesh);
    }

    private static void appendMesh(MeshAppender.Context context) {
        SectionPos origin = context.sectionOrigin();
        ChunkFacadeBaker.bakeSection(origin.origin(), context.blockRenderView(), context.vertexConsumerProvider());
    }
}
