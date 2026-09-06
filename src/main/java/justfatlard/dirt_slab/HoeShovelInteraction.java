package justfatlard.dirt_slab;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Handles hoe-till and shovel-flatten/halve/flip interactions on slab-related blocks.
 *
 * HoeItem/ShovelItem don't exist as Java classes in this snapshot: tool-block
 * interactions (tilling, flattening, stripping) are driven by the data-driven
 * BlockTransformer item component (DataComponents.BLOCK_TRANSFORMER), which has no
 * mod-facing extension point and no per-item-class method to mixin into. So this
 * uses Fabric API's UseBlockCallback (which fires before vanilla's own block-use
 * handling) to intercept hoe/shovel-on-block interactions via the vanilla
 * ItemTags.HOES / ItemTags.SHOVELS tags, which also covers any modded hoe or
 * shovel that carries those tags.
 */
public final class HoeShovelInteraction {
	private HoeShovelInteraction() {}

	public static void register() {
		UseBlockCallback.EVENT.register(HoeShovelInteraction::onUseBlock);
	}

	private static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		ItemStack stack = player.getItemInHand(hand);
		InteractionResult result;

		if (stack.getItem().builtInRegistryHolder().is(ItemTags.HOES)) {
			result = useHoe(player, world, hand, hitResult);
		} else if (stack.getItem().builtInRegistryHolder().is(ItemTags.SHOVELS)) {
			result = useShovel(player, world, hand, hitResult);
		} else {
			return InteractionResult.PASS;
		}

		// Swing the arm, because nothing else will. Vanilla swings when the client itself decides
		// an interaction happened, and here it never does: this mod lives on the server and the
		// client has no idea a shovel means anything to the block it is pointed at. The block
		// changed and the sound played, so the tool ended up doing its job with the player standing
		// perfectly still - which reads as the game having ignored the click.
		//
		// Broadcast rather than sent to the one player: an arm that only moves for the person
		// swinging it is a different bug wearing the same clothes.
		if (result == InteractionResult.SUCCESS && !world.isClientSide()) {
			player.swing(hand, stack.getInteractAnimation(), true);
		}

