package net.kubik.cobaltmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main class for Cobalt, responsible for initializing the mod and managing chunk rendering.
 */
public class Cobalt implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("cobaltmod");
	public static final String MOD_ID = "cobaltmod";

	public static final int CHUNK_BUFFER_RADIUS = 2;

	/**
	 * A volatile BitSet to track which chunks should be rendered.
	 */
	public static volatile BitSet chunksToRender = new BitSet();

	private Vec3d lastPlayerPos = Vec3d.ZERO;
	private float lastPlayerYaw = 0f;
	private float lastPlayerPitch = 0f;

	private static final double POSITION_THRESHOLD_SQUARED = 1.0 * 1.0;
	private static final float ROTATION_THRESHOLD = 5.0f;

	/**
	 * ExecutorService to handle chunk calculations in a separate thread.
	 */
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	/**
	 * Initializes Cobalt by registering the world render event and setting up necessary resources.
	 */
	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Cobalt!");

		WorldRenderEvents.AFTER_SETUP.register(context -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.world == null) return;

			Vec3d playerPos = client.player.getPos();
			float yaw = client.player.getYaw();
			float pitch = client.player.getPitch();

			if (playerPos.squaredDistanceTo(lastPlayerPos) < POSITION_THRESHOLD_SQUARED &&
					Math.abs(yaw - lastPlayerYaw) < ROTATION_THRESHOLD &&
					Math.abs(pitch - lastPlayerPitch) < ROTATION_THRESHOLD) {
				return;
			}

			lastPlayerPos = playerPos;
			lastPlayerYaw = yaw;
			lastPlayerPitch = pitch;

			executorService.submit(() -> calculateChunksToRender(client, playerPos, yaw, pitch));
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			executorService.shutdownNow();
			LOGGER.info("Cobalt executor service shut down.");
		}));
	}

	/**
	 * Calculates which chunks should be rendered based on the player's position and view direction.
	 *
	 * @param client    The Minecraft client instance.
	 * @param playerPos The player's current position.
	 * @param yaw       The player's current yaw.
	 * @param pitch     The player's current pitch.
	 */
	private void calculateChunksToRender(MinecraftClient client, Vec3d playerPos, float yaw, float pitch) {
		BitSet chunksToRenderLocal = new BitSet();

		int renderDistanceChunks = client.options.getViewDistance().getValue();

		Vec3d lookVec = client.player.getRotationVec(1.0F).normalize();

		double maxAngleCos = Math.cos(Math.toRadians(90.0));

		ChunkPos playerChunkPos = client.player.getChunkPos();
		int playerChunkX = playerChunkPos.x;
		int playerChunkZ = playerChunkPos.z;
		int minChunkX = playerChunkX - renderDistanceChunks;
		int maxChunkX = playerChunkX + renderDistanceChunks;
		int minChunkZ = playerChunkZ - renderDistanceChunks;
		int maxChunkZ = playerChunkZ + renderDistanceChunks;

		int size = renderDistanceChunks * 2 + 1;

		double playerY = playerPos.y;

		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			double chunkCenterX = (chunkX << 4) + 8;
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				double chunkCenterZ = (chunkZ << 4) + 8;
				Vec3d chunkCenter = new Vec3d(chunkCenterX, playerY, chunkCenterZ);

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
			int chunkX = playerChunkX + dx;
			for (int dz = -CHUNK_BUFFER_RADIUS; dz <= CHUNK_BUFFER_RADIUS; dz++) {
				int chunkZ = playerChunkZ + dz;
				int localX = chunkX - minChunkX;
				int localZ = chunkZ - minChunkZ;
				int index = localX + localZ * size;
				chunksToRenderLocal.set(index);
			}
		}

		chunksToRender = chunksToRenderLocal;
	}
}