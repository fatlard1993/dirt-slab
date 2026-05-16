package justfatlard.dirt_slab.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.FarmlandSlab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SecondaryPoiSensor;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

@Mixin(SecondaryPoiSensor.class)
public class SecondaryPointsOfInterestSensorMixin {

	// Inject at the end of the sense method to add FarmlandSlab positions for farmer villagers
	@Inject(at = @At("TAIL"), method = "doTick")
	private void addFarmlandSlabToSecondaryJobSites(ServerLevel serverWorld, Villager villagerEntity, CallbackInfo ci) {
		// Only process for farmer villagers
		if (!villagerEntity.getVillagerData().profession().is(VillagerProfession.FARMER)) {
			return;
		}

		ResourceKey<Level> registryKey = serverWorld.dimension();
		BlockPos blockPos = villagerEntity.blockPosition();
		Brain<?> brain = villagerEntity.getBrain();

		// Get existing secondary job sites or create a new list
		List<GlobalPos> existingSites = brain.getMemory(MemoryModuleType.SECONDARY_JOB_SITE)
			.map(list -> new java.util.ArrayList<>(list))
			.orElse(new java.util.ArrayList<>());

		// Scan for FarmlandSlab blocks
		for (int j = -4; j <= 4; j++) {
			for (int k = -2; k <= 2; k++) {
				for (int l = -4; l <= 4; l++) {
					BlockPos blockPos2 = blockPos.offset(j, k, l);
					Block block = serverWorld.getBlockState(blockPos2).getBlock();

					if (block instanceof FarmlandSlab) {
						GlobalPos globalPos = GlobalPos.of(registryKey, blockPos2);
						if (!existingSites.contains(globalPos)) {
							existingSites.add(globalPos);
						}
					}
				}
			}
		}

		// Update the memory if we found any sites
		if (!existingSites.isEmpty()) {
			brain.setMemory(MemoryModuleType.SECONDARY_JOB_SITE, existingSites);
		}
	}
}
