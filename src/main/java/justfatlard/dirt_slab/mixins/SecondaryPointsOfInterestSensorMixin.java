package justfatlard.dirt_slab.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.FarmlandSlab;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SecondaryPointsOfInterestSensor;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;

@Mixin(SecondaryPointsOfInterestSensor.class)
public class SecondaryPointsOfInterestSensorMixin {

	// Inject at the end of the sense method to add FarmlandSlab positions for farmer villagers
	@Inject(at = @At("TAIL"), method = "sense")
	private void addFarmlandSlabToSecondaryJobSites(ServerWorld serverWorld, VillagerEntity villagerEntity, CallbackInfo ci) {
		// Only process for farmer villagers
		if (!villagerEntity.getVillagerData().profession().matchesKey(VillagerProfession.FARMER)) {
			return;
		}

		RegistryKey<World> registryKey = serverWorld.getRegistryKey();
		BlockPos blockPos = villagerEntity.getBlockPos();
		Brain<?> brain = villagerEntity.getBrain();

		// Get existing secondary job sites or create a new list
		List<GlobalPos> existingSites = brain.getOptionalRegisteredMemory(MemoryModuleType.SECONDARY_JOB_SITE)
			.map(list -> new java.util.ArrayList<>(list))
			.orElse(new java.util.ArrayList<>());

		// Scan for FarmlandSlab blocks
		for (int j = -4; j <= 4; j++) {
			for (int k = -2; k <= 2; k++) {
				for (int l = -4; l <= 4; l++) {
					BlockPos blockPos2 = blockPos.add(j, k, l);
					Block block = serverWorld.getBlockState(blockPos2).getBlock();

					if (block instanceof FarmlandSlab) {
						GlobalPos globalPos = GlobalPos.create(registryKey, blockPos2);
						if (!existingSites.contains(globalPos)) {
							existingSites.add(globalPos);
						}
					}
				}
			}
		}

		// Update the memory if we found any sites
		if (!existingSites.isEmpty()) {
			brain.remember(MemoryModuleType.SECONDARY_JOB_SITE, existingSites);
		}
	}
}
