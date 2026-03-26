package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

@Mixin(EatGrassGoal.class)
public class EatGrassGoalMixin {
	@Shadow
	private World world;
	@Shadow
	private MobEntity mob;

	private BlockPos getGrassSlabPos() {
		BlockPos mobPos = this.mob.getBlockPos();
		boolean betweenBlocks = (Math.round(mob.getY()) - mob.getY()) > 0;
		BlockPos checkPos = betweenBlocks ? mobPos : mobPos.down();
		if(world.getBlockState(checkPos).getBlock() == DirtSlabBlocks.GRASS_SLAB) return checkPos;
		return null;
	}

	@Inject(at = @At(value = "INVOKE", target = "java/util/function/Predicate.test(Ljava/lang/Object;)Z"), method = "canStart", cancellable = true)
	public void canStart(CallbackInfoReturnable<Boolean> info){
		if(getGrassSlabPos() != null) info.setReturnValue(true);
	}

	@Inject(at = @At(value = "INVOKE", target = "java/util/function/Predicate.test(Ljava/lang/Object;)Z"), method = "tick", cancellable = true)
	public void tick(CallbackInfo info){
		BlockPos posDown = getGrassSlabPos();
		if(posDown == null) return;

		BlockState state = world.getBlockState(posDown);

		if(this.world instanceof ServerWorld serverWorld && serverWorld.getGameRules().getValue(GameRules.DO_MOB_GRIEFING)){
			this.world.syncWorldEvent(2001, posDown, Block.getRawIdFromState(Blocks.GRASS_BLOCK.getDefaultState()));

			this.world.setBlockState(posDown, SlabRegistry.copySlabProperties(state, DirtSlabBlocks.DIRT_SLAB), 2);
		}

		this.mob.onEatingGrass();
		info.cancel();
	}
}
