package dev.brian.copperbucket;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.material.Fluids;

public final class ModItems {
	public static final CopperBucketItem COPPER_BUCKET = register("copper_bucket",
		props -> new CopperBucketItem(Fluids.EMPTY, props));
	public static final CopperBucketItem COPPER_WATER_BUCKET = register("copper_water_bucket",
		props -> new CopperBucketItem(Fluids.WATER, props));
	public static final CopperBucketItem COPPER_LAVA_BUCKET = register("copper_lava_bucket",
		props -> new CopperBucketItem(Fluids.LAVA, props));
	public static final CopperMilkBucketItem COPPER_MILK_BUCKET = register("copper_milk_bucket",
		props -> new CopperMilkBucketItem(props.component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)));

	private ModItems() {
	}

	private static <T extends Item> T register(String path, Function<Item.Properties, T> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Copperbucket.id(path));
		Item.Properties properties = new Item.Properties()
			.setId(key)
			.durability(DurabilityPolicy.MAX_DURABILITY)
			.repairable(Items.COPPER_INGOT);
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties));
	}

	public static void initialize() {
		ResourceKey<CreativeModeTab> tools = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
			Identifier.withDefaultNamespace("tools_and_utilities"));
		CreativeModeTabEvents.modifyOutputEvent(tools).register(output -> output.insertAfter(
			Items.MILK_BUCKET, COPPER_BUCKET, COPPER_WATER_BUCKET, COPPER_LAVA_BUCKET, COPPER_MILK_BUCKET));
	}
}
