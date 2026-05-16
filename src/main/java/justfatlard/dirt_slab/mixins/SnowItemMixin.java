package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabSnowLayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

@Mixin(BlockItem.class)
public class SnowItemMixin {

	/**
	 * Intercept snow block item placement to place our slab snow on bottom slabs
	 */
	@Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"), cancellable = true)
	private void interceptSnowPlacement(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		BlockItem self = (BlockItem)(Object)this;

		// Only intercept snow layer item
		if (self.asItem() != Items.SNOW) {
			return;
		}

		Level world = context.getLevel();
		BlockPos placePos = context.getClickedPos();
		BlockPos belowPos = placePos.below();
		BlockState belowState = world.getBlockState(belowPos);

		// Check if we're placing on a bottom slab
		if (SlabRegistry.isTerrainSlab(belowState.getBlock()) && belowState.hasProperty(SlabBlock.TYPE)) {
			SlabType type = belowState.getValue(SlabBlock.TYPE);

			if (type == SlabType.BOTTOM) {
				BlockState currentState = world.getBlockState(placePos);

				// If there's already our slab snow, try to add layers
				if (currentState.is(justfatlard.dirt_slab.DirtSlabBlocks.SNOW_LAYER_SLAB)) {
					int currentLayers = currentState.getValue(SlabSnowLayerBlock.LAYERS);
					if (currentLayers < 8) {
						world.setBlock(placePos, currentState.setValue(SlabSnowLayerBlock.LAYERS, currentLayers + 1), Block.UPDATE_ALL);
						if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
							context.getItemInHand().shrink(1);
						}
						cir.setReturnValue(InteractionResult.SUCCESS);
						return;
					}
					// Already at max layers
					cir.setReturnValue(InteractionResult.FAIL);
					return;
				}

				// If the position is air, place our slab snow
				if (currentState.isAir()) {
					BlockState snowState = SlabSnowLayerBlock.createForSlab(belowState);
					world.setBlock(placePos, snowState, Block.UPDATE_ALL);
					if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
						context.getItemInHand().shrink(1);
					}
					cir.setReturnValue(InteractionResult.SUCCESS);
					return;
				}
			}
		}
	}
}
