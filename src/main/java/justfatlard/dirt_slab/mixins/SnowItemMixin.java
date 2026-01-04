package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlabSnowLayerBlock;

@Mixin(BlockItem.class)
public class SnowItemMixin {

	/**
	 * Intercept snow block item placement to place our slab snow on bottom slabs
	 */
	@Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("HEAD"), cancellable = true)
	private void interceptSnowPlacement(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
		BlockItem self = (BlockItem)(Object)this;

		// Only intercept snow layer item
		if (self.asItem() != Items.SNOW) {
			return;
		}

		World world = context.getWorld();
		BlockPos placePos = context.getBlockPos();
		BlockPos belowPos = placePos.down();
		BlockState belowState = world.getBlockState(belowPos);

		// Check if we're placing on a bottom slab
		if (Main.isAnySlab(belowState.getBlock()) && belowState.contains(SlabBlock.TYPE)) {
			SlabType type = belowState.get(SlabBlock.TYPE);

			if (type == SlabType.BOTTOM) {
				BlockState currentState = world.getBlockState(placePos);

				// If there's already our slab snow, try to add layers
				if (currentState.isOf(justfatlard.dirt_slab.DirtSlabBlocks.SNOW_LAYER_SLAB)) {
					int currentLayers = currentState.get(SlabSnowLayerBlock.LAYERS);
					if (currentLayers < 8) {
						world.setBlockState(placePos, currentState.with(SlabSnowLayerBlock.LAYERS, currentLayers + 1), Block.NOTIFY_ALL);
						if (!context.getPlayer().getAbilities().creativeMode) {
							context.getStack().decrement(1);
						}
						cir.setReturnValue(ActionResult.SUCCESS);
						return;
					}
					// Already at max layers
					cir.setReturnValue(ActionResult.FAIL);
					return;
				}

				// If the position is air, place our slab snow
				if (currentState.isAir()) {
					BlockState snowState = SlabSnowLayerBlock.createForSlab(belowState);
					world.setBlockState(placePos, snowState, Block.NOTIFY_ALL);
					if (!context.getPlayer().getAbilities().creativeMode) {
						context.getStack().decrement(1);
					}
					cir.setReturnValue(ActionResult.SUCCESS);
					return;
				}
			}
		}
	}
}
