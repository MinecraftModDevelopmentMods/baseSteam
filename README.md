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
`5b9abe578b053d2ef59b1962cf9e04f6880c91b9`, with development jar SHA-256
`799B59B06EB2AC3FC4682AA64BF42C3A2A2C1D89D7FFFE9C72745DEC7DFBE0D8`.
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

Use the checked-in Gradle wrapper and the exact development API jars described
above. The build checks their hashes against `gradle.properties`; runtime jars
are not replacements for these development inputs. Set the JDK-home and API-jar
environment variables below to local paths. Java 8 is only for legacy utilities,
not compilation or Minecraft. Power's `unitBootstrapJar` task builds the
test-only Forge capability bootstrap; it is never embedded or launched in production.

For example, from the Steam checkout in PowerShell:

```powershell
$env:JAVA_HOME = $env:JAVA17_HOME
$jdks = @($env:JAVA17_HOME, $env:JAVA16_HOME, $env:JAVA25_HOME, $env:JAVA8_HOME) -join ','
$toolchains = @("-Porg.gradle.java.installations.paths=$jdks",
  '-Porg.gradle.java.installations.auto-detect=false',
  '-Porg.gradle.java.installations.auto-download=false')
$power = $env:POWERADVANTAGE_CHECKOUT
& "$power/gradlew.bat" -p $power @toolchains unitBootstrapJar `
  "-PbaseMetalsDeobfJar=$env:BASEMETALS_DEOBF_JAR" `
  "-PoreSpawnDeobfJar=$env:ORESPAWN_DEOBF_JAR"
./gradlew.bat @toolchains check build deobfJar javadoc `
  verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums `
  verifyEclipseProductionClasspath `
  "-PpowerAdvantageDeobfJar=$env:POWERADVANTAGE_DEOBF_JAR" `
  "-PbaseMetalsDeobfJar=$env:BASEMETALS_DEOBF_JAR" `
  "-PoreSpawnDeobfJar=$env:ORESPAWN_DEOBF_JAR" `
  "-PforgeCapabilityBootstrapJar=$power/build/unit-bootstrap/forge-native-unit-bootstrap.jar"
```

`-PforgeCapabilityBootstrapJar` can instead point to a verified bootstrap jar
built from the same pinned Power source. On other platforms use `./gradlew`.
The public artifact family remains `SteamAdvantage-2.2.9.117011.jar`,
`-sources.jar` and `-javadoc.jar`; the development jar stays under
`build/libs-dev`.

`node tools/verify-native-port.js` performs source and resource checks only.
The Java tests, release audits, Eclipse audit, packaged runtime, migration and
reproducibility gates must be run against the exact pinned candidate.
CI is prepared for 1.17.1 but has not been run hosted.

The optional Base Metals recipe-guard fix passed 199 executed tests, release/resource
audits, Javadoc and Eclipse configuration checks, re-executing all 32 tasks without
build-cache reuse. It changes nine recipe conditions, so the preceding candidate's
frozen-J artifact equality is historical, not proof of these new resource bytes.
The [module test record](docs/PORT117-STATUS.md) identifies exact inputs and retained
failures. Combined server, client, migration and endurance qualification is tracked
in the coordinated Power Advantage checkout's `docs/1.17/CURRENT-QUALIFICATION.md`;
read its artifact pins and remaining limits before reusing a result. Automated
checks do not replace manual gameplay or visual acceptance.

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
Optional Base Metals crushing recipes also require `forge:mod_loaded` before
their recipe-mode condition, so APOCALYPTIC loads safely without that serializer.

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
