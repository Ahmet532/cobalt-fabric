package net.kubik.cobaltmod.mixin;

import net.kubik.cobaltmod.Cobalt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;

@Mixin(BuiltChunk.class)
public class ChunkRenderMixin {

	@Inject(method = "shouldBuild", at = @At("HEAD"), cancellable = true)
	private void onShouldBuild(CallbackInfoReturnable<Boolean> cir) {
		BuiltChunk thisChunk = (BuiltChunk) (Object) this;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null) {
			return;
		}

		BlockPos chunkOrigin = thisChunk.getOrigin();
		ChunkPos chunkPos = new ChunkPos(chunkOrigin);

		BitSet chunksToRenderBitSet = Cobalt.chunksToRender.get();

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