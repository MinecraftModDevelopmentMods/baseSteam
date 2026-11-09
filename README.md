# baseSteam

**Minecraft 1.18.2 (Forge), version 2.2.11.118021. Local candidate.**

baseSteam, formerly Steam Advantage, adds boilers, steam-powered machines,
pumps, storage, elevators and a blackpowder musket to basePower. Its items and
blocks use the shared basePower creative tab. This version is not published.

## Upgrading a World

Back up your world and test the upgrade on a copy. Replace Steam Advantage and
Power Advantage with baseSteam and basePower; do not install both sets of mods.
The migration recognizes old Steam Advantage IDs and keeps the registry paths,
machine settings and saved data used by earlier baseSteam versions. The
Minecraft 1.18.2 world upgrade still needs verification for this candidate.

Existing `basesteam-common.toml` settings take precedence. When that file is
absent, baseSteam can copy `steamadvantage-common.toml` or import settings from an
older `.cfg` file. The old config is left in place.

## Build Target

Minecraft 1.18.2, Forge 40.3.0, official 1.18.2 mappings and MCP
20220404.173914. ForgeGradle 7.0.34 and Gradle 9.6.1 run on Java 17.
Production compilation and launches use Temurin 17, class major 61.
The build-only Mavenizer fixture uses Java 25 and corrects the known Forge 40
`Holder.Direct` decompile issue. Java 8 is only for older build utilities.

The mod ID and resource namespace are `basesteam`; the Java implementation
namespace is `zone.moddev.mc.basesteam`. The shared `cyano.poweradvantage.api`
names remain in use.

basePower 2.4.10.118021 is required. Base Metals 3.1.2.118021 remains optional
in Forge and requires OreSpawn 4.1.0.118021 when installed. The CurseForge
install metadata lists basePower and Base Metals. No dependency is embedded.

## Local Verification

Use the checked-in Gradle wrapper and supply explicit paths to the development
jars matching the pins in `gradle.properties`. Use a Steam-only Gradle user
home and the test-only Forge capability bootstrap. Player JARs are distinct
from development and published JARs. Before a packaged smoke, run
`verifyPlayerDependencies` with explicit `powerAdvantagePlayerJar`,
`baseMetalsPlayerJar` and `oreSpawnPlayerJar` project properties.

Use the coordinated basePower 1.18.2 source pin and matching frozen JARs.
The build rejects the earlier API-only checkpoint.

For example, from the Steam checkout in PowerShell:

```powershell
$env:JAVA_HOME = $env:JAVA17_HOME
$jdks = @($env:JAVA17_HOME, $env:JAVA25_HOME, $env:JAVA8_HOME) -join ','
$toolchains = @("-Porg.gradle.java.installations.paths=$jdks",
  '-Porg.gradle.java.installations.auto-detect=false',
  '-Porg.gradle.java.installations.auto-download=false')
$env:GRADLE_USER_HOME = $env:BASESTEAM_GRADLE_USER_HOME
./gradlew.bat @toolchains check build deobfJar javadoc `
  verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums `
  verifyEclipseProductionClasspath `
  "-PpowerAdvantageDeobfJar=$env:POWERADVANTAGE_DEOBF_JAR" `
  "-PbaseMetalsDeobfJar=$env:BASEMETALS_DEOBF_JAR" `
  "-PoreSpawnDeobfJar=$env:ORESPAWN_DEOBF_JAR" `
  "-PforgeCapabilityBootstrapJar=$env:FORGE_CAPABILITY_BOOTSTRAP_JAR"
```

On other platforms use `./gradlew`. The artifact family is
`baseSteam-2.2.11.118021.jar`, plus sources and Javadoc. The development jar
stays under `build/libs-dev`.

`node tools/verify-native-port.js` and `node tools/generate-recipe-unlocks.js --check`
run without the dependency jars. [1.18.2 port status](docs/PORT118-STATUS.md)
tracks current checks. [1.17.1 port status](docs/PORT117-STATUS.md) is historical.

## Port Details

Native block-entity factories preserve their actual constructor position and
block state. Loading NBT does not replace that position, including when a drill
moves along a track. Power's shared native ticker retains per-tick work before
the eight-tick power update on both logical sides. The drill-bit helper has its
own native ticker. Minecraft 1.18.2 uses `saveAdditional` for native saves,
and the drill-bit direction packet carries the native block-entity type.

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
Optional Base Metals crushing recipes also require `forge:mod_loaded` before
their recipe-mode condition, so APOCALYPTIC loads safely without that serializer.

Steam reads Base Metals' crushing recipes without overriding their balance.
Geothermal heat uses native Forge lava temperature, retaining the existing
1300 K input and resulting heat value.

## Acceptance Still Required

Historical results from earlier versions do not qualify this candidate.
Compilation, Java tests, repeat Eclipse refresh, packaged server/client checks,
world migration and manual gameplay are separate gates.

Manual testing must cover all boilers, crusher and furnace reload/redstone,
drills in six directions and track moves with inventory and energy, helper
rotation and cleanup, pumps, stills, elevator rides, GUI captions and controls,
musket last-cartridge firing, terrain boundaries, sounds and enchantments,
multiplayer synchronization and capability unload/reload.

Use disposable whole-world copies for upgrades. Preserve original worlds and
backups; never reopen an upgraded world with an older Minecraft version.
