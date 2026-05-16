package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlicedTopSlab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

@Mixin(ShovelItem.class)
public class ShovelMixin {
	@Inject(at = @At("HEAD"), method = "useOn", cancellable = true)
	private void useOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> info){
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);

		if(context.getClickedFace() != Direction.DOWN && SlicedTopSlab.canExistAt(state, world, pos)){
			Player player = context.getPlayer();
			Block block = state.getBlock();
			boolean isPlayerSneaking = player != null && player.isShiftKeyDown();
			boolean success = false;
			BlockState newState = null;
			SlabType slabType = block instanceof SlabBlock ? (SlabType)state.getValue(SlabBlock.TYPE) : SlabType.DOUBLE;

			// Behavior 1: Sneak + single slab → flip orientation (top ↔ bottom)
			if(isPlayerSneaking && SlabRegistry.isTerrainSlab(block) && block instanceof SlabBlock && slabType != SlabType.DOUBLE){
				newState = block.defaultBlockState().setValue(SlabBlock.TYPE, slabType == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM).setValue(SlabBlock.WATERLOGGED, state.getValue(SlabBlock.WATERLOGGED));
				success = true;
			}

			// Behavior 2: Sneak + full block or double slab → halve into single slab, drop remainder
			else if(isPlayerSneaking && ((block instanceof SlabBlock && slabType == SlabType.DOUBLE) || SlabRegistry.getShovelHalveResult(block) != null)){
				Block halveResult = SlabRegistry.getShovelHalveResult(block);
				if(halveResult != null){
					newState = halveResult.defaultBlockState();
					success = true;

					ItemEnchantments enchantments = context.getItemInHand().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
					boolean isSilkTouch = world.registryAccess().lookup(Registries.ENCHANTMENT)
						.flatMap(registry -> registry.get(Enchantments.SILK_TOUCH))
						.map(silkTouch -> enchantments.getLevel(silkTouch) > 0)
						.orElse(false);

					if(!world.isClientSide()){
						// Coarse dirt always drops itself; silk touch drops the slab type; otherwise drops dirt slab
						if(block == Blocks.COARSE_DIRT || block == DirtSlabBlocks.COARSE_DIRT_SLAB || isSilkTouch){
							world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, new ItemStack(newState.getBlock().asItem())));
						}
						else {
							world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, new ItemStack(DirtSlabBlocks.DIRT_SLAB.asItem())));
						}
					}
				}
			}

			// Behavior 3: No sneak → flatten to path
			else if(!isPlayerSneaking){
				if(block == Blocks.DIRT){
					newState = Blocks.DIRT_PATH.defaultBlockState();
					success = true;
				}
				else if((block == DirtSlabBlocks.GRASS_SLAB || block == DirtSlabBlocks.DIRT_SLAB)){
					newState = SlabRegistry.copySlabProperties(state, DirtSlabBlocks.GRASS_PATH_SLAB);
					success = true;
				}
			}

			if(success){
				if(!world.isClientSide()){
					world.setBlockAndUpdate(pos, newState);

					((ServerLevel) world).sendParticles(ParticleTypes.MYCELIUM, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.25, 0.02, 0.25, 0.15);

					if(player != null) context.getItemInHand().hurtAndBreak(1, player, context.getHand());
				}

				else world.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);


				info.setReturnValue(InteractionResult.SUCCESS);
			}
		}
	}
}
