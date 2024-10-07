package net.kubik.cobaltmod.compat.sodium;

import net.kubik.cobaltmod.Cobalt;
import net.kubik.cobaltmod.util.ChunkCalculator;

import java.lang.reflect.Method;

public class SodiumHooks {
    private static ChunkCalculator chunkCalculator;

    public static void init(ChunkCalculator calculator) {
        chunkCalculator = calculator;
        try {
            initSodiumHooks();
        } catch (Exception e) {
            Cobalt.LOGGER.error("Failed to initialize Sodium hooks", e);
        }
    }

    private static void initSodiumHooks() throws Exception {
        Class<?> sodiumWorldRendererClass = Class.forName("net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer");
        Method instanceMethod = sodiumWorldRendererClass.getMethod("instance");
        Object sodiumWorldRenderer = instanceMethod.invoke(null);

        // Set up chunk build callback
        Class<?> renderSectionManagerClass = Class.forName("net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager");
        Method getRenderSectionManagerMethod = sodiumWorldRendererClass.getMethod("getRenderSectionManager");
        Object renderSectionManager = getRenderSectionManagerMethod.invoke(sodiumWorldRenderer);

        Class<?> chunkBuildCallbackClass = Class.forName("net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager$ChunkBuildCallback");
        Method addChunkBuildCallbackMethod = renderSectionManagerClass.getMethod("addChunkBuildCallback", chunkBuildCallbackClass);

        Object chunkBuildCallback = java.lang.reflect.Proxy.newProxyInstance(
                SodiumHooks.class.getClassLoader(),
                new Class<?>[] { chunkBuildCallbackClass },
                (proxy, method, args) -> {
                    if (method.getName().equals("shouldBuild")) {
                        Object renderSection = args[0];
                        Method getPositionMethod = renderSection.getClass().getMethod("getPosition");
                        Object sectionPos = getPositionMethod.invoke(renderSection);
                        Method getXMethod = sectionPos.getClass().getMethod("getX");
                        Method getZMethod = sectionPos.getClass().getMethod("getZ");
                        int x = (int) getXMethod.invoke(sectionPos);
                        int z = (int) getZMethod.invoke(sectionPos);
                        return SodiumCompatibility.shouldRenderChunk(x, z);
                    }
                    return null;
                }
        );

        addChunkBuildCallbackMethod.invoke(renderSectionManager, chunkBuildCallback);

        // Set up chunk render list callback
        Class<?> chunkRenderListCallbackClass = Class.forName("net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager$ChunkRenderListCallback");
        Method addChunkRenderListCallbackMethod = renderSectionManagerClass.getMethod("addChunkRenderListCallback", chunkRenderListCallbackClass);

        Object chunkRenderListCallback = java.lang.reflect.Proxy.newProxyInstance(
                SodiumHooks.class.getClassLoader(),
                new Class<?>[] { chunkRenderListCallbackClass },
                (proxy, method, args) -> {
                    if (method.getName().equals("onRenderListBuild")) {
                        Object renderLists = args[1];
                        Method removeIfMethod = renderLists.getClass().getMethod("removeIf", java.util.function.Predicate.class);
                        removeIfMethod.invoke(renderLists, (java.util.function.Predicate<?>) renderList -> {
                            try {
                                Method getRegionMethod = renderList.getClass().getMethod("getRegion");
                                Object renderRegion = getRegionMethod.invoke(renderList);
                                Method sectionsWithEntitiesIteratorMethod = renderList.getClass().getMethod("sectionsWithEntitiesIterator");
                                Object iterator = sectionsWithEntitiesIteratorMethod.invoke(renderList);

                                if (iterator == null) {
                                    return false;
                                }

                                Method hasNextMethod = iterator.getClass().getMethod("hasNext");
                                Method nextByteAsIntMethod = iterator.getClass().getMethod("nextByteAsInt");
                                Method getSectionMethod = renderRegion.getClass().getMethod("getSection", int.class);

                                while ((boolean) hasNextMethod.invoke(iterator)) {
                                    int sectionId = (int) nextByteAsIntMethod.invoke(iterator);
                                    Object section = getSectionMethod.invoke(renderRegion, sectionId);
                                    Method getPositionMethod = section.getClass().getMethod("getPosition");
                                    Object sectionPos = getPositionMethod.invoke(section);
                                    Method getXMethod = sectionPos.getClass().getMethod("getX");
                                    Method getZMethod = sectionPos.getClass().getMethod("getZ");
                                    int x = (int) getXMethod.invoke(sectionPos);
                                    int z = (int) getZMethod.invoke(sectionPos);

                                    if (!SodiumCompatibility.shouldRenderChunk(x, z)) {
                                        return true;
                                    }
                                }

                                return false;
                            } catch (Exception e) {
                                Cobalt.LOGGER.error("Error in chunk render list callback", e);
                                return false;
                            }
                        });
                    }
                    return null;
                }
        );

        addChunkRenderListCallbackMethod.invoke(renderSectionManager, chunkRenderListCallback);
    }
}