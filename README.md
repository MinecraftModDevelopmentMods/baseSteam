# baseSteam

**Minecraft 1.21.1 (Forge), local candidate 2.2.15.121011.**

baseSteam adds boilers, steam-powered machines, pumps, storage, elevators
and a blackpowder musket to basePower. Items and blocks share the basePower
creative tab. The mod ID and resource namespace remain `basesteam`.

## Build

Target: Forge 52.1.0, official Minecraft 1.21.1 mappings and MCP
20240808.132146. ForgeGradle 7.0.34 and the checksum-pinned Gradle 9.6.1
wrapper use Temurin 21.0.7+6. Mavenizer uses Java 25; the launcher helper
also needs Java 8. Minecraft itself runs on Java 21.

The default dependency paths use the coordinated 1.21.1 checkouts. This
candidate needs basePower 2.4.14.121011, Base Metals 3.1.2.121011 and
OreSpawn 4.1.0.121011 as separate native jars, not bundled copies.
Exact local source, jar and test-bootstrap pins are in `gradle.properties`.
They are checked before compilation and tests. Local approval does not
establish public availability.

The Mavenizer fixture comes from OreSpawn's OS1211 checkout. Another layout
can use `-Pforge52MavenizerJar=<path>`. The `*DeobfJar` and `*PlayerJar`
properties select separate development and packaged inputs; both must
satisfy the committed checksums.

With the pinned local dependencies available:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE/.jdks/temurin-21.0.7+6"
$jdk8 = "$env:USERPROFILE/.jdks/temurin-8.0.502+7/jdk8u502-b07"
$jdks = "$env:JAVA_HOME,$env:USERPROFILE/.jdks/jdk-25.0.3+9,$jdk8"
$toolchains = @("-Porg.gradle.java.installations.paths=$jdks",
  '-Porg.gradle.java.installations.auto-detect=false',
  '-Porg.gradle.java.installations.auto-download=false')
./gradlew.bat @toolchains check build deobfJar verifyReleaseArtifacts writeReleaseChecksums
./gradlew.bat @toolchains verifyEclipseProductionClasspath
./gradlew.bat @toolchains -PsteamGameTestRunId=candidate verifyPlayerDependencies runGameTestServer
./tools/run-packaged-server-smoke.ps1 -ForgeInstall $env:FORGE_1211_SERVER_INSTALL -RunId candidate-with-metals
./tools/run-packaged-server-smoke.ps1 -ForgeInstall $env:FORGE_1211_SERVER_INSTALL -RunId candidate-without-metals -WithoutBaseMetals
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
See the [1.21.1 notes](docs/PORT1211-STATUS.md).
