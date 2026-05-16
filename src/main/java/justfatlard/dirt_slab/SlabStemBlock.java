package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabStemBlock extends VegetationBlock implements BonemealableBlock, OffsetableSlab {
	public static final MapCodec<SlabStemBlock> CODEC = simpleCodec(SlabStemBlock::new);
	public static final int MAX_AGE = 7;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

	// Normal shapes (same as vanilla StemBlock)
	private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[]{
		Block.box(7.0, 0.0, 7.0, 9.0, 2.0, 9.0),   // age 0
		Block.box(7.0, 0.0, 7.0, 9.0, 4.0, 9.0),   // age 1
		Block.box(7.0, 0.0, 7.0, 9.0, 6.0, 9.0),   // age 2
		Block.box(7.0, 0.0, 7.0, 9.0, 8.0, 9.0),   // age 3
		Block.box(7.0, 0.0, 7.0, 9.0, 10.0, 9.0),  // age 4
		Block.box(7.0, 0.0, 7.0, 9.0, 12.0, 9.0),  // age 5
		Block.box(7.0, 0.0, 7.0, 9.0, 14.0, 9.0),  // age 6
		Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)   // age 7
	};

	// Offset shapes for bottom slab placement (8 pixels lower)
	private static final VoxelShape[] OFFSET_AGE_TO_SHAPE = new VoxelShape[]{
		Block.box(7.0, -8.0, 7.0, 9.0, -6.0, 9.0),   // age 0
		Block.box(7.0, -8.0, 7.0, 9.0, -4.0, 9.0),   // age 1
		Block.box(7.0, -8.0, 7.0, 9.0, -2.0, 9.0),   // age 2
		Block.box(7.0, -8.0, 7.0, 9.0, 0.0, 9.0),    // age 3
		Block.box(7.0, -8.0, 7.0, 9.0, 2.0, 9.0),    // age 4
		Block.box(7.0, -8.0, 7.0, 9.0, 4.0, 9.0),    // age 5
		Block.box(7.0, -8.0, 7.0, 9.0, 6.0, 9.0),    // age 6
		Block.box(7.0, -8.0, 7.0, 9.0, 8.0, 9.0)     // age 7
	};

	private final ItemLike pickBlockItem;
	private final boolean isMelon;

	public SlabStemBlock(boolean isMelon, ItemLike pickBlockItem, Properties settings) {
		super(settings);
		this.isMelon = isMelon;
		this.pickBlockItem = pickBlockItem;
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(BOTTOM_OFFSET, false));
	}

	public SlabStemBlock(Properties settings) {
		this(true, null, settings);
	}

	@Override
	protected MapCodec<? extends VegetationBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_AGE_TO_SHAPE[state.getValue(AGE)];
		}
		return AGE_TO_SHAPE[state.getValue(AGE)];
	}

	@Override
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		return floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos below = pos.below();
		BlockState floorState = world.getBlockState(below);
		return mayPlaceOn(floorState, world, below);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (world.getRawBrightness(pos, 0) >= 9) {
			float growthChance = getGrowthChance(world, pos);
			if (random.nextInt((int)(25.0F / growthChance) + 1) == 0) {
				int age = state.getValue(AGE);
				if (age < MAX_AGE) {
					// Preserve BOTTOM_OFFSET when growing
					boolean offset = state.getValue(BOTTOM_OFFSET);
					world.setBlock(pos, state.setValue(AGE, age + 1).setValue(BOTTOM_OFFSET, offset), Block.UPDATE_CLIENTS);
				} else {
					// At max age, try to spawn fruit - try all directions with random start
					Direction[] horizontals = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
					int startIndex = random.nextInt(4);

					for (int i = 0; i < 4; i++) {
						Direction direction = horizontals[(startIndex + i) % 4];
						BlockPos fruitPos = pos.relative(direction);
						BlockPos groundPos = fruitPos.below();
						BlockState groundState = world.getBlockState(groundPos);


						// Check standard position (same level as stem, ground below)
						if (canSpawnFruitAt(world, fruitPos) && isValidGourdGround(groundState)) {
							// Spawn the fruit
							Block gourdBlock = isMelon ? Blocks.MELON : Blocks.PUMPKIN;
							world.setBlock(fruitPos, gourdBlock.defaultBlockState(), Block.UPDATE_ALL);

							// Convert stem to attached stem pointing toward the fruit
							// Use slab attached stems to preserve BOTTOM_OFFSET
							boolean offset = state.getValue(BOTTOM_OFFSET);
							Block attachedStemBlock = isMelon ? DirtSlabBlocks.ATTACHED_MELON_STEM_SLAB : DirtSlabBlocks.ATTACHED_PUMPKIN_STEM_SLAB;
							world.setBlock(pos, attachedStemBlock.defaultBlockState()
								.setValue(SlabAttachedStemBlock.FACING, direction)
								.setValue(BOTTOM_OFFSET, offset), Block.UPDATE_CLIENTS);
							break;
						}

						// Also check one level down for bottom slab configurations
						BlockPos lowerFruitPos = fruitPos.below();
						BlockPos lowerGroundPos = lowerFruitPos.below();
						BlockState lowerGroundState = world.getBlockState(lowerGroundPos);

						if (canSpawnFruitAt(world, lowerFruitPos) && isValidGourdGround(lowerGroundState)) {
							Block gourdBlock = isMelon ? Blocks.MELON : Blocks.PUMPKIN;
							world.setBlock(lowerFruitPos, gourdBlock.defaultBlockState(), Block.UPDATE_ALL);

							// Use slab attached stems to preserve BOTTOM_OFFSET
							boolean offset = state.getValue(BOTTOM_OFFSET);
							Block attachedStemBlock = isMelon ? DirtSlabBlocks.ATTACHED_MELON_STEM_SLAB : DirtSlabBlocks.ATTACHED_PUMPKIN_STEM_SLAB;
							world.setBlock(pos, attachedStemBlock.defaultBlockState()
								.setValue(SlabAttachedStemBlock.FACING, direction)
								.setValue(BOTTOM_OFFSET, offset), Block.UPDATE_CLIENTS);
							break;
						}
					}
				}
			}
		}
	}

	private boolean isValidGourdGround(BlockState groundState) {
		Block block = groundState.getBlock();

		if (SlabRegistry.isGourdGround(block)) return true;

		// Terrain slabs are valid if TOP or DOUBLE (bottom slabs have no surface for fruit)
		if (SlabRegistry.isTerrainSlab(block)) {
			SlabType slabType = groundState.getValue(SlabBlock.TYPE);
			return slabType == SlabType.TOP || slabType == SlabType.DOUBLE;
		}

		return false;
	}

	private boolean canSpawnFruitAt(ServerLevel world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		return state.isAir() || state.canBeReplaced();
	}

	// Fertilizable interface methods
	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
		return state.getValue(AGE) < MAX_AGE;
	}

	@Override
	public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
		int newAge = Math.min(state.getValue(AGE) + random.nextIntBetweenInclusive(2, 5), MAX_AGE);
		// Preserve BOTTOM_OFFSET when growing via bonemeal
		boolean offset = state.getValue(BOTTOM_OFFSET);
		world.setBlock(pos, state.setValue(AGE, newAge).setValue(BOTTOM_OFFSET, offset), Block.UPDATE_CLIENTS);
	}

	private float getGrowthChance(ServerLevel world, BlockPos pos) {
		float chance = 1.0F;
		BlockPos floorPos = pos.below();
		for (int x = -1; x <= 1; ++x) {
			for (int z = -1; z <= 1; ++z) {
				float bonus = 0.0F;
				BlockState floor = world.getBlockState(floorPos.offset(x, 0, z));
				if (floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
					bonus = floor.getValue(FarmlandSlab.MOISTURE) > 0 ? 3.0F : 1.0F;
				}
				if (x != 0 || z != 0) {
					bonus /= 4.0F;
				}
				chance += bonus;
			}
		}
		return chance;
	}

	@Override
	public boolean shouldOffset(LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		if (below.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			return below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
