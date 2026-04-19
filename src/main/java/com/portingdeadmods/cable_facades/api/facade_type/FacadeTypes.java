package com.portingdeadmods.cable_facades.api.facade_type;

import com.portingdeadmods.cable_facades.CFMain;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FacadeTypes {

    public static final ResourceLocation DEFAULT_ID = ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "facade");

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
}
