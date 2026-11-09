# baseSteam 1.19.4 local port

- Source: accepted 1.18.2 `d10c56cf021e6bd701e973cda90358495844a45f`,
  cloned locally with `--no-hardlinks` onto
  `feature/basesteam-1.19.4-port`.
- Target: baseSteam `2.2.12.119041`, basePower `2.4.11.119041`,
  Minecraft `1.19.4`, Forge `45.4.0`, official mappings,
  MCP `20230314.122934`, Temurin 17, ForgeGradle `7.0.34`, and Gradle `9.6.1`.
- No external publication, tag, deployment, upstream change or protected world.

## Tooling

The [official Forge listing](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.19.4.html)
identifies 45.4.0 as the recommended 1.19.4 Forge. ForgeGradle 7 configuration
and its Java 25 Mavenizer follow the local Forge 45.4.0 builds. The required
[compatibility derivative](../ci-fixtures/tools/README.md) is an external
build-only input from the pinned OreSpawn checkout, checked by SHA-256.
Java 25 is not a production toolchain. Use a separate Steam Gradle user home or the
agreed warm cache with distinct project state.

CI and CodeQL apply Steam's Forge 45 Mavenizer adapter to the immutable
basePower and Base Metals checkouts. This does not require the pinned Power
runtime source to contain the adapter and does not change either dependency pin.

## Compatibility

RegisterEvent preserves the block, item, enchantment and block-entity paths.
Player-facing items contribute through `ItemGroups.show`; the drill-bit and
pump-pipe block items stay hidden. NBT and legacy migration keys, recipes,
textures, and loot tables are unchanged. Sound dispatch retains its loaded-player,
range and spectator checks. Native damage types retain the old death-message keys.
The musket uses the TNT interaction to preserve full drops by default; it
honors the TNT drop-decay gamerule. Base Metals' crusher adapter is only
linked when the optional mod is loaded; the vanilla fallback and recipe
condition order remain in place.

## Inputs and evidence

The Base Metals 3.1.2 migration-hook development and player JARs are from
`_verification/base-119-port-20261109/Base Metals hook build`. OreSpawn is
from `D:/MinecraftMineralogy/OS119/OreSpawn`. The exact pins are in
[gradle.properties](../gradle.properties); the Base Metals and OreSpawn
inputs are not claims of new public releases.

Local receipts are under the clone's ignored `evidence/port119/`:

- `mapped-preflight-a`: six-suite exploratory run retained with three failures:
  missing Forge network-event transformation and two asset assertions without Power.
- `mapped-independent-b`: the native damage codec requires Minecraft bootstrap;
  the initial standalone attempt failed and is retained.
- `event-bootstrap-probe*`: successive failed native bootstrap experiments.
  The initial transform attempted to transform Event twice, the next missed a
  directly derived network event, and the third exposed a test item without a
  Forge registry delegate.
- `mapped-native-tests-d`: 22 JUnit tests passed across six suites with the
  test-only network-event agent and no Power JAR. This covered fuel, damage type
  codec, loot and pack metadata, optional recipe conditions, recipe unlocks,
  and sound dispatch. It did not execute machines or load a world.
- `provisional-compile-a`, `b` and `c`: direct Java 17 compile against mapped
  Forge SHA-256 `B05295524E3277ADB61C348E3C4FFB3101C7EF147F6136AC86470BD6BD5A006B`,
  the pinned Base Metals and OreSpawn development JARs, and provisional Power
  SHA-256 `22210403D5CEAD59354ABBEC796433283ADB68254A92A344C23DE829438822E5`.
  The first two receipts retained 16 production and four test compilation
  errors respectively; the third compiled 64 production and 32 test files.
  No tests, packages or launches used the provisional Power input.

The source audit verified 181 JSON/pack files, 12 block-entity factories,
17 harvest entries, and 24 recipe unlocks.

## Full local audits at Power 1cf39fd

