package dev.brian.copperbucket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DurabilityPolicyTest {
	@Test
	void freshBucketTakesOneDamageAndSurvives() {
		var outcome = DurabilityPolicy.afterUse(0, false);
		assertEquals(1, outcome.newDamage());
		assertFalse(outcome.breaks());
	}

	@Test
	void lastSafeUseSurvives() {
		var outcome = DurabilityPolicy.afterUse(DurabilityPolicy.MAX_DURABILITY - 2, false);
		assertEquals(DurabilityPolicy.MAX_DURABILITY - 1, outcome.newDamage());
		assertFalse(outcome.breaks());
	}

	@Test
	void finalUseBreaks() {
		var outcome = DurabilityPolicy.afterUse(DurabilityPolicy.MAX_DURABILITY - 1, false);
		assertEquals(DurabilityPolicy.MAX_DURABILITY, outcome.newDamage());
		assertTrue(outcome.breaks());
	}

	@Test
	void alreadyOverMaxStillBreaks() {
		assertTrue(DurabilityPolicy.afterUse(DurabilityPolicy.MAX_DURABILITY, false).breaks());
		assertTrue(DurabilityPolicy.afterUse(DurabilityPolicy.MAX_DURABILITY + 10, false).breaks());
	}

	@Test
	void creativeNeverChangesOrBreaks() {
		var outcome = DurabilityPolicy.afterUse(DurabilityPolicy.MAX_DURABILITY - 1, true);
		assertEquals(DurabilityPolicy.MAX_DURABILITY - 1, outcome.newDamage());
		assertFalse(outcome.breaks());
		assertEquals(0, DurabilityPolicy.afterUse(0, true).newDamage());
	}

	@Test
	void negativeDamageIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> DurabilityPolicy.afterUse(-1, false));
		assertThrows(IllegalArgumentException.class, () -> DurabilityPolicy.afterUse(-1, true));
	}

	@Test
	void breaksOnExactlyTheMaxDurabilityUse() {
		int damage = 0;
		for (int use = 1; use <= DurabilityPolicy.MAX_DURABILITY; use++) {
			var outcome = DurabilityPolicy.afterUse(damage, false);
			assertEquals(use == DurabilityPolicy.MAX_DURABILITY, outcome.breaks(), "use " + use);
			damage = outcome.newDamage();
		}
		assertEquals(DurabilityPolicy.MAX_DURABILITY, damage);
	}
}
