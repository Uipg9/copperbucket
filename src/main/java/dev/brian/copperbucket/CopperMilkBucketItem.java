package dev.brian.copperbucket;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Milk in a copper bucket: drinking it clears effects like vanilla milk, then costs 1 durability. */
public class CopperMilkBucketItem extends Item {
	public CopperMilkBucketItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		int damageBefore = stack.getDamageValue();
		ItemStack empty = stack.transmuteCopy(ModItems.COPPER_BUCKET);
		// The consumable component applies the milk effect and stats, then shrinks the stack by one.
		ItemStack remaining = super.finishUsingItem(stack, level, entity);
		if (!(entity instanceof Player player) || player.hasInfiniteMaterials()) {
			return remaining;
		}
		DurabilityPolicy.UseOutcome outcome = DurabilityPolicy.afterUse(damageBefore, false);
		if (outcome.breaks()) {
			player.onEquippedItemBroken(empty, player.getUsedItemHand().asEquipmentSlot());
			return ItemStack.EMPTY;
		}
		empty.set(DataComponents.DAMAGE, outcome.newDamage());
		return empty;
	}
}
