package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import justfatlard.dirt_slab.DirtSlabBlocks;
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

	@Inject(at = @At(value = "INVOKE", target = "java/util/function/Predicate.test(Ljava/lang/Object;)Z"), method = "canStart", cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	public void canStart(CallbackInfoReturnable<Boolean> info, BlockPos pos){
		boolean betweenBlocks = (Math.round(mob.getY()) - mob.getY()) > 0;
		BlockPos posDown = betweenBlocks ? pos : pos.down();
		Block block = world.getBlockState(posDown).getBlock();

		if(block == DirtSlabBlocks.GRASS_SLAB) info.setReturnValue(true);
	}

	@Inject(at = @At(value = "INVOKE", target = "java/util/function/Predicate.test(Ljava/lang/Object;)Z"), method = "tick", cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	public void tick(CallbackInfo info, BlockPos pos){
		boolean betweenBlocks = (Math.round(mob.getY()) - mob.getY()) > 0;
		BlockPos posDown = betweenBlocks ? pos : pos.down();
		BlockState state = world.getBlockState(posDown);
		Block block = state.getBlock();

		if(block == DirtSlabBlocks.GRASS_SLAB){
			if(this.world instanceof ServerWorld serverWorld && serverWorld.getGameRules().getValue(GameRules.DO_MOB_GRIEFING)){
				this.world.syncWorldEvent(2001, posDown, Block.getRawIdFromState(Blocks.GRASS_BLOCK.getDefaultState()));

				this.world.setBlockState(posDown, DirtSlabBlocks.DIRT_SLAB.getDefaultState().with(SlabBlock.TYPE, state.get(SlabBlock.TYPE)).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED)), 2);
			}

			this.mob.onEatingGrass();
		}
	}
}
