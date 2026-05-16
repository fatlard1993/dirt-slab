package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabLeafLitterBlock extends VegetationBlock implements OffsetableSlab {
	public static final MapCodec<SlabLeafLitterBlock> CODEC = simpleCodec(SlabLeafLitterBlock::new);
	public static final int MIN_AMOUNT = 1;
	public static final int MAX_AMOUNT = 4;
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final IntegerProperty SEGMENT_AMOUNT = BlockStateProperties.SEGMENT_AMOUNT;

	// Normal shape (leaf litter is very flat)
	private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.box(0.0, -8.0, 0.0, 16.0, -7.0, 16.0);

	public SlabLeafLitterBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(SEGMENT_AMOUNT, 1)
			.setValue(BOTTOM_OFFSET, false));
	}

	@Override
	protected MapCodec<? extends VegetationBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, SEGMENT_AMOUNT, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		return SlabRegistry.isPlantable(floor.getBlock());
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos groundPos = pos.below();
		BlockState groundState = world.getBlockState(groundPos);
		return mayPlaceOn(groundState, world, groundPos);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		int currentAmount = state.getValue(SEGMENT_AMOUNT);
		ItemStack stack = player.getMainHandItem();

		// Allow adding more leaf litter if player is holding leaf litter item
		if (stack.is(Items.LEAF_LITTER) && currentAmount < MAX_AMOUNT) {
			world.setBlock(pos, state.setValue(SEGMENT_AMOUNT, currentAmount + 1), Block.UPDATE_ALL);
			if (!player.isCreative()) {
				stack.shrink(1);
			}
			world.playSound(player, pos, SoundEvents.MOSS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}
}
