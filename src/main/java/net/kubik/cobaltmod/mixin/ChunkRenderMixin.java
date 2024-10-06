package net.kubik.cobaltmod.mixin;

import net.kubik.cobaltmod.Cobalt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;

/**
 * Mixin class for BuiltChunk to modify chunk rendering behavior.
 */
@Mixin(BuiltChunk.class)
public class ChunkRenderMixin {

	/**
	 * Injects into the shouldBuild method to determine if a chunk should be built based on Cobalt's calculations.
	 * @param cir Callback info returnable.
	 */
	@Inject(method = "shouldBuild", at = @At("HEAD"), cancellable = true)
	private void onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
		BuiltChunk thisChunk = (BuiltChunk) (Object) this;
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.world == null || client.player == null) {
			return;
		}

		ChunkPos chunkPos = new ChunkPos(thisChunk.getOrigin());

		BitSet chunksToRenderBitSet = Cobalt.getChunksToRender();

		int renderDistanceChunks = client.options.getViewDistance().getValue();

		int minChunkX = client.player.getChunkPos().x - renderDistanceChunks;
		int minChunkZ = client.player.getChunkPos().z - renderDistanceChunks;
		int size = renderDistanceChunks * 2 + 1;

		int localX = chunkPos.x - minChunkX;
		int localZ = chunkPos.z - minChunkZ;

		if (localX < 0 || localX >= size || localZ < 0 || localZ >= size) {
			cir.setReturnValue(false);
			return;
		}

		int index = localX + localZ * size;
		cir.setReturnValue(chunksToRenderBitSet.get(index));
	}
}