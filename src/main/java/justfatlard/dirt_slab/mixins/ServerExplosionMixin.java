package justfatlard.dirt_slab.mixins;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import justfatlard.dirt_slab.ExplosionRubble;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ServerExplosion.class)
public class ServerExplosionMixin {
	@Unique
	private Set<BlockPos> destroyedPositions = Set.of();

	// The full destroyed set is needed up front: during iteration, later entries are not yet removed from the world
	@Inject(method = "interactWithBlocks", at = @At("HEAD"))
	private void captureDestroyedPositions(List<BlockPos> positions, CallbackInfo ci){
		this.destroyedPositions = new HashSet<>(positions);
	}

	// Wraps the per-position destruction call; positions reaching here already passed resistance and griefing checks
	@WrapOperation(method = "interactWithBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onExplosionHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;Ljava/util/function/BiConsumer;)V"))
	private void splitIntoSlabs(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer, Operation<Void> original){
		if(ExplosionRubble.trySplit(state, level, pos, explosion, this.destroyedPositions)) return;

		original.call(state, level, pos, explosion, dropConsumer);
	}
}
