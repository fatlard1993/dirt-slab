package justfatlard.dirt_slab;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.block.SlabBlock;
import net.minecraft.world.tick.ScheduledTickView;

public class SpreadableSlab extends SlabBlock {
	public final Block baseBlock;
	public static final EnumProperty<SlabType> TYPE;
	public static final BooleanProperty WATERLOGGED;
	public static final BooleanProperty SNOWY;

	public SpreadableSlab(Settings settings, Block baseBlock){
		super(settings);

		this.baseBlock = baseBlock;

		this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState().with(TYPE, SlabType.BOTTOM)).with(WATERLOGGED, false)).with(SNOWY, false));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState blockState, World world, BlockPos blockPos, Random random){
		this.baseBlock.randomDisplayTick(blockState, world, blockPos, random);
	}

	@Override
	protected void scheduledTick(BlockState spreader, ServerWorld world, BlockPos pos, Random random){
		if(!canSurvive(spreader, world, pos)) Main.setToDirt(world, pos);

		else Main.spreadableTick(spreader, world, pos, random);
	}

	public static boolean canSurvive(BlockState state, WorldView world, BlockPos pos){
		BlockPos posUp = pos.up();
		BlockState topBlock = world.getBlockState(posUp);

		if(topBlock.getBlock() == Blocks.SNOW && (Integer)topBlock.get(SnowBlock.LAYERS) == 1) return true;
		if(topBlock.isOf(DirtSlabBlocks.SNOW_LAYER_SLAB) && topBlock.get(SlabSnowLayerBlock.LAYERS) == 1) return true;

		else if(state.getBlock() instanceof SpreadableSlab && !topBlock.isSolid() && state.get(TYPE) == SlabType.TOP) return true;

		else {
			int i = ChunkLightProvider.getRealisticOpacity(state, topBlock, Direction.UP, topBlock.getOpacity());

			return i < 15; // Max light level in Minecraft
		}
	}

	public static boolean canSpread(BlockState state, WorldView world, BlockPos pos){
		return canSurvive(state, world, pos) && !world.getFluidState(pos.up()).isIn(FluidTags.WATER) && !(state.getBlock() instanceof SpreadableSlab && state.get(WATERLOGGED) && state.get(TYPE) == SlabType.BOTTOM);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random){
		if(direction == Direction.UP && !state.canPlaceAt(world, pos)) tickView.scheduleBlockTick(pos, this, 1);

		state = super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);

		// Only update snowy when the block above changes (Direction.UP)
		if(direction == Direction.UP){
			return (BlockState)state.with(SNOWY, isSnow(neighborState));
		}
		return state;
	}

	private static boolean isSnow(BlockState state){
		Block block = state.getBlock();
		return block == Blocks.SNOW || block == Blocks.SNOW_BLOCK || state.isOf(DirtSlabBlocks.SNOW_LAYER_SLAB);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx){
		BlockState topState = ctx.getWorld().getBlockState(ctx.getBlockPos().up());

		return (BlockState)(!this.getDefaultState().canPlaceAt(ctx.getWorld(), ctx.getBlockPos()) ? pushEntitiesUpBeforeBlockChange(this.getDefaultState(), DirtSlabBlocks.DIRT_SLAB.getDefaultState(), ctx.getWorld(), ctx.getBlockPos()) : super.getPlacementState(ctx)).with(SNOWY, isSnow(topState));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){ builder.add(TYPE, WATERLOGGED, SNOWY); }

	static {
		TYPE = Properties.SLAB_TYPE;
		WATERLOGGED = Properties.WATERLOGGED;
		SNOWY = Properties.SNOWY;
	}
}
