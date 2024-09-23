package net.kubik.cobaltmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Cobalt implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("cobaltmod");
	public static final String MOD_ID = "cobaltmod";
	public static final int NUM_LINES = 50;
	public static final float LINE_CURVE_DEGREES = 90.0f;
	public static final int LINE_SEGMENTS = 50;
	public static final int CHUNK_BUFFER_RADIUS = 2;

	public static final AtomicReference<Set<ChunkPos>> chunksToRender = new AtomicReference<>(new HashSet<>());

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Cobalt Mod");

		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null) return;

			float yaw = client.player.getYaw();
			float pitch = client.player.getPitch();
			Vec3d playerPos = client.player.getPos();

			int renderDistance = client.options.getViewDistance().getValue() * 16;

			Set<ChunkPos> intersectedChunks = new HashSet<>();

			for (int i = 0; i < NUM_LINES; i++) {
				float lineYaw = yaw + (i - NUM_LINES / 2f) * (LINE_CURVE_DEGREES / NUM_LINES);
				intersectedChunks.addAll(getIntersectedChunks(playerPos, lineYaw, pitch, renderDistance));
			}

			Set<ChunkPos> chunksToRenderLocal = new HashSet<>(intersectedChunks);
			for (ChunkPos intersectedChunk : intersectedChunks) {
				chunksToRenderLocal.addAll(getNeighboringChunks(intersectedChunk));
			}

			ChunkPos playerChunkPos = new ChunkPos(client.player.getBlockPos());
			for (int dx = -CHUNK_BUFFER_RADIUS; dx <= CHUNK_BUFFER_RADIUS; dx++) {
				for (int dz = -CHUNK_BUFFER_RADIUS; dz <= CHUNK_BUFFER_RADIUS; dz++) {
					chunksToRenderLocal.add(new ChunkPos(playerChunkPos.x + dx, playerChunkPos.z + dz));
				}
			}

			chunksToRender.set(chunksToRenderLocal);
		});
	}

	private Set<ChunkPos> getIntersectedChunks(Vec3d start, float yaw, float pitch, int length) {
		Set<ChunkPos> intersectedChunks = new HashSet<>();
		float yawRad = (float) Math.toRadians(yaw);
		float pitchRad = (float) Math.toRadians(pitch);

		for (int i = 0; i <= LINE_SEGMENTS; i++) {
			float t = (float) i / LINE_SEGMENTS;
			Vec3d point = getPointOnCurve(start, yawRad, pitchRad, t * length);
			intersectedChunks.add(new ChunkPos(new BlockPos((int) point.x, (int) point.y, (int) point.z)));
		}

		return intersectedChunks;
	}

	private Set<ChunkPos> getNeighboringChunks(ChunkPos chunkPos) {
		Set<ChunkPos> neighbors = new HashSet<>();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				neighbors.add(new ChunkPos(chunkPos.x + dx, chunkPos.z + dz));
			}
		}
		return neighbors;
	}

	public static Vec3d getPointOnCurve(Vec3d start, float yawRad, float pitchRad, float distance) {
		float curveFactor = distance * (float) Math.toRadians(LINE_CURVE_DEGREES) / 100f;

		float x = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad);
		float y = -MathHelper.sin(pitchRad);
		float z = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad);

		float curvedYaw = yawRad + curveFactor;

		x = -MathHelper.sin(curvedYaw) * MathHelper.cos(pitchRad);
		z = MathHelper.cos(curvedYaw) * MathHelper.cos(pitchRad);

		return start.add(new Vec3d(x, y, z).multiply(distance));
	}
}