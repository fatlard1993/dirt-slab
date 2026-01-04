package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SlabSmallDripleafBlock extends TallPlantBlock implements Fertilizable {
	@SuppressWarnings("unchecked")
	public static final MapCodec<TallPlantBlock> CODEC = (MapCodec<TallPlantBlock>)(MapCodec<?>)createCodec(SlabSmallDripleafBlock::new);
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");

	private static final VoxelShape NORMAL_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(2.0, -8.0, 2.0, 14.0, 8.0, 14.0);

	public SlabSmallDripleafBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(HALF, DoubleBlockHalf.LOWER)
			.with(BOTTOM_OFFSET, false));
	}

	@Override
	public MapCodec<TallPlantBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(HALF, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return NORMAL_SHAPE;
	}

	@Override
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(Items.SMALL_DRIPLEAF);
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		// Small dripleaf can grow on clay, moss, and mud
		return floor.isOf(Blocks.CLAY) ||
			   floor.isOf(Blocks.MOSS_BLOCK) ||
			   floor.isOf(Blocks.MUD) ||
			   floor.getBlock() == DirtSlabBlocks.MUD_SLAB ||
			   Main.isGrassType(floor.getBlock());
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		if (state.get(HALF) == DoubleBlockHalf.LOWER) {
			BlockPos upperPos = pos.up();
			boolean isOffset = state.get(BOTTOM_OFFSET);
			BlockState upperState = this.getDefaultState()
				.with(HALF, DoubleBlockHalf.UPPER)
				.with(BOTTOM_OFFSET, isOffset);
			world.setBlockState(upperPos, upperState, Block.NOTIFY_ALL);
		}
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (Main.isAnySlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	// Fertilizable - grows into big dripleaf
	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		// When fertilized, grows into big dripleaf (vanilla behavior)
		// For simplicity, we convert to vanilla big dripleaf
		if (state.get(HALF) == DoubleBlockHalf.UPPER) {
			pos = pos.down();
			state = world.getBlockState(pos);
		}
		// Clear both halves
		world.setBlockState(pos, Blocks.AIR.getDefaultState());
		world.setBlockState(pos.up(), Blocks.AIR.getDefaultState());
		// Place vanilla big dripleaf (it handles its own multi-block structure)
		Blocks.BIG_DRIPLEAF.getDefaultState().with(HALF, DoubleBlockHalf.LOWER);
		// Actually use the vanilla placement
		world.setBlockState(pos, Blocks.BIG_DRIPLEAF_STEM.getDefaultState());
		world.setBlockState(pos.up(), Blocks.BIG_DRIPLEAF.getDefaultState());
	}
}
