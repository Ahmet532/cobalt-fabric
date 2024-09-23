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

import java.util.Set;

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

		Set<ChunkPos> chunksToRender = Cobalt.chunksToRender.get();
		if (chunksToRender.contains(chunkPos)) {
			cir.setReturnValue(true);
		} else {
			cir.setReturnValue(false);
		}
	}
}