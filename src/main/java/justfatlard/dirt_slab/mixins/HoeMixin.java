package justfatlard.dirt_slab.mixins;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import justfatlard.dirt_slab.Main;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(HoeItem.class)
public class HoeMixin {
	@Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
	private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info){
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();

		if(context.getSide() != Direction.DOWN && world.getBlockState(pos.up()).isAir()){
			BlockState state = world.getBlockState(pos);

			if(state.getBlock() == Main.COARSE_DIRT_SLAB){ // Coarse dirt slab to dirt slab
				PlayerEntity player = context.getPlayer();

				if(!world.isClient){
					world.setBlockState(pos, Main.DIRT_SLAB.getDefaultState().with(SlabBlock.TYPE, state.get(SlabBlock.TYPE)).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED)));

					if(player != null) context.getStack().damage(1, (LivingEntity)player, (Consumer<LivingEntity>)((playerEntity_1x) -> { (playerEntity_1x).sendToolBreakStatus(context.getHand()); }));
				}

				else 	world.playSound(player, pos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);

				info.setReturnValue(ActionResult.SUCCESS);
			}

			else if(state.getBlock() == Main.DIRT_SLAB || state.getBlock() == Main.GRASS_SLAB || state.getBlock() == Main.GRASS_PATH_SLAB){
				PlayerEntity player = context.getPlayer();

				if(!world.isClient){
					world.setBlockState(pos, Main.FARMLAND_SLAB.getDefaultState().with(SlabBlock.TYPE, state.get(SlabBlock.TYPE)).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED)));

					if(player != null) context.getStack().damage(1, (LivingEntity)player, (Consumer<LivingEntity>)((playerEntity_1x) -> { (playerEntity_1x).sendToolBreakStatus(context.getHand()); }));
				}

				else world.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);

				info.setReturnValue(ActionResult.SUCCESS);
			}
		}
	}
}