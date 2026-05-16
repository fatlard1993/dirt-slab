package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabSweetBerryBushBlock extends Block implements BonemealableBlock, OffsetableSlab {
	public static final MapCodec<SlabSweetBerryBushBlock> CODEC = simpleCodec(SlabSweetBerryBushBlock::new);
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

	private static final VoxelShape SMALL_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
	private static final VoxelShape LARGE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
	private static final VoxelShape SMALL_SHAPE_OFFSET = Block.box(3.0, -8.0, 3.0, 13.0, 0.0, 13.0);
	private static final VoxelShape LARGE_SHAPE_OFFSET = Block.box(1.0, -8.0, 1.0, 15.0, 8.0, 15.0);

	public SlabSweetBerryBushBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(BOTTOM_OFFSET, false).setValue(AGE, 0));
	}

	@Override
	protected MapCodec<SlabSweetBerryBushBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET, AGE);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(Items.SWEET_BERRIES);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		boolean offset = state.getValue(BOTTOM_OFFSET);
		if (state.getValue(AGE) == 0) {
			return offset ? SMALL_SHAPE_OFFSET : SMALL_SHAPE;
		}
		return offset ? LARGE_SHAPE_OFFSET : LARGE_SHAPE;
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return state.getValue(AGE) < 3;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		int age = state.getValue(AGE);
		if (age < 3 && random.nextInt(5) == 0 && world.getRawBrightness(pos.above(), 0) >= 9) {
			world.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean moving) {
		if (entity instanceof LivingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE) {
			entity.makeStuckInBlock(state, new Vec3(0.8, 0.75, 0.8));
			if (!world.isClientSide() && state.getValue(AGE) > 0 && world instanceof ServerLevel serverWorld) {
				double dx = Math.abs(entity.getX() - entity.xo);
				double dz = Math.abs(entity.getZ() - entity.zo);
				if (dx >= 0.003 || dz >= 0.003) {
					entity.hurtServer(serverWorld, world.damageSources().sweetBerryBush(), 1.0F);
				}
			}
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		int age = state.getValue(AGE);
		boolean mature = age == 3;
		if (age > 1) {
			int berryCount = 1 + world.getRandom().nextInt(2);
			popResource(world, pos, new ItemStack(Items.SWEET_BERRIES, berryCount + (mature ? 1 : 0)));
			world.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + world.getRandom().nextFloat() * 0.4F);
			world.setBlock(pos, state.setValue(AGE, 1), Block.UPDATE_CLIENTS);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		return SlabRegistry.isGrassType(below.getBlock()) || SlabRegistry.isTerrainSlab(below.getBlock()) ||
			   below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT) || below.is(Blocks.COARSE_DIRT) ||
			   below.is(Blocks.PODZOL) || below.is(Blocks.FARMLAND);
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
		return state.getValue(AGE) < 3;
	}

	@Override
	public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
		int newAge = Math.min(3, state.getValue(AGE) + 1);
		world.setBlock(pos, state.setValue(AGE, newAge), Block.UPDATE_CLIENTS);
	}
}
