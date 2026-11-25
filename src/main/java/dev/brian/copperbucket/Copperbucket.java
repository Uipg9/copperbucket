package dev.brian.copperbucket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Copperbucket implements ModInitializer {
	public static final String MOD_ID = "copperbucket";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.initialize();
		UseEntityCallback.EVENT.register(Copperbucket::milkCow);
	}

	/** Cows only accept the vanilla bucket by identity, so copper buckets are handled here. */
	private static InteractionResult milkCow(Player player, Level level, InteractionHand hand, Entity entity,
			EntityHitResult hit) {
		ItemStack held = player.getItemInHand(hand);
		if (!held.is(ModItems.COPPER_BUCKET) || !(entity instanceof AbstractCow cow) || cow.isBaby()
				|| player.isSpectator()) {
			return InteractionResult.PASS;
		}
		player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
		// Both sides, like vanilla Cow#mobInteract, so the client does not briefly disagree with the server.
		ItemStack filled = held.transmuteCopy(ModItems.COPPER_MILK_BUCKET);
		player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, filled));
		return InteractionResult.SUCCESS;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	/** Translation key following the vanilla convention: {@code category.modid.path}. */
	public static String langKey(String category, String path) {
		return category + "." + MOD_ID + "." + path;
	}
}
