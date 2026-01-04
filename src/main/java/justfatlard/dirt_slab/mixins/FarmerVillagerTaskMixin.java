package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.Shadow;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.FarmlandSlab;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.task.FarmerVillagerTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;

@Mixin(FarmerVillagerTask.class)
public class FarmerVillagerTaskMixin {
	@Shadow
	private BlockPos currentTarget;

	// Inject into isSuitableTarget to also accept FarmlandSlab
	@Inject(at = @At("RETURN"), method = "isSuitableTarget", cancellable = true)
	private void isSuitableTargetForFarmlandSlab(BlockPos pos, ServerWorld world, CallbackInfoReturnable<Boolean> cir) {
		// If already returning true, don't need to check
		if (cir.getReturnValue()) return;

		// Check if there's air above a FarmlandSlab
		BlockState blockState = world.getBlockState(pos);
		Block block2 = world.getBlockState(pos.down()).getBlock();

		if (blockState.isAir() && block2 instanceof FarmlandSlab) {
			cir.setReturnValue(true);
		}
	}

	// Inject into keepRunning to handle planting on FarmlandSlab
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;", ordinal = 0), method = "keepRunning", cancellable = true)
	private void keepRunningForFarmlandSlab(ServerWorld serverWorld, VillagerEntity villagerEntity, long l, CallbackInfo ci) {
		// This injection point is right before vanilla planting logic
		// We handle our own planting if above a FarmlandSlab
		if (this.currentTarget == null) return;

		BlockState blockState = serverWorld.getBlockState(this.currentTarget);
		Block block2 = serverWorld.getBlockState(this.currentTarget.down()).getBlock();

		// If it's air above FarmlandSlab and villager has seeds
		if (blockState.isAir() && block2 instanceof FarmlandSlab && villagerEntity.hasSeedToPlant()) {
			SimpleInventory simpleInventory = villagerEntity.getInventory();

			for (int i = 0; i < simpleInventory.size(); i++) {
				ItemStack itemStack = simpleInventory.getStack(i);
				boolean planted = false;

				if (!itemStack.isEmpty() && itemStack.isIn(ItemTags.VILLAGER_PLANTABLE_SEEDS) && itemStack.getItem() instanceof BlockItem blockItem) {
					// Get the slab crop variant if available
					BlockState cropState = getSlabCropState(blockItem);
					if (cropState != null) {
						serverWorld.setBlockState(this.currentTarget, cropState);
						serverWorld.emitGameEvent(GameEvent.BLOCK_PLACE, this.currentTarget, GameEvent.Emitter.of(villagerEntity, cropState));
						planted = true;
					}
				}

				if (planted) {
					serverWorld.playSound(
						null, this.currentTarget.getX(), this.currentTarget.getY(), this.currentTarget.getZ(),
						SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1.0F, 1.0F
					);
					itemStack.decrement(1);
					if (itemStack.isEmpty()) {
						simpleInventory.setStack(i, ItemStack.EMPTY);
					}
					// Cancel the rest of the method since we handled planting
					ci.cancel();
					return;
				}
			}
		}
	}

	private static BlockState getSlabCropState(BlockItem blockItem) {
		Block block = blockItem.getBlock();
		// Map vanilla crops to their slab variants
		if (block == net.minecraft.block.Blocks.WHEAT) {
			return DirtSlabBlocks.WHEAT_SLAB_CROP.getDefaultState();
		}
		if (block == net.minecraft.block.Blocks.CARROTS) {
			return DirtSlabBlocks.CARROT_SLAB_CROP.getDefaultState();
		}
		if (block == net.minecraft.block.Blocks.POTATOES) {
			return DirtSlabBlocks.POTATO_SLAB_CROP.getDefaultState();
		}
		if (block == net.minecraft.block.Blocks.BEETROOTS) {
			return DirtSlabBlocks.BEETROOT_SLAB_CROP.getDefaultState();
		}
		return null;
	}
}
