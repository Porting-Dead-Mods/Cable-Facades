package com.portingdeadmods.cable_facades.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * API for Cable Facades mod.
 * Allows other mods to register blocks that can be covered by facades.
 */
public class CableFacadesAPI {

    private static final List<String> additionalAllowedBlocks = new ArrayList<>();
    private static final List<String> additionalDisallowedBlocks = new ArrayList<>();
    private static final List<String> additionalZFightingBlocks = new ArrayList<>();
    private static final List<String> additionalHiddenBlocks = new ArrayList<>();
    private static final List<Consumer<CableFacadesAPI>> pendingCallbacks = new ArrayList<>();
    private static boolean initialized = false;

    /**
     * Register blocks that can be covered by facades.
     * This should be called during mod initialization.
     *
     * @param blocks Identifiers of blocks or patterns (supports wildcards)
     */
    public void registerAllowedBlocks(String... blocks) {
        additionalAllowedBlocks.addAll(Arrays.asList(blocks));
    }

    /**
     * Register blocks that cannot be covered by facades.
     * This will override allowedBlocks if the same block is in both lists.
     *
     * @param blocks Identifiers of blocks or patterns (supports wildcards)
     */
    public void registerDisallowedBlocks(String... blocks) {
        additionalDisallowedBlocks.addAll(Arrays.asList(blocks));
    }

    /**
     * Register blocks that need z-fighting fixes.
     *
     * @param blocks Identifiers of blocks or patterns (supports wildcards)
     */
    public void registerZFightingBlocks(String... blocks) {
        additionalZFightingBlocks.addAll(Arrays.asList(blocks));
    }

    /**
     * Register blocks that should be hidden when covered by a facade.
     *
     * @param blocks Identifiers of blocks or patterns (supports wildcards)
     */
    public void registerHiddenBlocks(String... blocks) {
        additionalHiddenBlocks.addAll(Arrays.asList(blocks));
    }

    /**
     * Get all additional allowed blocks registered through API
     */
    public static List<String> getAdditionalAllowedBlocks() {
        return additionalAllowedBlocks;
    }

    /**
     * Get all additional disallowed blocks registered through API
     */
    public static List<String> getAdditionalDisallowedBlocks() {
        return additionalDisallowedBlocks;
    }

    /**
     * Get all additional z-fighting blocks registered through API
     */
    public static List<String> getAdditionalZFightingBlocks() {
        return additionalZFightingBlocks;
    }

    /**
     * Get all additional hidden blocks registered through API
     */
    public static List<String> getAdditionalHiddenBlocks() {
        return additionalHiddenBlocks;
    }

    /**
     * Get the CableFacadesAPI instance. For early game access, consider using
     * {@link #enqueueAPICallback(Consumer)} instead.
     *
     * @return The API instance
     */
    public static CableFacadesAPI getInstance() {
        if (!initialized) {
            initialized = true;
        }
        return SingletonHolder.INSTANCE;
    }

    /**
     * Register a callback to be executed when the API is ready.
     * Useful for early initialization before the API is fully available.
     *
     * @param callback Consumer that will receive the API instance
     */
    public static void enqueueAPICallback(Consumer<CableFacadesAPI> callback) {
        if (initialized) {
            callback.accept(getInstance());
        } else {
            pendingCallbacks.add(callback);
        }
    }

    /**
     * Called internally to execute pending callbacks
     */
    public static void initializeAPI() {
        initialized = true;
        pendingCallbacks.forEach(callback -> callback.accept(getInstance()));
        pendingCallbacks.clear();
    }

    private static class SingletonHolder {
        private static final CableFacadesAPI INSTANCE = new CableFacadesAPI();
    }
}