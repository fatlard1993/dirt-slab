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
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
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

public class SlabSaplingBlock extends Block implements Fertilizable {
	public static final MapCodec<SlabSaplingBlock> CODEC = createCodec(SlabSaplingBlock::new);
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");
	public static final IntProperty STAGE = IntProperty.of("stage", 0, 1);

	private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(2.0, -8.0, 2.0, 14.0, 4.0, 14.0);

	private final RegistryKey<ConfiguredFeature<?, ?>> treeFeature;
	private final RegistryKey<ConfiguredFeature<?, ?>> megaTreeFeature;
	private final Item saplingItem;

	public SlabSaplingBlock(Settings settings) {
		this(settings, null, null, null);
	}

	public SlabSaplingBlock(Settings settings, RegistryKey<ConfiguredFeature<?, ?>> treeFeature, Item saplingItem) {
		this(settings, treeFeature, null, saplingItem);
	}

	public SlabSaplingBlock(Settings settings, RegistryKey<ConfiguredFeature<?, ?>> treeFeature, RegistryKey<ConfiguredFeature<?, ?>> megaTreeFeature, Item saplingItem) {
		super(settings);
		this.treeFeature = treeFeature;
		this.megaTreeFeature = megaTreeFeature;
		this.saplingItem = saplingItem;
		this.setDefaultState(this.stateManager.getDefaultState().with(BOTTOM_OFFSET, false).with(STAGE, 0));
	}

	@Override
	protected MapCodec<SlabSaplingBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET, STAGE);
	}

	@Override
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
		return saplingItem != null ? new ItemStack(saplingItem) : ItemStack.EMPTY;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(BOTTOM_OFFSET) ? OFFSET_SHAPE : SHAPE;
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		return Main.isGrassType(below.getBlock()) || Main.isAnySlab(below.getBlock()) ||
			   below.isOf(Blocks.GRASS_BLOCK) || below.isOf(Blocks.DIRT) || below.isOf(Blocks.COARSE_DIRT) ||
			   below.isOf(Blocks.PODZOL) || below.isOf(Blocks.FARMLAND) || below.isOf(Blocks.MUD) ||
			   below.isOf(Blocks.MUDDY_MANGROVE_ROOTS) || below.isOf(Blocks.MOSS_BLOCK);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
		if (direction == Direction.DOWN && !canPlaceAt(state, world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (world.getLightLevel(pos.up()) >= 9 && random.nextInt(7) == 0) {
			this.grow(world, random, pos, state);
		}
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return (double)random.nextFloat() < 0.45;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		if (state.get(STAGE) == 0) {
			world.setBlockState(pos, state.with(STAGE, 1), Block.NO_REDRAW);
		} else {
			generateTree(world, random, pos, state);
		}
	}

	private void generateTree(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		if (treeFeature == null) return;

		// Check for mega tree (2x2 saplings)
		if (megaTreeFeature != null) {
			for (int dx = 0; dx >= -1; --dx) {
				for (int dz = 0; dz >= -1; --dz) {
					if (canGenerateMegaTree(world, pos, dx, dz)) {
						generateMegaTree(world, random, pos, dx, dz);
						return;
					}
				}
			}
		}

		// Generate regular tree
		Optional<RegistryEntry.Reference<ConfiguredFeature<?, ?>>> feature = world.getRegistryManager()
			.getOrThrow(RegistryKeys.CONFIGURED_FEATURE)
			.getOptional(treeFeature);

		if (feature.isPresent()) {
			BlockPos belowPos = pos.down();
			BlockState below = world.getBlockState(belowPos);
			if (Main.isAnySlab(below.getBlock())) {
				world.setBlockState(belowPos, Blocks.DIRT.getDefaultState());
			}
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			if (!feature.get().value().generate(world, world.getChunkManager().getChunkGenerator(), random, pos)) {
				world.setBlockState(pos, state);
				if (Main.isAnySlab(below.getBlock())) {
					world.setBlockState(belowPos, below);
				}
			}
		}
	}

	private boolean canGenerateMegaTree(ServerWorld world, BlockPos pos, int dx, int dz) {
		Block thisBlock = this.asBlock();
		for (int x = 0; x <= 1; ++x) {
			for (int z = 0; z <= 1; ++z) {
				BlockPos checkPos = pos.add(dx + x, 0, dz + z);
				BlockState checkState = world.getBlockState(checkPos);
				if (!checkState.isOf(thisBlock) && !(checkState.getBlock() instanceof SlabSaplingBlock)) {
					return false;
				}
			}
		}
		return true;
	}

	private void generateMegaTree(ServerWorld world, Random random, BlockPos pos, int dx, int dz) {
		Optional<RegistryEntry.Reference<ConfiguredFeature<?, ?>>> feature = world.getRegistryManager()
			.getOrThrow(RegistryKeys.CONFIGURED_FEATURE)
			.getOptional(megaTreeFeature);

		if (feature.isPresent()) {
			BlockPos cornerPos = pos.add(dx, 0, dz);

			// Replace slabs with dirt and clear saplings
			for (int x = 0; x <= 1; ++x) {
				for (int z = 0; z <= 1; ++z) {
					BlockPos saplingPos = cornerPos.add(x, 0, z);
					BlockPos belowPos = saplingPos.down();
					BlockState below = world.getBlockState(belowPos);
					if (Main.isAnySlab(below.getBlock())) {
						world.setBlockState(belowPos, Blocks.DIRT.getDefaultState());
					}
					world.setBlockState(saplingPos, Blocks.AIR.getDefaultState());
				}
			}

			if (!feature.get().value().generate(world, world.getChunkManager().getChunkGenerator(), random, cornerPos.add(1, 0, 1))) {
				// Tree generation failed - this is acceptable, don't restore saplings for mega trees
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