		return result;
	}

	// ── Hoe: till dirt/grass-type slabs into farmland slabs ────────────────────

	/**
	 * Hoe and shovel on a mixed slab, which work on its surface and leave the half underneath be.
	 *
	 * <p>Handled ahead of the ordinary path because the checks there are about slab geometry - which
	 * half is filled, whether a sliced top can exist - and a mixed slab is a full cube with no
	 * geometry left to ask about. Only the material of the top half is in question.
	 *
	 * <p>A hoe will not till one. Farmland is deliberately not a mixable slab: its behaviour lives
	 * in a moisture level a mixed slab has no room for, so one that looked like farmland could never
	 * be farmed. Refusing is the honest answer; coarse dirt still loosens to dirt.
	 *
	 * @return the new state, or null if this is not a mixed slab or nothing applies to it
	 */
	private static BlockState mixedSlabResult(BlockState state, boolean shovel) {
		if (shovel) {
			for (Block surface : new Block[]{
					DirtSlabBlocks.GRASS_SLAB, DirtSlabBlocks.DIRT_SLAB,
					DirtSlabBlocks.COARSE_DIRT_SLAB, DirtSlabBlocks.PODZOL_SLAB,
					DirtSlabBlocks.MYCELIUM_SLAB, DirtSlabBlocks.ROOTED_DIRT_SLAB}) {
				BlockState pathed = justfatlard.dirt_slab.integration.MixedSlabIntegration
					.resurface(state, surface, DirtSlabBlocks.GRASS_PATH_SLAB);
				if (pathed != null) return pathed;
			}
			return null;
		}

		return justfatlard.dirt_slab.integration.MixedSlabIntegration
			.resurface(state, DirtSlabBlocks.COARSE_DIRT_SLAB, DirtSlabBlocks.DIRT_SLAB);
	}

	private static InteractionResult applyMixed(Player player, Level world, InteractionHand hand,
			BlockPos pos, BlockState result,
			net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound) {
		if (!world.isClientSide()) {
			world.setBlockAndUpdate(pos, result);
			world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				sound, SoundSource.BLOCKS, 1.0F, 1.0F);
			player.getItemInHand(hand).hurtAndBreak(1, player, hand);
		}
		SlabEffects.dirtParticles(world, pos, 1);
		return InteractionResult.SUCCESS;
	}

	private static InteractionResult useHoe(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		BlockPos pos = hitResult.getBlockPos();
		BlockState state = world.getBlockState(pos);

		if (hitResult.getDirection() == Direction.DOWN) return InteractionResult.PASS;

		BlockState mixed = mixedSlabResult(state, false);
		if (mixed != null) return applyMixed(player, world, hand, pos, mixed, SoundEvents.HOE_TILL);

		if (!SlicedTopSlab.canExistAt(state, world, pos)) return InteractionResult.PASS;

		Block block = state.getBlock();
		BlockState newState = null;

		if (block == DirtSlabBlocks.COARSE_DIRT_SLAB) {
			newState = SlabRegistry.copySlabProperties(state, DirtSlabBlocks.DIRT_SLAB);
			if (!world.isClientSide()) {
				world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
		} else if (block == DirtSlabBlocks.DIRT_SLAB || block == DirtSlabBlocks.GRASS_SLAB || block == DirtSlabBlocks.GRASS_PATH_SLAB) {
			newState = SlabRegistry.copySlabProperties(state, DirtSlabBlocks.FARMLAND_SLAB);
			if (!world.isClientSide()) {
				world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
		}

		if (newState == null) return InteractionResult.PASS;

		if (!world.isClientSide()) {
			world.setBlockAndUpdate(pos, newState);
			player.getItemInHand(hand).hurtAndBreak(1, player, hand);
		}

		SlabEffects.dirtParticles(world, pos, 1);

		return InteractionResult.SUCCESS;
	}

	// ── Shovel: flip / halve / flatten slab-related blocks ─────────────────────

	private static InteractionResult useShovel(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		BlockPos pos = hitResult.getBlockPos();
		BlockState state = world.getBlockState(pos);

		if (hitResult.getDirection() == Direction.DOWN) return InteractionResult.PASS;

		BlockState mixed = mixedSlabResult(state, true);
		if (mixed != null) return applyMixed(player, world, hand, pos, mixed, SoundEvents.SHOVEL_FLATTEN);

		if (!SlicedTopSlab.canExistAt(state, world, pos)) return InteractionResult.PASS;

		Block block = state.getBlock();
		boolean isPlayerSneaking = player.isShiftKeyDown();
		BlockState newState = null;
		SlabType slabType = block instanceof SlabBlock ? state.getValue(SlabBlock.TYPE) : SlabType.DOUBLE;

		// Behavior 1: Sneak + single slab -> flip orientation (top <-> bottom)
		if (isPlayerSneaking && SlabRegistry.isTerrainSlab(block) && block instanceof SlabBlock && slabType != SlabType.DOUBLE) {
			newState = block.defaultBlockState()
				.setValue(SlabBlock.TYPE, slabType == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM)
				.setValue(SlabBlock.WATERLOGGED, state.getValue(SlabBlock.WATERLOGGED));
		}

		// Behavior 2: Sneak + full block or double slab -> halve into single slab, drop remainder
		else if (isPlayerSneaking && ((block instanceof SlabBlock && slabType == SlabType.DOUBLE) || SlabRegistry.getShovelHalveResult(block) != null)) {
			Block halveResult = SlabRegistry.getShovelHalveResult(block);
			if (halveResult != null) {
				newState = halveResult.defaultBlockState();

				ItemStack toolStack = player.getItemInHand(hand);
				ItemEnchantments enchantments = toolStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
				boolean isSilkTouch = world.registryAccess().lookup(Registries.ENCHANTMENT)
					.flatMap(registry -> registry.get(Enchantments.SILK_TOUCH))
					.map(silkTouch -> enchantments.getLevel(silkTouch) > 0)
					.orElse(false);

				if (!world.isClientSide()) {
					// Coarse dirt always drops itself; silk touch drops the slab type; otherwise drops dirt slab
					if (block == Blocks.COARSE_DIRT || block == DirtSlabBlocks.COARSE_DIRT_SLAB || isSilkTouch) {
						world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, new ItemStack(newState.getBlock().asItem())));
					} else {
						world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, new ItemStack(DirtSlabBlocks.DIRT_SLAB.asItem())));
					}
				}
			}
		}

		// Behavior 3: No sneak -> flatten to path
		else if (!isPlayerSneaking) {
			if (block == Blocks.DIRT) {
				newState = Blocks.DIRT_PATH.defaultBlockState();
			} else if (block == DirtSlabBlocks.GRASS_SLAB || block == DirtSlabBlocks.DIRT_SLAB) {
				newState = SlabRegistry.copySlabProperties(state, DirtSlabBlocks.GRASS_PATH_SLAB);
			}
		}

		if (newState == null) return InteractionResult.PASS;

		if (!world.isClientSide()) {
			world.setBlockAndUpdate(pos, newState);

			((ServerLevel) world).sendParticles(ParticleTypes.MYCELIUM, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.25, 0.02, 0.25, 0.15);

			player.getItemInHand(hand).hurtAndBreak(1, player, hand);

			world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
		}

		return InteractionResult.SUCCESS;
	}
}
