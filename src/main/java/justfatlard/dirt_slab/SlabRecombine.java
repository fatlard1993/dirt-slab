package justfatlard.dirt_slab;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Two halves of the same ground make the ground again.
 *
 * <p>A slab laid on a slab is a double slab everywhere else in the game, and for stone that is the
 * right answer: there is no other block it could be. A terrain slab has one. Two dirt slabs are
 * dirt, and leaving them as a doubled slab gives a block that looks like dirt, breaks like dirt and
 * is not dirt - so grass will not spread onto it, a shovel will not path it, and nothing that asks
 * the world for {@code minecraft:dirt} finds it. The half that was cut goes back.
 *
 * <p>Only a slab onto its own kind. Dirt on grass is two different grounds and stays two slabs;
 * {@link SlabRegistry#getFullBlockState} decides what a kind turns back into, and a slab it does
 * not know is left alone.
 */
public final class SlabRecombine {
	private SlabRecombine() {}

	public static void register() {
		UseBlockCallback.EVENT.register(SlabRecombine::onUseBlock);
	}

	private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand,
			BlockHitResult hit) {
		if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
		if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

		BlockPos pos = hit.getBlockPos();
		BlockState existing = level.getBlockState(pos);
		if (!existing.hasProperty(SlabBlock.TYPE)) return InteractionResult.PASS;
		if (existing.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) return InteractionResult.PASS;

		BlockState whole = SlabRegistry.getFullBlockState(existing.getBlock());
		if (whole == null) return InteractionResult.PASS;

		ItemStack held = player.getItemInHand(hand);
		if (!(held.getItem() instanceof BlockItem item)) return InteractionResult.PASS;
		if (item.getBlock() != existing.getBlock()) return InteractionResult.PASS;

		if (!fillsEmptyHalf(existing.getValue(SlabBlock.TYPE), pos, hit)) return InteractionResult.PASS;

		serverLevel.setBlockAndUpdate(pos, whole);

		var sound = whole.getSoundType();
		level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS,
			(sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
		if (!player.isCreative()) held.shrink(1);

		return InteractionResult.SUCCESS;
	}

	/**
	 * Whether this click means "into the empty half", by vanilla's own rule.
	 *
	 * <p>The same test {@code SlabBlock.canBeReplaced} makes, so a click that vanilla would have
	 * turned into a double slab is exactly the click that lands here, and every other click is
	 * left to place a slab in the next block along the way it always did.
	 */
	private static boolean fillsEmptyHalf(SlabType type, BlockPos pos, BlockHitResult hit) {
		boolean aboveMiddle = hit.getLocation().y - pos.getY() > 0.5;
		var face = hit.getDirection();
		if (type == SlabType.BOTTOM) {
			return face == net.minecraft.core.Direction.UP
				|| (aboveMiddle && face.getAxis().isHorizontal());
		}
		return face == net.minecraft.core.Direction.DOWN
			|| (!aboveMiddle && face.getAxis().isHorizontal());
	}
}
