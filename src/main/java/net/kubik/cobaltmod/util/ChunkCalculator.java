package net.kubik.cobaltmod.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles the calculation of which chunks should be rendered based on the player's position and view.
 */
public class ChunkCalculator {
    /** The radius of chunks to always render around the player. */
    private static final int CHUNK_BUFFER_RADIUS = 2;

    /** Atomic reference to the BitSet representing which chunks should be rendered. */
    private static final AtomicReference<BitSet> chunksToRender = new AtomicReference<>(new BitSet());

    /** The last recorded player position. */
    private Vec3d lastPlayerPos = Vec3d.ZERO;

    /** The last recorded player yaw. */
    private float lastPlayerYaw = 0f;

    /** The last recorded player pitch. */
    private float lastPlayerPitch = 0f;

    /** The squared distance threshold for recalculation. */
    private static final double POSITION_THRESHOLD_SQUARED = 1.0 * 1.0;

    /** The rotation threshold for recalculation. */
    private static final float ROTATION_THRESHOLD = 5.0f;

    /**
     * Determines if the chunks to render should be recalculated based on player movement.
     * @param playerPos The current player position.
     * @param yaw The current player yaw.
     * @param pitch The current player pitch.
     * @return true if recalculation is needed, false otherwise.
     */
    public boolean shouldRecalculate(Vec3d playerPos, float yaw, float pitch) {
        if (playerPos.squaredDistanceTo(lastPlayerPos) < POSITION_THRESHOLD_SQUARED &&
                Math.abs(yaw - lastPlayerYaw) < ROTATION_THRESHOLD &&
                Math.abs(pitch - lastPlayerPitch) < ROTATION_THRESHOLD) {
            return false;
        }

        lastPlayerPos = playerPos;
        lastPlayerYaw = yaw;
        lastPlayerPitch = pitch;
        return true;
    }

    /**
     * Calculates which chunks should be rendered based on the player's position and view.
     * @param client The Minecraft client instance.
     * @param playerPos The player's current position.
     * @param yaw The player's current yaw.
     * @param pitch The player's current pitch.
     */
    public void calculateChunksToRender(MinecraftClient client, Vec3d playerPos, float yaw, float pitch) {
        BitSet newChunksToRender = new BitSet();

        int renderDistanceChunks = client.options.getViewDistance().getValue();

        Vec3d lookVec = client.player.getRotationVec(1.0F).normalize();
        double maxAngleCos = 0.0;  // 90 derecenin kosinüsü 0'dır
    

        ChunkPos playerChunkPos = client.player.getChunkPos();
        int playerChunkX = playerChunkPos.x;
        int playerChunkZ = playerChunkPos.z;
        int minChunkX = playerChunkX - renderDistanceChunks;
        int maxChunkX = playerChunkX + renderDistanceChunks;
        int minChunkZ = playerChunkZ - renderDistanceChunks;
        int maxChunkZ = playerChunkZ + renderDistanceChunks;

        int size = renderDistanceChunks * 2 + 1;

        double playerX = playerPos.x;
        double playerZ = playerPos.z;

        // Calculate visible chunks
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            double chunkCenterX = (chunkX << 4) + 8;
            double deltaX = chunkCenterX - playerX;
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                double chunkCenterZ = (chunkZ << 4) + 8;
                double deltaZ = chunkCenterZ - playerZ;

                double distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
                if (distanceSquared == 0) continue;

                double invDistance = 1.0 / Math.sqrt(distanceSquared);
                double toChunkX = deltaX * invDistance;
                double toChunkZ = deltaZ * invDistance;

                double dotProduct = lookVec.x * toChunkX + lookVec.z * toChunkZ;

                if (dotProduct >= maxAngleCos) {
                    int localX = chunkX - minChunkX;
                    int localZ = chunkZ - minChunkZ;
                    int index = localX + localZ * size;
                    newChunksToRender.set(index);
                }
            }
        }

        // Add buffer chunks around the player
        for (int dx = -CHUNK_BUFFER_RADIUS; dx <= CHUNK_BUFFER_RADIUS; dx++) {
            int chunkX = playerChunkX + dx;
            for (int dz = -CHUNK_BUFFER_RADIUS; dz <= CHUNK_BUFFER_RADIUS; dz++) {
                int chunkZ = playerChunkZ + dz;
                int localX = chunkX - minChunkX;
                int localZ = chunkZ - minChunkZ;
                if (localX >= 0 && localX < size && localZ >= 0 && localZ < size) {
                    int index = localX + localZ * size;
                    newChunksToRender.set(index);
                }
            }
        }

        chunksToRender.set(newChunksToRender);
    }

    /**
     * Gets the current BitSet representing which chunks should be rendered.
     * @return A BitSet where set bits indicate chunks that should be rendered.
     */
    public static BitSet getChunksToRender() {
        return chunksToRender.get();
    }
}
