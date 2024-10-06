package net.kubik.cobaltmod.compat.sodium;

import net.kubik.cobaltmod.Cobalt;
import net.kubik.cobaltmod.util.ChunkCalculator;

import java.lang.reflect.Method;

/**
 * Handles the direct interaction with Sodium's internals using reflection.
 */
public class SodiumHooks {
    /** The chunk calculator instance. */
    private static ChunkCalculator chunkCalculator;

    /**
     * Initializes the Sodium hooks.
     * @param calculator The chunk calculator instance.
     */
    public static void init(ChunkCalculator calculator) {
        chunkCalculator = calculator;
        try {
            initSodiumHooks();
        } catch (Exception e) {
            Cobalt.LOGGER.error("Failed to initialize Sodium hooks", e);
        }
    }

    /**
     * Sets up the hooks into Sodium's rendering system.
     * This method uses reflection to interact with Sodium's classes without creating a hard dependency.
     * @throws Exception if any reflection operations fail.
     */
    private static void initSodiumHooks() throws Exception {
        Class<?> sodiumWorldRendererClass = Class.forName("me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer");
        Method getInstance = sodiumWorldRendererClass.getMethod("getInstance");
        Object sodiumWorldRenderer = getInstance.invoke(null);

        // Set up chunk build callback
        Class<?> chunkBuildCallbackClass = Class.forName("me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer$ChunkBuildCallback");
        Method setChunkBuildCallback = sodiumWorldRendererClass.getMethod("setChunkBuildCallback", chunkBuildCallbackClass);

        Object chunkBuildCallback = java.lang.reflect.Proxy.newProxyInstance(
                SodiumHooks.class.getClassLoader(),
                new Class<?>[] { chunkBuildCallbackClass },
                (proxy, method, args) -> {
                    if (method.getName().equals("shouldBuild")) {
                        Object renderSection = args[0];
                        Method getPosition = renderSection.getClass().getMethod("getPosition");
                        Object chunkSectionPos = getPosition.invoke(renderSection);
                        Method getX = chunkSectionPos.getClass().getMethod("getX");
                        Method getZ = chunkSectionPos.getClass().getMethod("getZ");
                        int x = (int) getX.invoke(chunkSectionPos);
                        int z = (int) getZ.invoke(chunkSectionPos);
                        return SodiumCompatibility.shouldRenderChunk(x, z);
                    }
                    return null;
                }
        );

        setChunkBuildCallback.invoke(sodiumWorldRenderer, chunkBuildCallback);

        // Set up chunk render list callback
        Class<?> chunkRenderListCallbackClass = Class.forName("me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer$ChunkRenderListCallback");
        Method setChunkRenderListCallback = sodiumWorldRendererClass.getMethod("setChunkRenderListCallback", chunkRenderListCallbackClass);

        Object chunkRenderListCallback = java.lang.reflect.Proxy.newProxyInstance(
                SodiumHooks.class.getClassLoader(),
                new Class<?>[] { chunkRenderListCallbackClass },
                (proxy, method, args) -> {
                    if (method.getName().equals("onRenderListBuild")) {
                        Object renderList = args[1];
                        Method removeIf = renderList.getClass().getMethod("removeIf", java.util.function.Predicate.class);
                        removeIf.invoke(renderList, (java.util.function.Predicate<?>) renderSection -> {
                            try {
                                Method getPosition = renderSection.getClass().getMethod("getPosition");
                                Object chunkSectionPos = getPosition.invoke(renderSection);
                                Method getX = chunkSectionPos.getClass().getMethod("getX");
                                Method getZ = chunkSectionPos.getClass().getMethod("getZ");
                                int x = (int) getX.invoke(chunkSectionPos);
                                int z = (int) getZ.invoke(chunkSectionPos);
                                return !SodiumCompatibility.shouldRenderChunk(x, z);
                            } catch (Exception e) {
                                Cobalt.LOGGER.error("Error in chunk render list callback", e);
                                return false;
                            }
                        });
                    }
                    return null;
                }
        );

        setChunkRenderListCallback.invoke(sodiumWorldRenderer, chunkRenderListCallback);
    }
}