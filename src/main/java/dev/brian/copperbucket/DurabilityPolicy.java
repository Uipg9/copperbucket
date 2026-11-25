package dev.brian.copperbucket;

/**
 * Durability rule for the filled copper bucket. Pure Java so it can be unit-tested without the game.
 *
 * <p>1 damage per full cycle, applied when the contents are used up (emptied into the world or
 * drunk). Filling is free, and automation (dispensers etc.) never calls this. Creative mode takes
 * no damage.
 */
public final class DurabilityPolicy {
	public static final int MAX_DURABILITY = 64;

	/** Result of using up a bucket's contents: the damage to store, and whether the bucket is destroyed. */
	public record UseOutcome(boolean breaks, int newDamage) {
	}

	private DurabilityPolicy() {
	}

	public static UseOutcome afterUse(int currentDamage, boolean creative) {
		if (currentDamage < 0) {
			throw new IllegalArgumentException("currentDamage must be >= 0: " + currentDamage);
		}
		if (creative) {
			return new UseOutcome(false, currentDamage);
		}
		int newDamage = currentDamage + 1;
		return new UseOutcome(newDamage >= MAX_DURABILITY, newDamage);
	}
}
