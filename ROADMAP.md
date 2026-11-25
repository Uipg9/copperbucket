# Copper Bucket roadmap

## v1 (in progress)
- Empty / water / lava / milk copper buckets, 64 uses, recipe = vanilla bucket with copper ingots.
- Rule: filling is free; 1 damage when the contents are used up (placed or drunk). Creative takes none.
- Cow milking via `UseEntityCallback`. Anvil repair with copper ingots, Unbreaking/Mending work.

## v2 candidates (deferred because vanilla hard-codes `Items.BUCKET`)
1. **Cauldron**: `CauldronInteractions.Dispatcher.put` is package-private in 26.3, so this needs an
   accessor mixin or access widener. Must carry damage across swaps (no free repair).
2. **Dispenser**: vanilla's fluid behavior returns `Items.BUCKET` as the empty result. Register our own
   behaviors per variant; dispensers never damage the bucket (carry damage unchanged).
3. **Mooshroom / goat milking** if they use identity checks too (verify with javap first).
4. **Powder snow / mob buckets**: out of scope unless asked for.
5. Optional: lava bucket as furnace fuel (needs remainder with carried damage).

## Review follow-ups (from the v1 code review, all non-blocking)
- Parity: vanilla lava bucket is furnace fuel and milk/lava have `craftRemainder(BUCKET)`. Copper versions have
  neither. A mod recipe using the `c:buckets/milk` tag would consume the copper bucket without a remainder.
- A recipe advancement so the recipe unlocks in the recipe book on its own.
- Grindstone / crafting-grid merging of two damaged buckets works like any damageable item (document only).
- Filling a bucket stack above 1 (only via /give) would destroy the whole stack on break. Not a gameplay issue.

## Quality / perf notes
- No tick hooks, no mixins in v1: cost is only on item use and entity interaction.
- `UseEntityCallback` must bail out first on `!(entity instanceof AbstractCow)` / wrong item.
- Keep `DurabilityPolicy` pure and unit-tested; tune `MAX_DURABILITY` there only.
- Revisit durability value after playtesting (64 is a guess).