Power source `1cf39fd78ad891da26c32893ee0de675f61ec243` was the verified
local checkpoint for this build. All development, player and test-bootstrap
inputs were copied and hashed before the build, and neither Gradle run read
the mutable parent build paths. The Power bootstrap already includes the
native network event transform, so the Steam-only experimental agent is not
part of this suite or the release artifacts.

`evidence/port119/local-final-a/full-a.log` and `test-results-a/` retain the
first run: 207 tests, four failures. The plain JUnit harness had not registered
Forge's vanilla fluid types or initialized the cached fluid states needed by
the native pump fixtures. The fixes run the actual Forge test registry phases
and initialize the fixture states; production code and assertions were not
relaxed.

`full-b.log` passed the full Gradle build, release configuration and
dependencies, package contents and checksums, explicit player dependency
audit, and Eclipse production classpath and launch configuration audit.
`test-results-b/` records 207 tests in 31 suites with no failures, errors or
skips. `steam-source-b.json` inventories the source before these handoff notes.

The exact output set is frozen in `local-final-a/artifacts/` and recorded in
`artifact-receipt.json`. The player JAR SHA-256 is
`8DDFAF1E924EDB47616CA4CAC26C2FB4E49385DE40D4E1638FB82626036023C4`;
the development JAR SHA-256 is
`419921119001F2421C2B30C80C75AAA0C0F8680369064DC6A650ACE8673E28F2`.

This evidence is for the pinned local Power checkpoint only. It does not
qualify a later Power repair, the combined suite, world upgrade, Eclipse launch
execution or manual acceptance. Steam/Electric 1.18 publication and
reconciliation against merged source are not claimed.

## Rebuild at repaired Power c999531

Power checkpoint `c9995315e2aee9d9bffec8eb8b1191ad8a0faeb7` was pinned
with deobf SHA-256
`512FDEC9275B33533EF773C110E6229E0F07268775FAD4B5942A15B68A9EB87D`
and player SHA-256
`E1689C1147F6F6F93F6516D9188A279093D6CCC2FF1FBEB506468B876BA03E60`.
The bootstrap and companion pins are unchanged. The build read only the
verified frozen inputs listed in `evidence/port119/repaired-power-c999531-a/inputs.json`.

All 36 Gradle tasks executed in the offline rebuild, including the full
suite, dependency and package audits, and the Eclipse production classpath
and launch configuration audit. All 207 tests in 31 suites passed.
The four JARs and checksum file match the preceding checkpoint byte for byte,
but the rebuild and tests have separate receipts. The new evidence directory
contains `full-a.log`, `test-results/`, `artifact-receipt.json` and the Steam
manifest entry `combined-input.json`. The frozen player JAR is under
`artifacts/` with SHA-256
`8DDFAF1E924EDB47616CA4CAC26C2FB4E49385DE40D4E1638FB82626036023C4`.

This is the Steam handoff for the combined test. No combined run, hosted
reconciliation, Eclipse launch, or manual acceptance is claimed here.

## Drill-bit atlas repair

The combined Forge 45 client reported a missing baked sprite for
`basesteam:entity/drill_bit`. Steam now contributes that single sprite to
`assets/minecraft/atlases/blocks.json`. The historical texture path and PNG
are unchanged, and the existing Power directory sources remain separate.

The model audit now rejects unstitched Steam and Power textures referenced by
Steam models. The package audit allows only this exact atlas file outside
Steam's asset namespace and verifies that it contains only the drill-bit
source. The expected pre-fix failure is retained in
`evidence/port119/drill-bit-atlas-a/atlas-red.log`.

The full rebuild against the same frozen Power `c999531` pins passed all 36
Gradle tasks and 207 tests in 31 suites. The old/new player and deobf archives
each gained only the atlas JSON; all 73 classes and every other payload entry
are byte-identical. The comparison and refreshed combined manifest entry are
in `jar-payload-delta.json` and `combined-input.json` in that evidence directory.
The graphical and affected combined integration reruns are pending.
