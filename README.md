# baseSteam

**Minecraft 1.21.11 (Forge), baseSteam 2.2.16.121111.**

baseSteam adds boilers, steam-powered machines, pumps, storage, elevators
and a blackpowder musket to basePower. Items and blocks share the basePower
creative tab. The mod ID and resource namespace remain `basesteam`.

## Build

Target: Forge 61.1.0, official Minecraft 1.21.11 mappings and MCP
20251223.124241. ForgeGradle 7.0.34 and the checksum-pinned Gradle 9.6.1
wrapper use Temurin 21.0.7+6. Mavenizer uses Java 25; the launcher helper
also needs Java 8. Minecraft itself runs on Java 21.

Install [basePower 2.4.15.121111](https://github.com/MinecraftModDevelopmentMods/basePower/releases/tag/2.4.15.121111),
[Base Metals 3.1.2.121111](https://www.curseforge.com/minecraft/mc-mods/base-metals/files/9566738)
and [OreSpawn 4.1.0.121111](https://www.curseforge.com/minecraft/mc-mods/mmd-orespawn/files/9268464)
as separate Forge 1.21.11 jars on both client and server, not bundled copies.
Base Metals is REQUIRED on both client and server, not an optional integration.
The default build paths use the coordinated 1.21.11 checkouts. Exact released
source, jar and test-bootstrap pins are in `gradle.properties` and are checked
before compilation and tests.

This version has passed automated qualification and manual acceptance. The
[qualification record](https://github.com/MinecraftModDevelopmentMods/basePower/blob/master-1.21.11/docs/1.21.11/QUALIFICATION.md)
records the machine, upgrade, graphical and two-world endurance checks and
their coverage limits. Earlier no-Base-Metals receipts are
historical evidence, not a supported installation profile.
The mod root declares server data format 94.1. Forge supplies format 75.x
metadata for its hidden client resource aggregate. Native resource-reader
tests cover both paths without codec warnings; see
[Forge metadata notes](docs/FORGE61-RESOURCE-METADATA.md) for the separate
Forge 61.1.0 server compatibility-label defect.

The Mavenizer fixture comes from OreSpawn's OS12111 checkout. Another layout
can use `-Pforge61MavenizerJar=<path>`. The `*DeobfJar` and `*PlayerJar`
properties select separate development and packaged inputs; both must
satisfy the committed checksums.

With the pinned released dependencies available locally:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE/.jdks/temurin-21.0.7+6"
$env:GRADLE_USER_HOME = 'D:/GradleCaches/basesteam-12111-port'
$jdk8 = "$env:USERPROFILE/.jdks/temurin-8.0.502+7/jdk8u502-b07"
$jdks = "$env:JAVA_HOME,$env:USERPROFILE/.jdks/jdk-25.0.3+9,$jdk8"
$toolchains = @("-Porg.gradle.java.installations.paths=$jdks",
  '-Porg.gradle.java.installations.auto-detect=false',
  '-Porg.gradle.java.installations.auto-download=false')
./gradlew.bat @toolchains check build deobfJar verifyReleaseArtifacts writeReleaseChecksums
./gradlew.bat @toolchains verifyEclipseProductionClasspath
./gradlew.bat @toolchains -PsteamGameTestRunId=candidate verifyPlayerDependencies runGameTestServer
./tools/run-packaged-server-smoke.ps1 -ForgeInstall $env:FORGE_12111_SERVER_INSTALL -RunId candidate-with-metals -UseNioTransport -Reopen
```

For a checkout outside the sibling-folder layout, unit tests also need the
verified Power test bootstrap and its two source files. Build Power first,
then supply these paths as well as the dependency-jar overrides:

```text
-PforgeCapabilityBootstrapJar=<Power checkout>/build/unit-bootstrap/forge-native-unit-bootstrap.jar
-PforgeCapabilityBootstrapSource=<Power checkout>/test-rig/unit-bootstrap/ForgeCapabilityBootstrap.java
-PforgeUnitRegistriesSource=<Power checkout>/test-rig/unit-support/zone/moddev/mc/advantageworks/unit/ForgeUnitRegistries.java
```

The atlas test uses the same verified Power jar selected for compilation;
it does not look for a second copy in a fixed local folder. Missing or
changed inputs fail the checks. The current source check passes 252 unit
tests. Native resource tests disable interactive terminal setup in their
child process, while keeping warnings and errors fatal.
Keep Java option environment variables unset
when running the packaged family rig; it records its own runtime arguments.

Independent source checks: `node tools/verify-native-port.js` and
`node tools/generate-recipe-unlocks.js --check`.
Hosted CI and CodeQL verify merged dependency sources and checksum-matching
public downloads before compiling.

## Old Worlds

Back up worlds and upgrade disposable copies. Do not install both Steam
Advantage and baseSteam or reopen an upgraded world on an older Minecraft
version. Existing `basesteam-common.toml` settings take precedence.

Steam retains machine IDs and saved fields, drill directions, musket wear
and loading, and all four musket enchantments. The singular datapack folders
do not change logical recipe or advancement identities.

Use basePower's migration rig for whole-world checks. For your own world,
check machines, restarts, redstone, all drill directions, elevators,
GUI captions, whole fluid gauges, track transparency and musket effects
in-game. Release acceptance does not turn the remaining manual checks into
automated passes or qualify every modpack.
See the [1.21.11 notes](docs/PORT12111-STATUS.md). The
[1.21.1 notes](docs/PORT1211-STATUS.md) describe the accepted source baseline,
not qualification of this port.
