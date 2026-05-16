package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

@Mixin(EatBlockGoal.class)
public class EatGrassGoalMixin {
	@Shadow
	private Level level;
	@Shadow
	private Mob mob;

	private BlockPos getGrassSlabPos() {
		BlockPos mobPos = this.mob.blockPosition();
		boolean betweenBlocks = (Math.round(mob.getY()) - mob.getY()) > 0;
		BlockPos checkPos = betweenBlocks ? mobPos : mobPos.below();
		if(level.getBlockState(checkPos).getBlock() == DirtSlabBlocks.GRASS_SLAB) return checkPos;
		return null;
	}

	@Inject(at = @At(value = "INVOKE", target = "java/util/function/Predicate.test(Ljava/lang/Object;)Z"), method = "canUse", cancellable = true)
	public void canStart(CallbackInfoReturnable<Boolean> info){
		if(getGrassSlabPos() != null) info.setReturnValue(true);
	}

	@Inject(at = @At(value = "INVOKE", target = "java/util/function/Predicate.test(Ljava/lang/Object;)Z"), method = "tick", cancellable = true)
	public void tick(CallbackInfo info){
		BlockPos posDown = getGrassSlabPos();
		if(posDown == null) return;

		BlockState state = level.getBlockState(posDown);

		if(this.level instanceof ServerLevel serverWorld && serverWorld.getGameRules().get(GameRules.MOB_GRIEFING)){
			this.level.levelEvent(2001, posDown, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));

			this.level.setBlock(posDown, SlabRegistry.copySlabProperties(state, DirtSlabBlocks.DIRT_SLAB), 2);
		}

		this.mob.ate();
		info.cancel();
	}
}
