package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabAzaleaBlock extends Block implements BonemealableBlock, OffsetableSlab {
	public static final MapCodec<SlabAzaleaBlock> CODEC = simpleCodec(SlabAzaleaBlock::new);
	public static final BooleanProperty FLOWERING = BooleanProperty.create("flowering");

	private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape OFFSET_SHAPE = Block.box(0.0, -8.0, 0.0, 16.0, 8.0, 16.0);

	public SlabAzaleaBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(BOTTOM_OFFSET, false).setValue(FLOWERING, false));
	}

	@Override
	protected MapCodec<SlabAzaleaBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET, FLOWERING);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(state.getValue(FLOWERING) ? Items.FLOWERING_AZALEA : Items.AZALEA);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		return SlabRegistry.isGrassType(below.getBlock()) || SlabRegistry.isTerrainSlab(below.getBlock()) ||
			   below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT) || below.is(Blocks.COARSE_DIRT) ||
			   below.is(Blocks.PODZOL) || below.is(Blocks.FARMLAND) || below.is(Blocks.CLAY) ||
			   below.is(Blocks.MOSS_BLOCK);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == Direction.DOWN && !canSurvive(state, world, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
		return world.getFluidState(pos.above()).isEmpty();
	}

	@Override
	public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
		return (double)random.nextFloat() < 0.45;
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
		// Try to grow into an azalea tree
		Optional<Holder.Reference<ConfiguredFeature<?, ?>>> feature = world.registryAccess()
			.lookupOrThrow(Registries.CONFIGURED_FEATURE)
			.get(TreeFeatures.AZALEA_TREE);

		if (feature.isPresent()) {
			// Replace the block below with dirt if it's a slab (tree needs solid ground)
			BlockPos belowPos = pos.below();
			BlockState below = world.getBlockState(belowPos);
			if (SlabRegistry.isTerrainSlab(below.getBlock())) {
				world.setBlockAndUpdate(belowPos, Blocks.DIRT.defaultBlockState());
			}
			// Remove this block so tree can generate
			world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			if (!feature.get().value().place(world, world.getChunkSource().getGenerator(), random, pos)) {
				// If tree generation failed, put the azalea back
				world.setBlockAndUpdate(pos, state);
				if (SlabRegistry.isTerrainSlab(below.getBlock())) {
					world.setBlockAndUpdate(belowPos, below);
				}
			}
		}
	}
}
