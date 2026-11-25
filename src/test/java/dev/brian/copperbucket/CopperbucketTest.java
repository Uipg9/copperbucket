package dev.brian.copperbucket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CopperbucketTest {
	@Test
	void idUsesModNamespace() {
		var id = Copperbucket.id("example");
		assertEquals(Copperbucket.MOD_ID, id.getNamespace());
		assertEquals("example", id.getPath());
	}

	@Test
	void langKeyFollowsMinecraftConvention() {
		assertEquals("item.copperbucket.example", Copperbucket.langKey("item", "example"));
	}
}
