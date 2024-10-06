package net.kubik.cobaltmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.kubik.cobaltmod.util.ChunkCalculator;
import net.kubik.cobaltmod.compat.sodium.SodiumCompatibility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main class for Cobalt.
 * This class initializes the mod and sets up the chunk calculation system.
 */
public class Cobalt implements ModInitializer {

	/** The mod ID for Cobalt. */
	public static final String MOD_ID = "cobaltmod";

	/** The logger for Cobalt. */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** The chunk calculator instance. */
	private final ChunkCalculator chunkCalculator = new ChunkCalculator();

	/**
	 * The executor service for running chunk calculations asynchronously.
	 * Uses a single thread to avoid overwhelming the system.
	 */
	private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "Cobalt-ChunkCalculator");
		t.setDaemon(true);
		return t;
	});

	/**
	 * Initializes Cobalt.
	 * This method is called by the Fabric loader when the game starts.
	 */
	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Cobalt!");

		// Initialize Sodium compatibility
		SodiumCompatibility.init(chunkCalculator);

		// Register the world render event
		WorldRenderEvents.AFTER_SETUP.register(context -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.world == null) return;

			Vec3d playerPos = client.player.getPos();
			float yaw = client.player.getYaw();
			float pitch = client.player.getPitch();

			if (chunkCalculator.shouldRecalculate(playerPos, yaw, pitch)) {
				if (!executorService.isShutdown()) {
					executorService.submit(() -> chunkCalculator.calculateChunksToRender(client, playerPos, yaw, pitch));
				}
			}
		});

		// Set up shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			executorService.shutdownNow();
			LOGGER.info("Cobalt executor service shut down.");
		}));
	}

	/**
	 * Gets the BitSet representing which chunks should be rendered.
	 * @return A BitSet where set bits indicate chunks that should be rendered.
	 */
	public static BitSet getChunksToRender() {
		return ChunkCalculator.getChunksToRender();
	}
}