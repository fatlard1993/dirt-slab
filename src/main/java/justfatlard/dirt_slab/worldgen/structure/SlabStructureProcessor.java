package justfatlard.dirt_slab.worldgen.structure;

import com.mojang.serialization.MapCodec;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;

/**
 * Structure processor that randomly converts some full blocks to slab variants
 * in village generation for a more natural, weathered look.
 */
public class SlabStructureProcessor extends StructureProcessor {
	public static final MapCodec<SlabStructureProcessor> CODEC = MapCodec.unit(SlabStructureProcessor::new);

	public static StructureProcessorType<SlabStructureProcessor> TYPE;

	private static final float CONVERSION_CHANCE = 0.15f; // 15% chance to convert

	public SlabStructureProcessor() {
	}

	@Override
	public StructureTemplate.StructureBlockInfo process(
			WorldView world,
			BlockPos pos,
			BlockPos pivot,
			StructureTemplate.StructureBlockInfo originalBlockInfo,
			StructureTemplate.StructureBlockInfo currentBlockInfo,
			StructurePlacementData data) {

		BlockState state = currentBlockInfo.state();
		Block block = state.getBlock();
		Random random = data.getRandom(currentBlockInfo.pos());

		// Check if this block can be converted and roll the dice
		if (random.nextFloat() < CONVERSION_CHANCE) {
			BlockState slabState = getSlabState(block);
			if (slabState != null) {
				return new StructureTemplate.StructureBlockInfo(
					currentBlockInfo.pos(),
					slabState.with(SlabBlock.TYPE, SlabType.BOTTOM),
					currentBlockInfo.nbt()
				);
			}
		}

		return currentBlockInfo;
	}

	private BlockState getSlabState(Block block) {
		// Convert dirt-related blocks to their slab variants
		if (block == Blocks.GRASS_BLOCK) return DirtSlabBlocks.GRASS_SLAB.getDefaultState();
		if (block == Blocks.DIRT) return DirtSlabBlocks.DIRT_SLAB.getDefaultState();
		if (block == Blocks.COARSE_DIRT) return DirtSlabBlocks.COARSE_DIRT_SLAB.getDefaultState();
		if (block == Blocks.DIRT_PATH) return DirtSlabBlocks.GRASS_PATH_SLAB.getDefaultState();
		if (block == Blocks.FARMLAND) return DirtSlabBlocks.FARMLAND_SLAB.getDefaultState();
		if (block == Blocks.PODZOL) return DirtSlabBlocks.PODZOL_SLAB.getDefaultState();

		return null;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return TYPE;
	}

	public static void register() {
		TYPE = Registry.register(
			Registries.STRUCTURE_PROCESSOR,
			Identifier.of(Main.MOD_ID, "slab_processor"),
			() -> CODEC
		);
	}
}
