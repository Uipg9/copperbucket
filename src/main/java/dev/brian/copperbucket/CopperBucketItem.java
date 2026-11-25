package dev.brian.copperbucket;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Empty, water and lava copper buckets. Vanilla's {@code BucketItem.use} always hands back a vanilla
 * bucket (its empty-result helper is static), so both directions are reimplemented here to carry the
 * stack's damage across variants.
 */
public class CopperBucketItem extends BucketItem {
	public CopperBucketItem(Fluid fluid, Properties properties) {
		super(fluid, properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		BlockHitResult hit = getPlayerPOVHitResult(level, player, getFluidContext());
		if (hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResult.PASS;
		}
		BlockPos pos = hit.getBlockPos();
		Direction face = hit.getDirection();
		BlockPos adjacent = pos.relative(face);
		if (!level.mayInteract(player, pos) || !player.mayUseItemAt(adjacent, face, held)) {
			return InteractionResult.FAIL;
		}
		return content == Fluids.EMPTY
			? pickUp(level, player, held, pos)
			: placeContents(level, player, hand, held, pos, adjacent, hit);
	}

	private InteractionResult pickUp(Level level, Player player, ItemStack held, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		FluidState fluid = state.getFluidState();
		boolean supported = fluid.isSourceOfType(Fluids.WATER) || fluid.isSourceOfType(Fluids.LAVA);
		if (!supported || !(state.getBlock() instanceof BucketPickup pickup)) {
			return InteractionResult.FAIL;
		}
		ItemStack vanillaFilled = pickup.pickupBlock(player, level, pos, state);
		if (vanillaFilled.isEmpty()) {
			return InteractionResult.FAIL;
		}
		ItemStack filled;
		if (vanillaFilled.is(Items.WATER_BUCKET)) {
			filled = held.transmuteCopy(ModItems.COPPER_WATER_BUCKET);
		} else if (vanillaFilled.is(Items.LAVA_BUCKET)) {
			filled = held.transmuteCopy(ModItems.COPPER_LAVA_BUCKET);
		} else {
			return InteractionResult.FAIL;
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		pickup.getPickupSound().ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
		level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
		ItemStack result = ItemUtils.createFilledResult(held, player, filled);
		if (player instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.FILLED_BUCKET.trigger(serverPlayer, vanillaFilled);
		}
		return InteractionResult.SUCCESS.heldItemTransformedTo(result);
	}

	private InteractionResult placeContents(Level level, Player player, InteractionHand hand, ItemStack held,
			BlockPos pos, BlockPos adjacent, BlockHitResult hit) {
		BlockState clicked = level.getBlockState(pos);
		BlockPos target = clicked.getBlock() instanceof LiquidBlockContainer && content == Fluids.WATER ? pos : adjacent;
		if (!emptyContents(player, level, target, hit)) {
			return InteractionResult.FAIL;
		}
		checkExtraContent(player, level, held, target);
		if (player instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, target, held);
		}
		player.awardStat(Stats.ITEM_USED.get(this));

		boolean creative = player.hasInfiniteMaterials();
		DurabilityPolicy.UseOutcome outcome = DurabilityPolicy.afterUse(held.getDamageValue(), creative);
		ItemStack result;
		if (creative) {
			// Creative keeps the full bucket, exactly like vanilla.
			result = held;
		} else if (outcome.breaks()) {
			player.onEquippedItemBroken(held, hand.asEquipmentSlot());
			result = ItemStack.EMPTY;
		} else {
			result = held.transmuteCopy(ModItems.COPPER_BUCKET);
			result.set(DataComponents.DAMAGE, outcome.newDamage());
		}
		return InteractionResult.SUCCESS.heldItemTransformedTo(result);
	}
}
