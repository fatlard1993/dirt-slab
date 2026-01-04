package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlicedTopSlab;

@Mixin(ShovelItem.class)
public class ShovelMixin {
	@Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
	private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info){
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);

		if(context.getSide() != Direction.DOWN && SlicedTopSlab.canExistAt(state, world, pos)){
			PlayerEntity player = context.getPlayer();
			Block block = state.getBlock();
			Boolean isPlayerSneaking = player != null && player.isSneaking();
			Boolean success = false;
			BlockState newState = Blocks.GREEN_WOOL.getDefaultState();
			SlabType slabType = block instanceof SlabBlock ? (SlabType)state.get(SlabBlock.TYPE) : SlabType.DOUBLE;

			if(isPlayerSneaking && block instanceof SlabBlock && slabType != SlabType.DOUBLE){ // single swaps
				if(block == DirtSlabBlocks.DIRT_SLAB){
					newState = block.getDefaultState().with(SlabBlock.TYPE, slabType == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED));

					success = true;
				}

				else if(block == DirtSlabBlocks.GRASS_SLAB){
					newState = block.getDefaultState().with(SlabBlock.TYPE, slabType == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED));

					success = true;
				}

				else if(block == DirtSlabBlocks.COARSE_DIRT_SLAB && slabType == SlabType.BOTTOM || slabType == SlabType.TOP){
					newState = block.getDefaultState().with(SlabBlock.TYPE, slabType == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED));

					success = true;
				}

				else if(block == DirtSlabBlocks.FARMLAND_SLAB && slabType == SlabType.BOTTOM || slabType == SlabType.TOP){
					newState = block.getDefaultState().with(SlabBlock.TYPE, slabType == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED));

					success = true;
				}

				else if(block == DirtSlabBlocks.GRASS_PATH_SLAB && slabType == SlabType.BOTTOM || slabType == SlabType.TOP){
					newState = block.getDefaultState().with(SlabBlock.TYPE, slabType == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED));

					success = true;
				}

				else if(block == DirtSlabBlocks.MYCELIUM_SLAB && slabType == SlabType.BOTTOM || slabType == SlabType.TOP){
					newState = block.getDefaultState().with(SlabBlock.TYPE, slabType == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED));

					success = true;
				}

				else if(block == DirtSlabBlocks.PODZOL_SLAB && slabType == SlabType.BOTTOM || slabType == SlabType.TOP){
					newState = block.getDefaultState().with(SlabBlock.TYPE, slabType == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED));

					success = true;
				}
			}

			else if(isPlayerSneaking && ((block instanceof SlabBlock && slabType == SlabType.DOUBLE) || block instanceof Block)){ // doubles to singles
				ItemEnchantmentsComponent enchantments = context.getStack().getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
				boolean isSilkTouch = world.getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT)
					.flatMap(registry -> registry.getOptional(Enchantments.SILK_TOUCH))
					.map(silkTouch -> enchantments.getLevel(silkTouch) > 0)
					.orElse(false);

				if(block == Blocks.DIRT || block == DirtSlabBlocks.DIRT_SLAB){
					newState = DirtSlabBlocks.DIRT_SLAB.getDefaultState();

					success = true;
				}

				else if(block == Blocks.GRASS_BLOCK || block == DirtSlabBlocks.GRASS_SLAB){
					newState = DirtSlabBlocks.GRASS_SLAB.getDefaultState();

					success = true;
				}

				else if(block == Blocks.COARSE_DIRT || block == DirtSlabBlocks.COARSE_DIRT_SLAB){
					newState = DirtSlabBlocks.COARSE_DIRT_SLAB.getDefaultState();

					success = true;

					world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY() + 1, pos.getZ(), new ItemStack(newState.getBlock().asItem())));
				}

				else if(block == Blocks.FARMLAND || block == DirtSlabBlocks.FARMLAND_SLAB){
					newState = DirtSlabBlocks.FARMLAND_SLAB.getDefaultState();

					success = true;
				}

				else if(block == Blocks.DIRT_PATH || block == DirtSlabBlocks.GRASS_PATH_SLAB){
					newState = DirtSlabBlocks.GRASS_PATH_SLAB.getDefaultState();

					success = true;
				}

				else if(block == Blocks.MYCELIUM || block == DirtSlabBlocks.MYCELIUM_SLAB){
					newState = DirtSlabBlocks.MYCELIUM_SLAB.getDefaultState();

					success = true;
				}

				else if(block == Blocks.PODZOL || block == DirtSlabBlocks.PODZOL_SLAB){
					newState = DirtSlabBlocks.PODZOL_SLAB.getDefaultState();

					success = true;
				}

				if(success && !(block == Blocks.COARSE_DIRT || block == DirtSlabBlocks.COARSE_DIRT_SLAB)){
					if(isSilkTouch) world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY() + 1, pos.getZ(), new ItemStack(newState.getBlock().asItem())));

					else world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY() + 1, pos.getZ(), new ItemStack(DirtSlabBlocks.DIRT_SLAB.asItem())));
				}
			}

			else if(!isPlayerSneaking){
				if(block == Blocks.DIRT){
					newState = Blocks.DIRT_PATH.getDefaultState();

					success = true;
				}

				else if((block == DirtSlabBlocks.GRASS_SLAB || block == DirtSlabBlocks.DIRT_SLAB)){
					newState = DirtSlabBlocks.GRASS_PATH_SLAB.getDefaultState().with(SlabBlock.TYPE, state.get(SlabBlock.TYPE)).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED));

					success = true;
				}
			}

			if(success){
				if(!world.isClient()){
					world.setBlockState(pos, newState);

					((ServerWorld) world).spawnParticles(ParticleTypes.MYCELIUM, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.25, 0.02, 0.25, 0.15);

					if(player != null) context.getStack().damage(1, player, context.getHand());
				}

				else world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);


				info.setReturnValue(ActionResult.SUCCESS);
			}
		}
	}
}
