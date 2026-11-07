# baseSteam

**Minecraft 1.16.5, version 2.2.9.116051.**

Use [basePower 2.4.8.116051](https://www.curseforge.com/minecraft/mc-mods/basepower/files/9501921)
and [Base Metals 3.1.1.116051](https://www.curseforge.com/minecraft/mc-mods/base-metals/files/9501458). The
[shared test record](https://github.com/MinecraftModDevelopmentMods/basePower/blob/master-1.16.5/docs/1.16/BASE-RELEASE-CANDIDATE.md)
covers machine operation, world upgrades and full restarts.

baseSteam continues DrCyano's Steam Advantage. It adds boilers, steam
distribution and steam-powered machinery to basePower. Back up your world
before upgrading; the port maps legacy Steam Advantage blocks and saved state.

## Build Target

The `master-1.16.5` target uses Minecraft 1.16.5, Forge 36.2.34,
official 1.16.5 mappings, and MCP config 20210115.111550.
ForgeGradle 7.0.34 and Gradle 9.6.1 run on Temurin 17.0.1+12.
Compilation and Minecraft launches use Temurin 8.0.502+7 and Java 8 bytecode;
Mavenizer uses Temurin 25.0.3+9.0.LTS.

basePower 2.4.8.116051 is a separate required mod. Its source revision and
development JAR checksum are pinned in `gradle.properties` and checked during builds.
Use an immutable development jar through `-PpowerAdvantageDeobfJar=<path>`.
Release audits reject missing, placeholder, or mismatched pins. These source pins
do not grant publication approval.

Power's migration hook preserves legacy Forge swimming speed, nametag distance,
gravity and reach attributes, including their values and modifiers. Steam uses
that shared hook without duplicating the conversion.

Earlier dependency-candidate build and runtime receipts remain historical evidence
for their original inputs. They do not qualify the current candidate. Automated
build and test results, bounded runtime checks, and manual acceptance are separate
gates.

The supplied native development dependencies are Base Metals 3.1.1.116051
(source `995f6cc4dd2d5eecc628a42e5c43fa5390c24bca`) and OreSpawn 4.1.0.116051
(source `a113e5d767169189f0e35ade1e173a12d550b00e`). Their exact jar hashes
are recorded in `gradle.properties`. None of the dependencies is embedded.

With matching native development jars available:

```text
gradlew.bat compileJava compileTestJava -PpowerAdvantageDeobfJar=<path>
```

Alternative layouts may also pass `-PbaseMetalsDeobfJar=<path>` and
`-PoreSpawnDeobfJar=<path>`. Explicit toolchain installations can be supplied
through Gradle project properties. Do not use a dependency from an older target.

With the exact dependency jars pinned, the full build gates are:

```text
gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums verifyEclipseProductionClasspath
```

A compilation pass is not a test pass, release-audit pass, or runtime acceptance.
Shared Gradle caches require exclusive ownership; do not run competing builds
against the same cache or checkout.

## Compatibility

New saves use `basesteam:*`. The shared upgrade code maps older
`steamadvantage:*` IDs to their baseSteam equivalents. Trade ranges, machine
costs, output quantities and saved machine data are preserved.

The native port uses world-only conduit callbacks. Power Advantage owns registry-key
dimension handling, shared capability invalidation/revival, fluid adapters, and base
tile lifecycle. Steam uses native `load(BlockState, CompoundNBT)` and
`save(CompoundNBT)`, retaining deferred drill targets, cached facing, progress,
inventory, energy, legacy tank aliases, and musket loaded-state bytes.
Saving a drill must not reacquire its world or neighbouring chunks.

Client setup registers native item model predicates, drill rendering, and the
track's cutout layer. Native block predicates preserve non-occlusion and prevent
full-collision tracks and platforms from conducting redstone. Existing renderer
geometry, UVs, lighting, and animation axes are retained.

Every crafting variant has ingredient-triggered recipe-book discovery.
Regenerate unlock JSON after recipe changes with
`node tools/generate-recipe-unlocks.js`. Processing recipes remain outside the
crafting book. Recipe conditions are registered on the queued setup thread.

Unit tests cover native properties, exact levels and costs, persistence,
world-only callbacks, recipe discovery, and resources. They do not establish
in-world GUIs, sounds, interactions, multiplayer behavior, or arbitrary modpacks.

Power Advantage supplies the combined-mod runtime harness and shared legacy
migration hook. Upgrade testing must use disposable whole-world copies with
pinned inputs. Preserve original worlds and backups; never open a world in an
older Minecraft version after upgrading it. Manual acceptance and publication
approval remain separate from automated evidence.

The shared [machine operating checks](https://github.com/MinecraftModDevelopmentMods/basePower/blob/master-1.16.5/docs/1.16/OPERATING-TESTS.md)
exercise finite boiler, processing, pumping, storage, elevator and musket jobs.
They check real output and resource use, including failed jobs and complete
server restarts. Live player controls and rendering still need a walkthrough.
