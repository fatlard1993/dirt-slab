package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabEffects;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlicedTopSlab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(HoeItem.class)
public class HoeMixin {
	@Inject(at = @At("HEAD"), method = "useOn", cancellable = true)
	private void useOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> info){
		BlockPos pos = context.getClickedPos();
		Level world = context.getLevel();
		BlockState state = world.getBlockState(pos);

		if(context.getClickedFace() != Direction.DOWN && SlicedTopSlab.canExistAt(state, world, pos)){
			Player player = context.getPlayer();
			Block block = state.getBlock();
			boolean success = false;
			BlockState newState = null;

			if(block == DirtSlabBlocks.COARSE_DIRT_SLAB){
				newState = SlabRegistry.copySlabProperties(state, DirtSlabBlocks.DIRT_SLAB);

				success = true;

				if(world.isClientSide()) world.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			}

			else if(block == DirtSlabBlocks.DIRT_SLAB || block == DirtSlabBlocks.GRASS_SLAB || block == DirtSlabBlocks.GRASS_PATH_SLAB){
				newState = SlabRegistry.copySlabProperties(state, DirtSlabBlocks.FARMLAND_SLAB);

				success = true;

				if(world.isClientSide()) world.playSound(player, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
			}

			if(success){
				if(!world.isClientSide()){
					world.setBlockAndUpdate(pos, newState);

					if(player != null) context.getItemInHand().hurtAndBreak(1, player, context.getHand());
				}

				SlabEffects.dirtParticles(world, pos, 1);

				info.setReturnValue(InteractionResult.SUCCESS);
			}
		}
	}
}
