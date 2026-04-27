package com.portingdeadmods.cable_facades.api;

import com.portingdeadmods.cable_facades.api.facade_type.FacadeType;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CableFacadesAPI {

    private static final List<String> additionalAllowedBlocks = new ArrayList<>();
    private static final List<String> additionalDisallowedBlocks = new ArrayList<>();
    private static final List<String> additionalZFightingBlocks = new ArrayList<>();
    private static final List<String> additionalHiddenBlocks = new ArrayList<>();
    private static final List<Consumer<CableFacadesAPI>> pendingCallbacks = new ArrayList<>();
    private static boolean initialized = false;

    public void registerAllowedBlocks(String... blocks) {
        additionalAllowedBlocks.addAll(Arrays.asList(blocks));
    }

    public void registerDisallowedBlocks(String... blocks) {
        additionalDisallowedBlocks.addAll(Arrays.asList(blocks));
    }

    public void registerZFightingBlocks(String... blocks) {
        additionalZFightingBlocks.addAll(Arrays.asList(blocks));
    }

    public void registerHiddenBlocks(String... blocks) {
        additionalHiddenBlocks.addAll(Arrays.asList(blocks));
    }

    public static List<String> getAdditionalAllowedBlocks() {
        return additionalAllowedBlocks;
    }

    public static List<String> getAdditionalDisallowedBlocks() {
        return additionalDisallowedBlocks;
    }

    public static List<String> getAdditionalZFightingBlocks() {
        return additionalZFightingBlocks;
    }

    public static List<String> getAdditionalHiddenBlocks() {
        return additionalHiddenBlocks;
    }

    public static CableFacadesAPI getInstance() {
        if (!initialized) {
            initialized = true;
        }
        return SingletonHolder.INSTANCE;
    }

    public static void enqueueAPICallback(Consumer<CableFacadesAPI> callback) {
        if (initialized) {
            callback.accept(getInstance());
        } else {
            pendingCallbacks.add(callback);
        }
    }

    public static void initializeAPI() {
        initialized = true;
        pendingCallbacks.forEach(callback -> callback.accept(getInstance()));
        pendingCallbacks.clear();
    }

    public static FacadeType registerFacadeType(FacadeType type) {
        return FacadeTypes.register(type);
    }

    private static class SingletonHolder {
        private static final CableFacadesAPI INSTANCE = new CableFacadesAPI();
    }
}
