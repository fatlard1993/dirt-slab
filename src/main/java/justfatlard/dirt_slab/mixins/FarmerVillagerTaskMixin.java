package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.Shadow;

import justfatlard.dirt_slab.FarmlandSlab;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

@Mixin(HarvestFarmland.class)
public class FarmerVillagerTaskMixin {
	@Shadow
	private BlockPos aboveFarmlandPos;

	// Inject into isSuitableTarget to also accept FarmlandSlab
	@Inject(at = @At("RETURN"), method = "validPos", cancellable = true)
	private void isSuitableTargetForFarmlandSlab(BlockPos pos, ServerLevel world, CallbackInfoReturnable<Boolean> cir) {
		// If already returning true, don't need to check
		if (cir.getReturnValue()) return;

		// Check if there's air above a FarmlandSlab
		BlockState blockState = world.getBlockState(pos);
		Block block2 = world.getBlockState(pos.below()).getBlock();

		if (blockState.isAir() && block2 instanceof FarmlandSlab) {
			cir.setReturnValue(true);
		}
	}

	// Inject into keepRunning to handle planting on FarmlandSlab
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0), method = "tick", cancellable = true)
	private void keepRunningForFarmlandSlab(ServerLevel serverWorld, Villager villagerEntity, long l, CallbackInfo ci) {
		// This injection point is right before vanilla planting logic
		// We handle our own planting if above a FarmlandSlab
		if (this.aboveFarmlandPos == null) return;

		BlockState blockState = serverWorld.getBlockState(this.aboveFarmlandPos);
		Block block2 = serverWorld.getBlockState(this.aboveFarmlandPos.below()).getBlock();

		// If it's air above FarmlandSlab and villager has seeds
		if (blockState.isAir() && block2 instanceof FarmlandSlab && villagerEntity.hasFarmSeeds()) {
			SimpleContainer simpleInventory = villagerEntity.getInventory();

			for (int i = 0; i < simpleInventory.getContainerSize(); i++) {
				ItemStack itemStack = simpleInventory.getItem(i);
				boolean planted = false;

				if (!itemStack.isEmpty() && itemStack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS) && itemStack.getItem() instanceof BlockItem blockItem) {
					// Get the slab crop variant if available
					BlockState cropState = getSlabCropState(blockItem);
					if (cropState != null) {
						serverWorld.setBlockAndUpdate(this.aboveFarmlandPos, cropState);
						serverWorld.gameEvent(GameEvent.BLOCK_PLACE, this.aboveFarmlandPos, GameEvent.Context.of(villagerEntity, cropState));
						planted = true;
					}
				}

				if (planted) {
					serverWorld.playSound(
						null, this.aboveFarmlandPos.getX(), this.aboveFarmlandPos.getY(), this.aboveFarmlandPos.getZ(),
						SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F
					);
					itemStack.shrink(1);
					if (itemStack.isEmpty()) {
						simpleInventory.setItem(i, ItemStack.EMPTY);
					}
					// Cancel the rest of the method since we handled planting
					ci.cancel();
					return;
				}
			}
		}
	}

	private static BlockState getSlabCropState(BlockItem blockItem) {
		Block cropSlab = SlabRegistry.getCropSlab(blockItem.getBlock());
		return cropSlab != null ? cropSlab.defaultBlockState() : null;
	}
}
