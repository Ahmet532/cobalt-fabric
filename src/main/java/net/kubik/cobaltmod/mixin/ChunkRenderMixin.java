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
 * Mixin class that modifies the chunk rendering behavior to optimize performance.
 */
@Mixin(BuiltChunk.class)
public class ChunkRenderMixin {

	/**
	 * Determines whether a chunk should be built based on the precomputed chunks to render.
	 *
	 * @param cir The callback information for the method.
	 */
	@Inject(method = "shouldBuild", at = @At("HEAD"), cancellable = true)
	private void onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
		BuiltChunk thisChunk = (BuiltChunk) (Object) this;
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.world == null || client.player == null) {
			return;
		}

		ChunkPos chunkPos = new ChunkPos(thisChunk.getOrigin());

		BitSet chunksToRenderBitSet = Cobalt.chunksToRender;

		int renderDistanceChunks = client.options.getViewDistance().getValue();

		int minChunkX = client.player.getChunkPos().x - renderDistanceChunks;
		int minChunkZ = client.player.getChunkPos().z - renderDistanceChunks;
		int size = renderDistanceChunks * 2 + 1;

		int localX = chunkPos.x - minChunkX;
		int localZ = chunkPos.z - minChunkZ;

		if (localX >= 0 && localX < size && localZ >= 0 && localZ < size) {
			int index = localX + localZ * size;
			if (chunksToRenderBitSet.get(index)) {
				cir.setReturnValue(true);
				return;
			}
		}

		cir.setReturnValue(false);
	}
}