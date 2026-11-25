# Copper Bucket

A bucket made of copper. It works like the vanilla bucket, but it wears out and eventually breaks, so it
is not a perfect replacement.

Fabric mod for Minecraft 26.3 (Java 25, Mojang names). Requires Fabric API only. Singleplayer and server.

## What you get

| Item | How |
|---|---|
| Copper Bucket | Same recipe as the vanilla bucket, with 3 copper ingots in a V |
| Copper Water Bucket | Use the Copper Bucket on a water source |
| Copper Lava Bucket | Use the Copper Bucket on a lava source |
| Copper Milk Bucket | Use the Copper Bucket on an adult cow |

## Durability

- Each bucket lasts **64 uses**. It never stacks (like any damaged tool).
- **Filling is free.** It costs 1 durability when you use the contents up: placing the water or lava, or
  drinking the milk. The empty bucket you get back keeps its damage, and the last use breaks it.
- Creative mode never uses durability.
- Repair it on an anvil with copper ingots. It cannot be enchanted.

## Not supported yet

Cauldrons and dispensers do not know about copper buckets (vanilla hard-codes the iron bucket there),
and powder snow, fish, axolotl and tadpole buckets are not made of copper. See `ROADMAP.md`.

## Development

Build and test: `.\gradlew.bat build`. Full check (build, unit tests, in-game client gametest):
`.\scripts\verify.ps1`. The durability rule lives in `DurabilityPolicy` (pure Java, unit-tested).

License: CC0-1.0.
