package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.state.property.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SlabStemBlock extends PlantBlock implements Fertilizable {
	public static final MapCodec<SlabStemBlock> CODEC = createCodec(SlabStemBlock::new);
	public static final int MAX_AGE = 7;
	public static final IntProperty AGE = Properties.AGE_7;
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");

	// Normal shapes (same as vanilla StemBlock)
	private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[]{
		Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 2.0, 9.0),   // age 0
		Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 4.0, 9.0),   // age 1
		Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 6.0, 9.0),   // age 2
		Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 8.0, 9.0),   // age 3
		Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 10.0, 9.0),  // age 4
		Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 12.0, 9.0),  // age 5
		Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 14.0, 9.0),  // age 6
		Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)   // age 7
	};

	// Offset shapes for bottom slab placement (8 pixels lower)
	private static final VoxelShape[] OFFSET_AGE_TO_SHAPE = new VoxelShape[]{
		Block.createCuboidShape(7.0, -8.0, 7.0, 9.0, -6.0, 9.0),   // age 0
		Block.createCuboidShape(7.0, -8.0, 7.0, 9.0, -4.0, 9.0),   // age 1
		Block.createCuboidShape(7.0, -8.0, 7.0, 9.0, -2.0, 9.0),   // age 2
		Block.createCuboidShape(7.0, -8.0, 7.0, 9.0, 0.0, 9.0),    // age 3
		Block.createCuboidShape(7.0, -8.0, 7.0, 9.0, 2.0, 9.0),    // age 4
		Block.createCuboidShape(7.0, -8.0, 7.0, 9.0, 4.0, 9.0),    // age 5
		Block.createCuboidShape(7.0, -8.0, 7.0, 9.0, 6.0, 9.0),    // age 6
		Block.createCuboidShape(7.0, -8.0, 7.0, 9.0, 8.0, 9.0)     // age 7
	};

	private final ItemConvertible pickBlockItem;
	private final boolean isMelon;

	public SlabStemBlock(boolean isMelon, ItemConvertible pickBlockItem, Settings settings) {
		super(settings);
		this.isMelon = isMelon;
		this.pickBlockItem = pickBlockItem;
		this.setDefaultState(this.stateManager.getDefaultState().with(AGE, 0).with(BOTTOM_OFFSET, false));
	}

	public SlabStemBlock(Settings settings) {
		this(true, null, settings);
	}

	@Override
	protected MapCodec<? extends PlantBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AGE, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(BOTTOM_OFFSET)) {
			return OFFSET_AGE_TO_SHAPE[state.get(AGE)];
		}
		return AGE_TO_SHAPE[state.get(AGE)];
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		return floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB;
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos below = pos.down();
		BlockState floorState = world.getBlockState(below);
		return canPlantOnTop(floorState, world, below);
	}

	@Override
	protected boolean hasRandomTicks(BlockState state) {
		return true;
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		int currentAge = state.get(AGE);
		System.out.println("[SlabStem] randomTick at " + pos + " age=" + currentAge + " offset=" + state.get(BOTTOM_OFFSET));

		if (world.getBaseLightLevel(pos, 0) >= 9) {
			float growthChance = getGrowthChance(world, pos);
			if (random.nextInt((int)(25.0F / growthChance) + 1) == 0) {
				int age = state.get(AGE);
				if (age < MAX_AGE) {
					// Preserve BOTTOM_OFFSET when growing
					boolean offset = state.get(BOTTOM_OFFSET);
					world.setBlockState(pos, state.with(AGE, age + 1).with(BOTTOM_OFFSET, offset), Block.NOTIFY_LISTENERS);
				} else {
					// At max age, try to spawn fruit - try all directions with random start
					Direction[] horizontals = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
					int startIndex = random.nextInt(4);

					for (int i = 0; i < 4; i++) {
						Direction direction = horizontals[(startIndex + i) % 4];
						BlockPos fruitPos = pos.offset(direction);
						BlockPos groundPos = fruitPos.down();
						BlockState groundState = world.getBlockState(groundPos);

						// Debug logging
						System.out.println("[SlabStem] Checking " + direction + ": fruitPos=" + fruitPos + " groundPos=" + groundPos);
						System.out.println("[SlabStem]   fruitPos block: " + world.getBlockState(fruitPos).getBlock());
						System.out.println("[SlabStem]   groundPos block: " + groundState.getBlock());
						System.out.println("[SlabStem]   canSpawn=" + canSpawnFruitAt(world, fruitPos) + " validGround=" + isValidGourdGround(groundState));

						// Check standard position (same level as stem, ground below)
						if (canSpawnFruitAt(world, fruitPos) && isValidGourdGround(groundState)) {
							// Spawn the fruit
							Block gourdBlock = isMelon ? Blocks.MELON : Blocks.PUMPKIN;
							world.setBlockState(fruitPos, gourdBlock.getDefaultState(), Block.NOTIFY_ALL);

							// Convert stem to attached stem pointing toward the fruit
							// Use slab attached stems to preserve BOTTOM_OFFSET
							boolean offset = state.get(BOTTOM_OFFSET);
							Block attachedStemBlock = isMelon ? DirtSlabBlocks.ATTACHED_MELON_STEM_SLAB : DirtSlabBlocks.ATTACHED_PUMPKIN_STEM_SLAB;
							world.setBlockState(pos, attachedStemBlock.getDefaultState()
								.with(SlabAttachedStemBlock.FACING, direction)
								.with(SlabAttachedStemBlock.BOTTOM_OFFSET, offset), Block.NOTIFY_LISTENERS);
							System.out.println("[SlabStem] Spawned fruit at " + fruitPos);
							break;
						}

						// Also check one level down for bottom slab configurations
						BlockPos lowerFruitPos = fruitPos.down();
						BlockPos lowerGroundPos = lowerFruitPos.down();
						BlockState lowerGroundState = world.getBlockState(lowerGroundPos);

						if (canSpawnFruitAt(world, lowerFruitPos) && isValidGourdGround(lowerGroundState)) {
							Block gourdBlock = isMelon ? Blocks.MELON : Blocks.PUMPKIN;
							world.setBlockState(lowerFruitPos, gourdBlock.getDefaultState(), Block.NOTIFY_ALL);

							// Use slab attached stems to preserve BOTTOM_OFFSET
							boolean offset = state.get(BOTTOM_OFFSET);
							Block attachedStemBlock = isMelon ? DirtSlabBlocks.ATTACHED_MELON_STEM_SLAB : DirtSlabBlocks.ATTACHED_PUMPKIN_STEM_SLAB;
							world.setBlockState(pos, attachedStemBlock.getDefaultState()
								.with(SlabAttachedStemBlock.FACING, direction)
								.with(SlabAttachedStemBlock.BOTTOM_OFFSET, offset), Block.NOTIFY_LISTENERS);
							System.out.println("[SlabStem] Spawned fruit at lower pos " + lowerFruitPos);
							break;
						}
					}
				}
			}
		}
	}

	private boolean isValidGourdGround(BlockState groundState) {
		Block block = groundState.getBlock();

		// Full blocks are always valid
		if (block == Blocks.FARMLAND ||
			block == Blocks.DIRT ||
			block == Blocks.COARSE_DIRT ||
			block == Blocks.PODZOL ||
			block == Blocks.GRASS_BLOCK ||
			block == Blocks.MOSS_BLOCK ||
			block == Blocks.MUD ||
			block == Blocks.MUDDY_MANGROVE_ROOTS ||
			block == Blocks.MYCELIUM ||
			block == Blocks.ROOTED_DIRT) {
			return true;
		}

		// Slabs are only valid if they're TOP or DOUBLE (not BOTTOM)
		// because melons can't sit on the surface of a bottom slab
		if (block == DirtSlabBlocks.FARMLAND_SLAB ||
			block == DirtSlabBlocks.DIRT_SLAB ||
			block == DirtSlabBlocks.COARSE_DIRT_SLAB ||
			block == DirtSlabBlocks.PODZOL_SLAB ||
			block == DirtSlabBlocks.GRASS_SLAB ||
			block == DirtSlabBlocks.MUD_SLAB ||
			block == DirtSlabBlocks.MYCELIUM_SLAB ||
			block == DirtSlabBlocks.ROOTED_DIRT_SLAB) {
			SlabType slabType = groundState.get(SlabBlock.TYPE);
			return slabType == SlabType.TOP || slabType == SlabType.DOUBLE;
		}

		return false;
	}

	private boolean canSpawnFruitAt(ServerWorld world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		return state.isAir() || state.isReplaceable();
	}

	// Fertilizable interface methods
	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
		return state.get(AGE) < MAX_AGE;
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		int newAge = Math.min(state.get(AGE) + random.nextBetween(2, 5), MAX_AGE);
		// Preserve BOTTOM_OFFSET when growing via bonemeal
		boolean offset = state.get(BOTTOM_OFFSET);
		world.setBlockState(pos, state.with(AGE, newAge).with(BOTTOM_OFFSET, offset), Block.NOTIFY_LISTENERS);
	}

	private float getGrowthChance(ServerWorld world, BlockPos pos) {
		float chance = 1.0F;
		BlockPos floorPos = pos.down();
		for (int x = -1; x <= 1; ++x) {
			for (int z = -1; z <= 1; ++z) {
				float bonus = 0.0F;
				BlockState floor = world.getBlockState(floorPos.add(x, 0, z));
				if (floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
					bonus = 1.0F;
				}
				if (x != 0 || z != 0) {
					bonus /= 4.0F;
				}
				chance += bonus;
			}
		}
		return chance;
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (below.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
