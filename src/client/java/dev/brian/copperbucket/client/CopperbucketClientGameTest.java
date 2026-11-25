package dev.brian.copperbucket.client;

import dev.brian.copperbucket.Copperbucket;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Blocks;

/**
 * In-game test: boots the client, joins a world, drives the copper buckets with real right-clicks
 * and asserts the resulting stacks, then takes screenshots.
 * Run with {@code .\gradlew.bat runClientGameTest}; screenshots land in
 * {@code build\run\clientGameTest\screenshots}.
 */
public class CopperbucketClientGameTest implements FabricClientGameTest {
	private static final BlockPos WATER_POS = new BlockPos(3, 101, 5);

	@Override
	public void runTest(ClientGameTestContext context) {
		if (!FabricLoader.getInstance().isModLoaded(Copperbucket.MOD_ID)) {
			throw new AssertionError(Copperbucket.MOD_ID + " is not loaded");
		}

		context.waitForScreen(TitleScreen.class);
		context.waitTicks(60); // let the menu fade-in finish so the screenshot is clean

		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			context.waitTicks(100);
			boolean inWorld = context.computeOnClient(mc -> mc.player != null && mc.level != null);
			if (!inWorld) {
				throw new AssertionError("player/level missing after world creation");
			}
			context.takeScreenshot("in_world");

			// (a) registration + recipe
			for (String path : new String[] {"copper_bucket", "copper_water_bucket", "copper_lava_bucket", "copper_milk_bucket"}) {
				if (!BuiltInRegistries.ITEM.containsKey(Copperbucket.id(path))) {
					throw new AssertionError("item not registered: " + path);
				}
			}
			int ingots = world.getServer().computeOnServer(server -> {
				RecipeHolder<?> holder = server.getRecipeManager()
					.byKey(ResourceKey.create(Registries.RECIPE, Copperbucket.id("copper_bucket")))
					.orElseThrow(() -> new AssertionError("recipe copperbucket:copper_bucket missing"));
				ShapedRecipe recipe = (ShapedRecipe) holder.value();
				return (int) recipe.getIngredients().stream().filter(java.util.Optional::isPresent).count();
			});
			expect(ingots == 3, "recipe should have 3 ingredients, had " + ingots);
				pass("registration and recipe");

			// Stage: stone platform, a water source in front of the player, survival, looking at the water.
			world.getServer().runCommand("gamemode survival @p");
			world.getServer().runCommand("fill 0 100 0 8 100 8 stone");
			world.getServer().runCommand("fill 0 101 0 8 104 8 air");
			world.getServer().runCommand("setblock 3 101 5 water");
			world.getServer().runCommand("tp @p 3.5 101 4.5 0 0");
			context.waitTicks(10);

			// (b) fill from the source: damage carries (0), water source is removed
			give(world, "copperbucket:copper_bucket");
			context.getInput().lookAt(WATER_POS);
			context.waitTicks(2);
			rightClick(context);
			expectHeld(world, "copperbucket:copper_water_bucket", 0, "after filling");
			expect(!isWater(world), "water source should be gone after pickup");
				pass("fill from source");

			// (c) empty it onto the ground: 1 damage, source placed again
			context.getInput().lookAt(new BlockPos(3, 100, 5));
			context.waitTicks(2);
			rightClick(context);
			expectHeld(world, "copperbucket:copper_bucket", 1, "after emptying");
			expect(isWater(world), "water source should exist after emptying");
				pass("empty onto ground");

			// (d) last durability point: the bucket breaks on the empty
			give(world, "copperbucket:copper_water_bucket[minecraft:damage=63]");
			world.getServer().runCommand("setblock 3 101 5 air");
			context.waitTicks(2);
			rightClick(context);
			expectHeld(world, "minecraft:air", 0, "after the 64th use broke the bucket");
				pass("break on last use");

			// lava round trip
			world.getServer().runCommand("fill 0 101 0 8 104 8 air");
			context.waitTicks(5); // drop leftover flowing water so it cannot turn the lava to obsidian
			world.getServer().runCommand("setblock 3 101 5 lava");
			give(world, "copperbucket:copper_bucket[minecraft:damage=2]");
			context.getInput().lookAt(WATER_POS);
			context.waitTicks(2);
			rightClick(context);
			expectHeld(world, "copperbucket:copper_lava_bucket", 2, "after filling with lava");
				pass("lava fill");

			// (e) milk: drink it (32 ticks), result is the empty bucket with one more damage
			world.getServer().runCommand("setblock 3 101 5 air");
			give(world, "copperbucket:copper_milk_bucket[minecraft:damage=5]");
			context.getInput().lookAt(0f, -90f);
			context.waitTicks(2);
			context.getInput().holdKeyFor(options -> options.keyUse, 45);
			context.waitTicks(5);
			expectHeld(world, "copperbucket:copper_bucket", 6, "after drinking milk");
				pass("drink milk");

				// cow milking: adult converts (free), baby does not
				world.getServer().runCommand("fill 0 101 0 8 104 8 air");
				world.getServer().runCommand("summon minecraft:cow 3.5 101 6.5 {NoAI:1b,Age:-24000}");
				give(world, "copperbucket:copper_bucket[minecraft:damage=3]");
				context.getInput().lookAt(new BlockPos(3, 101, 6));
				context.waitTicks(3);
				rightClick(context);
				expectHeld(world, "copperbucket:copper_bucket", 3, "after right-clicking a baby cow");
				pass("baby cow not milked");
				world.getServer().runCommand("kill @e[type=minecraft:cow]");
				world.getServer().runCommand("summon minecraft:cow 3.5 101 6.5 {NoAI:1b}");
				context.waitTicks(3);
				rightClick(context);
				expectHeld(world, "copperbucket:copper_milk_bucket", 3, "after milking an adult cow");
				pass("adult cow milked free");
				world.getServer().runCommand("kill @e[type=minecraft:cow]");

				// creative: placing water does not consume or damage the bucket
				world.getServer().runCommand("gamemode creative @p");
				give(world, "copperbucket:copper_water_bucket[minecraft:damage=10]");
				context.getInput().lookAt(new BlockPos(3, 100, 5));
				context.waitTicks(3);
				rightClick(context);
				expect(isWater(world), "creative: water should have been placed");
				expectHeld(world, "copperbucket:copper_water_bucket", 10, "creative placement");
				pass("creative placement keeps bucket");
				world.getServer().runCommand("gamemode survival @p");
				world.getServer().runCommand("setblock 3 101 5 air");
				context.waitTicks(5);

				// milk at the last point breaks the bucket
				give(world, "copperbucket:copper_milk_bucket[minecraft:damage=63]");
				context.getInput().lookAt(0f, -90f);
				context.waitTicks(2);
				context.getInput().holdKeyFor(options -> options.keyUse, 45);
				context.waitTicks(5);
				expectHeld(world, "minecraft:air", 0, "after drinking the last milk");
				pass("milk break");

				// water bucket at 62: empties to 63 (alive), refill and empty again breaks it
				give(world, "copperbucket:copper_water_bucket[minecraft:damage=62]");
				context.getInput().lookAt(new BlockPos(3, 100, 5));
				context.waitTicks(3);
				rightClick(context);
				expectHeld(world, "copperbucket:copper_bucket", 63, "after emptying at damage 62");
				context.getInput().lookAt(WATER_POS);
				context.waitTicks(3);
				rightClick(context);
				expectHeld(world, "copperbucket:copper_water_bucket", 63, "after refilling at damage 63");
				context.getInput().lookAt(new BlockPos(3, 100, 5));
				context.waitTicks(3);
				rightClick(context);
				expectHeld(world, "minecraft:air", 0, "after the final empty broke it");
				pass("durability 62 to break sequence");
				world.getServer().runCommand("setblock 3 101 5 air");

			// (f) screenshot with all four items in the hotbar
			world.getServer().runCommand("item replace entity @p hotbar.0 with copperbucket:copper_bucket");
			world.getServer().runCommand("item replace entity @p hotbar.1 with copperbucket:copper_water_bucket[minecraft:damage=10]");
			world.getServer().runCommand("item replace entity @p hotbar.2 with copperbucket:copper_lava_bucket[minecraft:damage=30]");
			world.getServer().runCommand("item replace entity @p hotbar.3 with copperbucket:copper_milk_bucket[minecraft:damage=50]");
			world.getServer().runCommand("item replace entity @p hotbar.4 with minecraft:bucket");
			world.getServer().runCommand("item replace entity @p hotbar.5 with minecraft:water_bucket");
			world.getServer().runCommand("item replace entity @p hotbar.6 with minecraft:lava_bucket");
			world.getServer().runCommand("item replace entity @p hotbar.7 with minecraft:milk_bucket");
			context.getInput().lookAt(0f, 20f);
			context.waitTicks(10);
			context.takeScreenshot("hotbar_buckets");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(10);
			context.takeScreenshot("inventory_buckets");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(5);

			// Mod Menu (dev-only): the mod must be registered in its list and the Mods screen must open.
			boolean listed = context.computeOnClient(mc -> ModMenu.MODS.containsKey(Copperbucket.MOD_ID));
			if (!listed) {
				throw new AssertionError(Copperbucket.MOD_ID + " is not listed in Mod Menu");
			}
			context.setScreen(() -> new ModsScreen(null));
			context.waitTicks(20);
			context.takeScreenshot("mod_menu");
				pass("mod menu");
		}
	}

	private static void rightClick(ClientGameTestContext context) {
		context.getInput().pressKey(options -> options.keyUse);
		context.waitTicks(5);
	}

	private static void give(TestSingleplayerContext world, String stack) {
		world.getServer().runCommand("item replace entity @p weapon.mainhand with " + stack);
	}

	private static boolean isWater(TestSingleplayerContext world) {
		return world.getServer().computeOnServer(server ->
			server.overworld().getBlockState(WATER_POS).is(Blocks.WATER)
				&& server.overworld().getFluidState(WATER_POS).isSource());
	}

	private static void expectHeld(TestSingleplayerContext world, String id, int damage, String when) {
		String actual = world.getServer().computeOnServer(server -> {
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
			ItemStack held = player.getMainHandItem();
			return BuiltInRegistries.ITEM.getKey(held.getItem()) + "#" + held.getDamageValue() + "x" + held.getCount();
		});
		String expected = id + "#" + damage + (id.equals("minecraft:air") ? "x0" : "x1");
		expect(actual.equals(expected), when + ": expected " + expected + " but held " + actual);
	}

	private static void pass(String step) {
		System.out.println("[copperbucket-gametest] PASS " + step);
	}

	private static void expect(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
