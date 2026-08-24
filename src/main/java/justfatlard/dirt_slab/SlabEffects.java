package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

		BlockState becomes = state.getBlock() instanceof SlabBlock
			? SlabRegistry.copySlabProperties(state, DirtSlabBlocks.DIRT_SLAB)
			: Blocks.DIRT.defaultBlockState();

		// Through pushEntitiesUp, the way vanilla's own farmland does it. This runs from fallOn,
		// which is the middle of the landing entity's collision step: swapping the block out from
		// under it there and leaving it to sort itself out is how a player trampling farmland
		// ended up inside the block and fell through. Farmland is 7px and dirt is 8, so what it
		// actually needs is a nudge up, and this is the call that gives it one.
		world.setBlockAndUpdate(pos, Block.pushEntitiesUp(state, becomes, world, pos));

		dirtParticles(world, pos, 3);
	}
}
