package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared particle and world-effect utilities for slab blocks.
 */
public class SlabEffects {
	public static void spawnParticles(Level world, ParticleOptions particle, BlockPos pos, double yOffset, int count){
		if(!world.isClientSide()) ((ServerLevel) world).sendParticles(particle, pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5, Mth.nextInt(world.getRandom(), 1, count), 0.25, 0.02, 0.25, 0.1);
	}

	public static void dirtParticles(Level world, BlockPos pos, int count){
		spawnParticles(world, ParticleTypes.MYCELIUM, pos, 1, count);
	}

	public static void waterParticles(Level world, BlockPos pos, int count){
		spawnParticles(world, ParticleTypes.SPLASH, pos, 1, count);
	}

	public static void happyParticles(Level world, BlockPos pos, int count){
		spawnParticles(world, ParticleTypes.HAPPY_VILLAGER, pos, 0.5, count);
	}

	public static void setToDirt(Level world, BlockPos pos){
		BlockState state = world.getBlockState(pos);

		if(state.getBlock() instanceof SlabBlock) world.setBlockAndUpdate(pos, SlabRegistry.copySlabProperties(state, DirtSlabBlocks.DIRT_SLAB));

		else world.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());

		dirtParticles(world, pos, 3);
	}
}
