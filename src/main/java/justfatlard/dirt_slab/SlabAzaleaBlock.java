package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.Optional;

public class SlabAzaleaBlock extends Block implements Fertilizable {
	public static final MapCodec<SlabAzaleaBlock> CODEC = createCodec(SlabAzaleaBlock::new);
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");
	public static final BooleanProperty FLOWERING = BooleanProperty.of("flowering");

	private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, 8.0, 16.0);

	public SlabAzaleaBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(BOTTOM_OFFSET, false).with(FLOWERING, false));
	}

	@Override
	protected MapCodec<SlabAzaleaBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET, FLOWERING);
	}

	@Override
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(state.get(FLOWERING) ? Items.FLOWERING_AZALEA : Items.AZALEA);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		return Main.isGrassType(below.getBlock()) || Main.isAnySlab(below.getBlock()) ||
			   below.isOf(Blocks.GRASS_BLOCK) || below.isOf(Blocks.DIRT) || below.isOf(Blocks.COARSE_DIRT) ||
			   below.isOf(Blocks.PODZOL) || below.isOf(Blocks.FARMLAND) || below.isOf(Blocks.CLAY) ||
			   below.isOf(Blocks.MOSS_BLOCK);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
		if (direction == Direction.DOWN && !canPlaceAt(state, world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
		return world.getFluidState(pos.up()).isEmpty();
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return (double)random.nextFloat() < 0.45;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		// Try to grow into an azalea tree
		Optional<RegistryEntry.Reference<ConfiguredFeature<?, ?>>> feature = world.getRegistryManager()
			.getOrThrow(RegistryKeys.CONFIGURED_FEATURE)
			.getOptional(TreeConfiguredFeatures.AZALEA_TREE);

		if (feature.isPresent()) {
			// Replace the block below with dirt if it's a slab (tree needs solid ground)
			BlockPos belowPos = pos.down();
			BlockState below = world.getBlockState(belowPos);
			if (Main.isAnySlab(below.getBlock())) {
				world.setBlockState(belowPos, Blocks.DIRT.getDefaultState());
			}
			// Remove this block so tree can generate
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			if (!feature.get().value().generate(world, world.getChunkManager().getChunkGenerator(), random, pos)) {
				// If tree generation failed, put the azalea back
				world.setBlockState(pos, state);
				if (Main.isAnySlab(below.getBlock())) {
					world.setBlockState(belowPos, below);
				}
			}
		}
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (Main.isAnySlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
