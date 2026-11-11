# baseSteam

**Minecraft 1.21.11 (Forge), local source port 2.2.16.121111.**

baseSteam adds boilers, steam-powered machines, pumps, storage, elevators
and a blackpowder musket to basePower. Items and blocks share the basePower
creative tab. The mod ID and resource namespace remain `basesteam`.

## Build

Target: Forge 61.1.0, official Minecraft 1.21.11 mappings and MCP
20251223.124241. ForgeGradle 7.0.34 and the checksum-pinned Gradle 9.6.1
wrapper use Temurin 21.0.7+6. Mavenizer uses Java 25; the launcher helper
also needs Java 8. Minecraft itself runs on Java 21.

The default dependency paths use the coordinated 1.21.11 checkouts. This
candidate needs basePower 2.4.15.121111, Base Metals 3.1.2.121111 and
OreSpawn 4.1.0.121111 as separate native jars, not bundled copies.
Exact local source, jar and test-bootstrap pins are in `gradle.properties`.
They are checked before compilation and tests. Local approval does not
establish public availability. The Base Metals shared hook and approved Power
integration commit 398439074a60bf9b8f8872f92485cffc4aa96be9 are pinned locally.
The mod root declares server data format 94.1. Forge supplies format 75.x
metadata for its hidden client resource aggregate. Native resource-reader
tests cover both paths without codec warnings; see
[Forge metadata notes](docs/FORGE61-RESOURCE-METADATA.md) for the separate
Forge 61.1.0 server compatibility-label defect.

The Mavenizer fixture comes from OreSpawn's OS12111 checkout. Another layout
can use `-Pforge61MavenizerJar=<path>`. The `*DeobfJar` and `*PlayerJar`
properties select separate development and packaged inputs; both must
satisfy the committed checksums.

With the pinned local dependencies available:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE/.jdks/temurin-21.0.7+6"
$env:GRADLE_USER_HOME = 'D:/GradleCaches/basesteam-12111-port'
$env:JAVA_TOOL_OPTIONS = '-XX:ActiveProcessorCount=4'
$jdk8 = "$env:USERPROFILE/.jdks/temurin-8.0.502+7/jdk8u502-b07"
$jdks = "$env:JAVA_HOME,$env:USERPROFILE/.jdks/jdk-25.0.3+9,$jdk8"
$toolchains = @("-Porg.gradle.java.installations.paths=$jdks",
  '-Porg.gradle.java.installations.auto-detect=false',
  '-Porg.gradle.java.installations.auto-download=false')
./gradlew.bat @toolchains check build deobfJar verifyReleaseArtifacts writeReleaseChecksums
./gradlew.bat @toolchains verifyEclipseProductionClasspath
./gradlew.bat @toolchains -PsteamGameTestRunId=candidate verifyPlayerDependencies runGameTestServer
./tools/run-packaged-server-smoke.ps1 -ForgeInstall $env:FORGE_12111_SERVER_INSTALL -RunId candidate-with-metals
./tools/run-packaged-server-smoke.ps1 -ForgeInstall $env:FORGE_12111_SERVER_INSTALL -RunId candidate-without-metals -WithoutBaseMetals
```

Independent source checks: `node tools/verify-native-port.js` and
`node tools/generate-recipe-unlocks.js --check`.
Hosted CI and CodeQL require merged sources and checksum-matching public
downloads before compiling. Pending local pins cannot pass that gate.

## Old Worlds

Back up worlds and upgrade disposable copies. Do not install both Steam
Advantage and baseSteam or reopen an upgraded world on an older Minecraft
version. Existing `basesteam-common.toml` settings take precedence.

Steam retains machine IDs and saved fields, drill directions, musket wear
and loading, and all four musket enchantments. The singular datapack folders
do not change logical recipe or advancement identities.

Use basePower's migration rig for whole-world checks. Before accepting the
port, check machines, restarts, redstone, all drill directions, elevators,
GUI captions, whole fluid gauges, track transparency and musket effects
in-game. Automated, packaged and manual acceptance are separate gates.
See the [1.21.11 notes](docs/PORT12111-STATUS.md). The
[1.21.1 notes](docs/PORT1211-STATUS.md) describe the accepted source baseline,
not qualification of this port.
