package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabSaplingBlock extends Block implements BonemealableBlock, OffsetableSlab {
	public static final MapCodec<SlabSaplingBlock> CODEC = simpleCodec(SlabSaplingBlock::new);
	public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);

	private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
	private static final VoxelShape OFFSET_SHAPE = Block.box(2.0, -8.0, 2.0, 14.0, 4.0, 14.0);

	private final ResourceKey<ConfiguredFeature<?, ?>> treeFeature;
	private final ResourceKey<ConfiguredFeature<?, ?>> megaTreeFeature;
	private final Item saplingItem;

	public SlabSaplingBlock(Properties settings) {
		this(settings, null, null, null);
	}

	public SlabSaplingBlock(Properties settings, ResourceKey<ConfiguredFeature<?, ?>> treeFeature, Item saplingItem) {
		this(settings, treeFeature, null, saplingItem);
	}

	public SlabSaplingBlock(Properties settings, ResourceKey<ConfiguredFeature<?, ?>> treeFeature, ResourceKey<ConfiguredFeature<?, ?>> megaTreeFeature, Item saplingItem) {
		super(settings);
		this.treeFeature = treeFeature;
		this.megaTreeFeature = megaTreeFeature;
		this.saplingItem = saplingItem;
		this.registerDefaultState(this.stateDefinition.any().setValue(BOTTOM_OFFSET, false).setValue(STAGE, 0));
	}

	@Override
	protected MapCodec<SlabSaplingBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET, STAGE);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
		return saplingItem != null ? new ItemStack(saplingItem) : ItemStack.EMPTY;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return state.getValue(BOTTOM_OFFSET) ? OFFSET_SHAPE : SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		return SlabRegistry.isGrassType(below.getBlock()) || SlabRegistry.isTerrainSlab(below.getBlock()) ||
			   below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT) || below.is(Blocks.COARSE_DIRT) ||
			   below.is(Blocks.PODZOL) || below.is(Blocks.FARMLAND) || below.is(Blocks.MUD) ||
			   below.is(Blocks.MUDDY_MANGROVE_ROOTS) || below.is(Blocks.MOSS_BLOCK);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == Direction.DOWN && !canSurvive(state, world, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (world.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
			this.performBonemeal(world, random, pos, state);
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
		return (double)random.nextFloat() < 0.45;
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
		if (state.getValue(STAGE) == 0) {
			world.setBlock(pos, state.setValue(STAGE, 1), Block.UPDATE_INVISIBLE);
		} else {
			generateTree(world, random, pos, state);
		}
	}

	private void generateTree(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
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
		Optional<Holder.Reference<ConfiguredFeature<?, ?>>> feature = world.registryAccess()
			.lookupOrThrow(Registries.CONFIGURED_FEATURE)
			.get(treeFeature);

		if (feature.isPresent()) {
			BlockPos belowPos = pos.below();
			BlockState below = world.getBlockState(belowPos);
			if (SlabRegistry.isTerrainSlab(below.getBlock())) {
				world.setBlockAndUpdate(belowPos, Blocks.DIRT.defaultBlockState());
			}
			world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			if (!feature.get().value().place(world, world.getChunkSource().getGenerator(), random, pos)) {
				world.setBlockAndUpdate(pos, state);
				if (SlabRegistry.isTerrainSlab(below.getBlock())) {
					world.setBlockAndUpdate(belowPos, below);
				}
			}
		}
	}

	private boolean canGenerateMegaTree(ServerLevel world, BlockPos pos, int dx, int dz) {
		Block thisBlock = this.asBlock();
		for (int x = 0; x <= 1; ++x) {
			for (int z = 0; z <= 1; ++z) {
				BlockPos checkPos = pos.offset(dx + x, 0, dz + z);
				BlockState checkState = world.getBlockState(checkPos);
				if (!checkState.is(thisBlock) && !(checkState.getBlock() instanceof SlabSaplingBlock)) {
					return false;
				}
			}
		}
		return true;
	}

	private void generateMegaTree(ServerLevel world, RandomSource random, BlockPos pos, int dx, int dz) {
		Optional<Holder.Reference<ConfiguredFeature<?, ?>>> feature = world.registryAccess()
			.lookupOrThrow(Registries.CONFIGURED_FEATURE)
			.get(megaTreeFeature);

		if (feature.isPresent()) {
			BlockPos cornerPos = pos.offset(dx, 0, dz);

			// Save original states for restoration on failure
			BlockState[][] savedSaplings = new BlockState[2][2];
			BlockState[][] savedBelows = new BlockState[2][2];

			for (int x = 0; x <= 1; ++x) {
				for (int z = 0; z <= 1; ++z) {
					BlockPos saplingPos = cornerPos.offset(x, 0, z);
					BlockPos belowPos = saplingPos.below();
					savedSaplings[x][z] = world.getBlockState(saplingPos);
					savedBelows[x][z] = world.getBlockState(belowPos);

					if (SlabRegistry.isTerrainSlab(savedBelows[x][z].getBlock())) {
						world.setBlockAndUpdate(belowPos, Blocks.DIRT.defaultBlockState());
					}
					world.setBlockAndUpdate(saplingPos, Blocks.AIR.defaultBlockState());
				}
			}

			if (!feature.get().value().place(world, world.getChunkSource().getGenerator(), random, cornerPos.offset(1, 0, 1))) {
				// Restore saplings and ground blocks on failure
				for (int x = 0; x <= 1; ++x) {
					for (int z = 0; z <= 1; ++z) {
						BlockPos saplingPos = cornerPos.offset(x, 0, z);
						BlockPos belowPos = saplingPos.below();
						world.setBlockAndUpdate(belowPos, savedBelows[x][z]);
						world.setBlockAndUpdate(saplingPos, savedSaplings[x][z]);
					}
				}
			}
		}
	}
}
