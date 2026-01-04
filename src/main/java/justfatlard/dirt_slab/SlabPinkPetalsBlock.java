package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SlabPinkPetalsBlock extends PlantBlock {
	public static final MapCodec<SlabPinkPetalsBlock> CODEC = createCodec(SlabPinkPetalsBlock::new);
	public static final int MIN_AMOUNT = 1;
	public static final int MAX_AMOUNT = 4;
	public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
	public static final IntProperty FLOWER_AMOUNT = Properties.FLOWER_AMOUNT;
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");

	// Normal shape
	private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, -5.0, 16.0);

	public SlabPinkPetalsBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(FACING, Direction.NORTH)
			.with(FLOWER_AMOUNT, 1)
			.with(BOTTOM_OFFSET, false));
	}

	@Override
	protected MapCodec<? extends PlantBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, FLOWER_AMOUNT, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		return Main.isGrassType(floor.getBlock()) ||
			   floor.getBlock() == DirtSlabBlocks.MUD_SLAB ||
			   floor.getBlock() == DirtSlabBlocks.ROOTED_DIRT_SLAB;
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos groundPos = pos.down();
		BlockState groundState = world.getBlockState(groundPos);
		return canPlantOnTop(groundState, world, groundPos);
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (Main.isAnySlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		int currentAmount = state.get(FLOWER_AMOUNT);
		ItemStack stack = player.getMainHandStack();

		// Allow adding more petals if player is holding pink petals item
		if (stack.isOf(Items.PINK_PETALS) && currentAmount < MAX_AMOUNT) {
			world.setBlockState(pos, state.with(FLOWER_AMOUNT, currentAmount + 1), Block.NOTIFY_ALL);
			if (!player.isCreative()) {
				stack.decrement(1);
			}
			world.playSound(player, pos, SoundEvents.BLOCK_PINK_PETALS_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
}
