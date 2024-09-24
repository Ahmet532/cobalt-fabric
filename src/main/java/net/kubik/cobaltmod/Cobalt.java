package net.kubik.cobaltmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

public class Cobalt implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("cobaltmod");
	public static final String MOD_ID = "cobaltmod";

	public static final int CHUNK_BUFFER_RADIUS = 2;

	public static final AtomicReference<BitSet> chunksToRender = new AtomicReference<>(new BitSet());

	private Vec3d lastPlayerPos = Vec3d.ZERO;
	private float lastPlayerYaw = 0f;
	private float lastPlayerPitch = 0f;

	private static final double POSITION_THRESHOLD = 1.0;
	private static final float ROTATION_THRESHOLD = 5.0f;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Cobalt Mod");

		WorldRenderEvents.AFTER_SETUP.register(context -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.world == null) return;

			Vec3d playerPos = client.player.getPos();
			float yaw = client.player.getYaw();
			float pitch = client.player.getPitch();

			if (playerPos.squaredDistanceTo(lastPlayerPos) < POSITION_THRESHOLD * POSITION_THRESHOLD &&
					Math.abs(yaw - lastPlayerYaw) < ROTATION_THRESHOLD &&
					Math.abs(pitch - lastPlayerPitch) < ROTATION_THRESHOLD) {
				return;
			}

			lastPlayerPos = playerPos;
			lastPlayerYaw = yaw;
			lastPlayerPitch = pitch;

			BitSet chunksToRenderLocal = new BitSet();

			int renderDistanceChunks = client.options.getViewDistance().getValue();

			Vec3d lookVec = client.player.getRotationVec(1.0F).normalize();

			double maxAngleCos = Math.cos(Math.toRadians(90.0));

			int playerChunkX = client.player.getChunkPos().x;
			int playerChunkZ = client.player.getChunkPos().z;
			int minChunkX = playerChunkX - renderDistanceChunks;
			int maxChunkX = playerChunkX + renderDistanceChunks;
			int minChunkZ = playerChunkZ - renderDistanceChunks;
			int maxChunkZ = playerChunkZ + renderDistanceChunks;

			int size = renderDistanceChunks * 2 + 1;

			for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
				for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
					double chunkCenterX = (chunkX << 4) + 8;
					double chunkCenterZ = (chunkZ << 4) + 8;
					Vec3d chunkCenter = new Vec3d(chunkCenterX, playerPos.y, chunkCenterZ);

					Vec3d toChunkVec = chunkCenter.subtract(playerPos).normalize();

					double dotProduct = lookVec.dotProduct(toChunkVec);

					if (dotProduct >= maxAngleCos) {
						int localX = chunkX - minChunkX;
						int localZ = chunkZ - minChunkZ;
						int index = localX + localZ * size;
						chunksToRenderLocal.set(index);
					}
				}
			}

			for (int dx = -CHUNK_BUFFER_RADIUS; dx <= CHUNK_BUFFER_RADIUS; dx++) {
				for (int dz = -CHUNK_BUFFER_RADIUS; dz <= CHUNK_BUFFER_RADIUS; dz++) {
					int chunkX = playerChunkX + dx;
					int chunkZ = playerChunkZ + dz;
					int localX = chunkX - minChunkX;
					int localZ = chunkZ - minChunkZ;
					int index = localX + localZ * size;
					chunksToRenderLocal.set(index);
				}
			}

			chunksToRender.set(chunksToRenderLocal);
		});
	}
}