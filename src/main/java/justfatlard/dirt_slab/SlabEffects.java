package justfatlard.dirt_slab;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Shared particle and world-effect utilities for slab blocks.
 */
public class SlabEffects {
	public static void spawnParticles(World world, ParticleEffect particle, BlockPos pos, double yOffset, int count){
		if(!world.isClient()) ((ServerWorld) world).spawnParticles(particle, pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5, MathHelper.nextInt(world.random, 1, count), 0.25, 0.02, 0.25, 0.1);
	}

	public static void dirtParticles(World world, BlockPos pos, int count){
		spawnParticles(world, ParticleTypes.MYCELIUM, pos, 1, count);
	}

	public static void waterParticles(World world, BlockPos pos, int count){
		spawnParticles(world, ParticleTypes.SPLASH, pos, 1, count);
	}

	public static void happyParticles(World world, BlockPos pos, int count){
		spawnParticles(world, ParticleTypes.HAPPY_VILLAGER, pos, 0.5, count);
	}

	public static void setToDirt(World world, BlockPos pos){
		BlockState state = world.getBlockState(pos);

		if(state.getBlock() instanceof SlabBlock) world.setBlockState(pos, SlabRegistry.copySlabProperties(state, DirtSlabBlocks.DIRT_SLAB));

		else world.setBlockState(pos, Blocks.DIRT.getDefaultState());

		dirtParticles(world, pos, 3);
	}
}
