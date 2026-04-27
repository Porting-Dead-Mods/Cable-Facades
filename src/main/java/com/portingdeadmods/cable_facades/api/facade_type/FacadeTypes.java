package com.portingdeadmods.cable_facades.api.facade_type;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FacadeTypes {

    public static final ResourceLocation DEFAULT_ID = new ResourceLocation(CFMain.MODID, "facade");

    private static final Map<ResourceLocation, FacadeType> REGISTRY = new ConcurrentHashMap<>();

    private FacadeTypes() {}

    public static FacadeType register(FacadeType type) {
        REGISTRY.put(type.id(), type);
        return type;
    }

    public static FacadeType get(ResourceLocation id) {
        FacadeType type = REGISTRY.get(id);
        return type != null ? type : REGISTRY.get(DEFAULT_ID);
    }

    public static FacadeType defaultType() {
        return REGISTRY.get(DEFAULT_ID);
    }

    public static boolean isRegistered(ResourceLocation id) {
        return REGISTRY.containsKey(id);
    }

    public static Collection<FacadeType> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    @Nullable
    public static FacadeItem fullItemFor(ResourceLocation id) {
        FacadeType type = get(id);
        if (type != null && type.fullItem() != null) {
            return type.fullItem().get();
        }
        FacadeType fallback = defaultType();
        if (fallback != null && fallback.fullItem() != null) {
            return fallback.fullItem().get();
        }
        return null;
    }

    @Nullable
    public static DirectionalFacadeItem directionalItemFor(ResourceLocation id) {
        FacadeType type = get(id);
        if (type != null && type.directionalItem() != null) {
            return type.directionalItem().get();
        }
        FacadeType fallback = defaultType();
        if (fallback != null && fallback.directionalItem() != null) {
            return fallback.directionalItem().get();
        }
        return null;
    }
}
