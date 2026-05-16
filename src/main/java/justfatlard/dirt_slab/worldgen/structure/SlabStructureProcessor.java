package justfatlard.dirt_slab.worldgen.structure;

import com.mojang.serialization.MapCodec;

import justfatlard.dirt_slab.DirtSlab;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

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
	public StructureTemplate.StructureBlockInfo processBlock(
			LevelReader world,
			BlockPos pos,
			BlockPos pivot,
			StructureTemplate.StructureBlockInfo originalBlockInfo,
			StructureTemplate.StructureBlockInfo currentBlockInfo,
			StructurePlaceSettings data) {

		BlockState state = currentBlockInfo.state();
		Block block = state.getBlock();
		RandomSource random = data.getRandom(currentBlockInfo.pos());

		// Check if this block can be converted and roll the dice
		if (random.nextFloat() < CONVERSION_CHANCE) {
			BlockState slabState = getSlabState(block);
			if (slabState != null) {
				return new StructureTemplate.StructureBlockInfo(
					currentBlockInfo.pos(),
					slabState.setValue(SlabBlock.TYPE, SlabType.BOTTOM),
					currentBlockInfo.nbt()
				);
			}
		}

		return currentBlockInfo;
	}

	private BlockState getSlabState(Block block) {
		return SlabRegistry.getTerrainSlabState(block);
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return TYPE;
	}

	public static void register() {
		TYPE = Registry.register(
			BuiltInRegistries.STRUCTURE_PROCESSOR,
			Identifier.fromNamespaceAndPath(DirtSlab.MOD_ID, "slab_processor"),
			() -> CODEC
		);
	}
}
