package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabEffects;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlicedTopSlab;

@Mixin(HoeItem.class)
public class HoeMixin {
	@Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
	private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info){
		BlockPos pos = context.getBlockPos();
		World world = context.getWorld();
		BlockState state = world.getBlockState(pos);

		if(context.getSide() != Direction.DOWN && SlicedTopSlab.canExistAt(state, world, pos)){
			PlayerEntity player = context.getPlayer();
			Block block = state.getBlock();
			boolean success = false;
			BlockState newState = null;

			if(block == DirtSlabBlocks.COARSE_DIRT_SLAB){
				newState = SlabRegistry.copySlabProperties(state, DirtSlabBlocks.DIRT_SLAB);

				success = true;

				if(world.isClient()) world.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}

			else if(block == DirtSlabBlocks.DIRT_SLAB || block == DirtSlabBlocks.GRASS_SLAB || block == DirtSlabBlocks.GRASS_PATH_SLAB){
				newState = SlabRegistry.copySlabProperties(state, DirtSlabBlocks.FARMLAND_SLAB);

				success = true;

				if(world.isClient()) world.playSound(player, pos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}

			if(success){
				if(!world.isClient()){
					world.setBlockState(pos, newState);

					if(player != null) context.getStack().damage(1, player, context.getHand());
				}

				SlabEffects.dirtParticles(world, pos, 1);

				info.setReturnValue(ActionResult.SUCCESS);
			}
		}
	}
}
