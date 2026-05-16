package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.gamerules.GameRules;

public class FarmlandSlab extends SlicedTopSlab {
	public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;

	protected FarmlandSlab(Properties settings){
		super(settings);

		this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, false).setValue(MOISTURE, 0));
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random){
		if(!state.canSurvive(world, pos)) SlabEffects.setToDirt(world, pos);

		else {
			int moisture = state.getValue(MOISTURE);

			if(!isWaterNearby(world, pos) && !world.isRainingAt(pos.above())){
				if(moisture > 0){
					SlabEffects.dirtParticles(world, pos, 1);

					world.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
				}

				else if(!hasCrop(world, pos)) SlabEffects.setToDirt(world, pos);
			}

			else if(moisture < 7){
				world.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 3, 0.25, 0.06, 0.25, 0.1);

				world.setBlock(pos, state.setValue(MOISTURE, 7), 2);
			}
		}
	}

	@Override
	public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, double distance){
		if(!world.isClientSide() && world.getRandom().nextFloat() < distance - 0.5F
			&& entity instanceof LivingEntity
			&& (entity instanceof Player || (world instanceof ServerLevel serverWorld && serverWorld.getGameRules().get(GameRules.MOB_GRIEFING)))
			&& entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512F
		){
			SlabEffects.setToDirt(world, pos);
		}

		super.fallOn(world, state, pos, entity, distance);
	}

	private static boolean hasCrop(BlockGetter world, BlockPos pos){
		Block block = world.getBlockState(pos.above()).getBlock();

		return block instanceof CropBlock || block instanceof StemBlock || block instanceof AttachedStemBlock
			|| block instanceof SlabStemBlock || block instanceof SlabAttachedStemBlock;
	}

	private static boolean isWaterNearby(LevelReader world, BlockPos pos){
		if(world.getBlockState(pos).getValue(WATERLOGGED)) return true;

		for(BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))){
			if(world.getFluidState(blockPos).is(FluidTags.WATER)) return true;
		}

		return false;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){ builder.add(TYPE, WATERLOGGED, MOISTURE); }
}
