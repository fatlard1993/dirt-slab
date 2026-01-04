package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class SlabSweetBerryBushBlock extends Block implements Fertilizable {
	public static final MapCodec<SlabSweetBerryBushBlock> CODEC = createCodec(SlabSweetBerryBushBlock::new);
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");
	public static final IntProperty AGE = Properties.AGE_3;

	private static final VoxelShape SMALL_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
	private static final VoxelShape LARGE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
	private static final VoxelShape SMALL_SHAPE_OFFSET = Block.createCuboidShape(3.0, -8.0, 3.0, 13.0, 0.0, 13.0);
	private static final VoxelShape LARGE_SHAPE_OFFSET = Block.createCuboidShape(1.0, -8.0, 1.0, 15.0, 8.0, 15.0);

	public SlabSweetBerryBushBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(BOTTOM_OFFSET, false).with(AGE, 0));
	}

	@Override
	protected MapCodec<SlabSweetBerryBushBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET, AGE);
	}

	@Override
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(Items.SWEET_BERRIES);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		boolean offset = state.get(BOTTOM_OFFSET);
		if (state.get(AGE) == 0) {
			return offset ? SMALL_SHAPE_OFFSET : SMALL_SHAPE;
		}
		return offset ? LARGE_SHAPE_OFFSET : LARGE_SHAPE;
	}

	@Override
	protected boolean hasRandomTicks(BlockState state) {
		return state.get(AGE) < 3;
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		int age = state.get(AGE);
		if (age < 3 && random.nextInt(5) == 0 && world.getBaseLightLevel(pos.up(), 0) >= 9) {
			world.setBlockState(pos, state.with(AGE, age + 1), Block.NOTIFY_LISTENERS);
		}
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean moving) {
		if (entity instanceof LivingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE) {
			entity.slowMovement(state, new Vec3d(0.8, 0.75, 0.8));
			if (!world.isClient() && state.get(AGE) > 0 && world instanceof ServerWorld serverWorld) {
				double dx = Math.abs(entity.getX() - entity.lastX);
				double dz = Math.abs(entity.getZ() - entity.lastZ);
				if (dx >= 0.003 || dz >= 0.003) {
					entity.damage(serverWorld, world.getDamageSources().sweetBerryBush(), 1.0F);
				}
			}
		}
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		int age = state.get(AGE);
		boolean mature = age == 3;
		if (age > 1) {
			int berryCount = 1 + world.random.nextInt(2);
			dropStack(world, pos, new ItemStack(Items.SWEET_BERRIES, berryCount + (mature ? 1 : 0)));
			world.playSound(null, pos, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 1.0F, 0.8F + world.random.nextFloat() * 0.4F);
			world.setBlockState(pos, state.with(AGE, 1), Block.NOTIFY_LISTENERS);
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		return Main.isGrassType(below.getBlock()) || Main.isAnySlab(below.getBlock()) ||
			   below.isOf(Blocks.GRASS_BLOCK) || below.isOf(Blocks.DIRT) || below.isOf(Blocks.COARSE_DIRT) ||
			   below.isOf(Blocks.PODZOL) || below.isOf(Blocks.FARMLAND);
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
		return state.get(AGE) < 3;
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		int newAge = Math.min(3, state.get(AGE) + 1);
		world.setBlockState(pos, state.with(AGE, newAge), Block.NOTIFY_LISTENERS);
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (Main.isAnySlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
