# SteamAdvantage

**Minecraft 1.17.1 local source port, version 2.2.9.117011.**

Steam Advantage adds boilers, steam-powered machines, pumps, storage, elevators
and a blackpowder musket to Power Advantage. This candidate is not a published
release. Manual testing and separate approval are required before any port push,
PR, merge, tag or release.

## Build Target

Minecraft 1.17.1, Forge 37.1.1, official 1.17.1 mappings and MCP
20210706.113038. ForgeGradle 7.0.34 and Gradle 9.6.1 run on Java 17.
Production compilation and launches use Temurin 16.0.2+7, class major 60.
Mavenizer uses Java 25. Java 8 is available only for older build utilities.

The implementation namespace is `zone.moddev.mc.steamadvantage`. The mod ID,
registry names, assets, saved NBT keys and provider IDs remain `steamadvantage`.
The existing `cyano.poweradvantage.api` names remain in use.

Power Advantage 2.4.8.117011 is required. The coordinated local source checkpoint is
`fc4c47f2ed8cfebbaa010e6a0e807137a3329f95`, with development jar SHA-256
`BDF486E16C1FE3B4CE2FE1ED78670AABA594063BCD2D4251265124B17FA9E767`.
This pin is not final runtime acceptance. The build must not use a 1.16
dependency or silently replace these exact inputs.

Base Metals 3.1.0.117011 and OreSpawn 4.1.0.117011 are separate mods, not
embedded libraries. Development jars must come from the coordinated builds of
published sources `dacfaa3af2e3310d5cb6b081da79f09260ae103d` and
`b56e2b62a1a6ea4d1dd96f8646a87801a817dbdf`. Their development hashes
are not the public runtime hashes recorded separately in `gradle.properties`.

Base Metals remains optional in Forge metadata; CurseForge's required-install
ordering remains Power Advantage then Base Metals. No dependency is embedded.

## Local Verification

The parent campaign owns dependency pins, the cache and the shared test rig.
Every local Gradle command must use its `Invoke-Gradle117.ps1` helper, which
holds `gradle-shared.lease` and uses
`D:/GradleCaches/advantage-117-20261031`. Supply the exact native development
jars with `-PpowerAdvantageDeobfJar`, `-PbaseMetalsDeobfJar` and
`-PoreSpawnDeobfJar`. Never run concurrent builds against that cache.
Standalone Java tests also require Power's verified test-only native capability
bootstrap jar through `-PforgeCapabilityBootstrapJar`; it is never embedded or
added to the production runtime.

With the exact dependency pins supplied, the required tasks are
`check build deobfJar javadoc verifyReleaseDependencies verifyReleaseArtifacts
writeReleaseChecksums verifyEclipseProductionClasspath`.
The public artifact family remains `SteamAdvantage-2.2.9.117011.jar`,
`-sources.jar` and `-javadoc.jar`; the development jar stays under
`build/libs-dev`.

`node tools/verify-native-port.js` performs source and resource checks only.
The Java tests, release audits, Eclipse audit, packaged runtime, migration and
reproducibility gates must be run against the exact pinned candidate.
CI is prepared for 1.17.1 but has not been run hosted.

The current Power checkpoint and native advancement correction passed 195
executed tests, release/resource audits, Javadoc and Eclipse configuration checks.
Two forced offline runs executed all 32 tasks and produced identical production,
sources, Javadoc and development jars. All four remain byte-identical to the
previous advancement-fix candidate; full packaged runtime qualification with
the new shared-persistence Power pin remains pending. The
[local test record](docs/PORT117-STATUS.md) identifies the exact inputs, retained
failed runs and remaining runtime and client-launch gaps. These automated checks
are not manual gameplay or visual acceptance.

## Port Details

Native block-entity factories preserve their actual constructor position and
block state. Loading NBT does not replace that position, including when a drill
moves along a track. Power's shared native ticker retains per-tick work before
the eight-tick power update on both logical sides. The drill-bit helper has its
own native ticker.

The port retains first-loaded-tick redstone sampling in the crusher and furnace,
water-only boiler inputs, dirty-owner callbacks, finite energy and fluid
accounting, persistence aliases, helper state and GUI caption rows. Native
PoseStack and vertex consumers retain the drill geometry, UVs and animation.
Musket terrain traversal stops before unloaded chunks; sound dispatch still
uses loaded server players.

Native pickaxe tags cover the metal machines and helpers without introducing
new minimum tool tiers or changing drop requirements. Ordinary blocks retain
their self-drop tables; hidden helpers still have no item form or drops.
Recipe ingredients, processing costs and quantities are unchanged. Recipe-book
advancements use native singleton `items` predicates; the ignored legacy `item`
field must not turn an ingredient discovery into an unrestricted predicate.

Steam reads the real Base Metals crushing recipes. In the pinned 1.17 release,
raw copper crushes into one powder and copper ore into two. This follows Base
Metals' published raw-metal recipes; Steam does not override the balance.
Geothermal heat uses native Forge lava temperature, retaining the existing
1300 K input and resulting heat value.

## Acceptance Still Required

No historical 1.16 automated result qualifies this candidate. Compilation,
executed unit tests, packaged server/client checks and manual gameplay are
separate gates. Until those runs complete, no runtime pass is claimed here.

Manual testing must cover all boilers, crusher and furnace reload/redstone,
drills in six directions and track moves with inventory and energy, helper
rotation and cleanup, pumps, stills, elevator rides, GUI captions and controls,
musket last-cartridge firing, terrain boundaries, sounds and enchantments,
multiplayer synchronization and capability unload/reload.

Use disposable whole-world copies for upgrades. Preserve original worlds and
backups; never reopen an upgraded world with an older Minecraft version.
