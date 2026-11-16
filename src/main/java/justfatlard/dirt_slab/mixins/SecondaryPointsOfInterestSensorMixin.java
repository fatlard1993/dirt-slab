package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import justfatlard.dirt_slab.FarmlandSlab;
import net.minecraft.world.entity.ai.sensing.SecondaryPoiSensor;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

@Mixin(SecondaryPoiSensor.class)
public class SecondaryPointsOfInterestSensorMixin {

	// A farmer counts farmland slabs as the farmland vanilla is already looking for around it
	@WrapOperation(method = "doTick", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableSet;contains(Ljava/lang/Object;)Z"))
	private boolean addFarmlandSlabToSecondaryJobSites(ImmutableSet<?> secondaryPoi, Object block, Operation<Boolean> original, @Local(argsOnly = true) Villager villagerEntity) {
		if (original.call(secondaryPoi, block)) return true;

		return block instanceof FarmlandSlab && villagerEntity.getVillagerData().profession().is(VillagerProfession.FARMER);
	}
}
