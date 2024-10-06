package net.kubik.cobaltmod.compat.sodium;

import net.fabricmc.loader.api.FabricLoader;
import net.kubik.cobaltmod.Cobalt;
import net.kubik.cobaltmod.util.ChunkCalculator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;

import java.util.BitSet;

/**
 * Handles compatibility with Sodium.
 */
public class SodiumCompatibility {
    /** Indicates whether Sodium is loaded. */
    private static final boolean isSodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");

    /** The chunk calculator instance. */
    private static ChunkCalculator chunkCalculator;

    /**
     * Initializes the Sodium compatibility layer.
     * @param calculator The chunk calculator instance.
     */
    public static void init(ChunkCalculator calculator) {
        chunkCalculator = calculator;
        if (isSodiumLoaded) {
            initSodiumHooks();
        }
    }

    /**
     * Initializes the Sodium hooks if Sodium is loaded.
     */
    private static void initSodiumHooks() {
        try {
            Class.forName("net.kubik.cobaltmod.compat.sodium.SodiumHooks").getMethod("init", ChunkCalculator.class).invoke(null, chunkCalculator);
        } catch (Exception e) {
            Cobalt.LOGGER.error("Failed to initialize Sodium hooks: " + e.getMessage());
        }
    }

    /**
     * Determines if a chunk should be rendered based on Cobalt's calculations.
     * @param chunkX The X coordinate of the chunk.
     * @param chunkZ The Z coordinate of the chunk.
     * @return true if the chunk should be rendered, false otherwise.
     */
    public static boolean shouldRenderChunk(int chunkX, int chunkZ) {
        if (chunkCalculator == null) return true;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return true;

        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        BitSet chunksToRender = chunkCalculator.getChunksToRender();
        int renderDistanceChunks = client.options.getViewDistance().getValue();

        int minChunkX = client.player.getChunkPos().x - renderDistanceChunks;
        int minChunkZ = client.player.getChunkPos().z - renderDistanceChunks;
        int size = renderDistanceChunks * 2 + 1;

        int localX = chunkPos.x - minChunkX;
        int localZ = chunkPos.z - minChunkZ;

        if (localX < 0 || localX >= size || localZ < 0 || localZ >= size) {
            return false;
        }

        int index = localX + localZ * size;
        return chunksToRender.get(index);
    }
}