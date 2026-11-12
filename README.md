# baseSteam

**Minecraft 26.1.2 (Forge), baseSteam 2.2.17.2601021. Local port in progress.**

baseSteam adds boilers, steam-powered machines, pumps, storage, elevators
and a blackpowder musket to basePower. Items and blocks share the basePower
creative tab. The mod ID and resource namespace remain `basesteam`.

## Build

Target: Forge 64.1.5, official Minecraft 26.1.2 names and MCP
20260409.101008, with reobf=false. ForgeGradle 7.0.34 and the checksum-pinned
Gradle 9.6.1 wrapper use Temurin 25.0.3+9. The compiler, Minecraft,
Mavenizer and Slime launcher also use that Java 25 toolchain.

This port requires the coordinated local basePower 2.4.16.2601021 and
Base Metals 3.1.2.2601021 hook, plus released OreSpawn 4.1.0.2601021.
They remain separate Forge jars on both client and server, not bundled copies.
Base Metals is REQUIRED on both client and server, not an optional integration.
The default build paths use the coordinated 26.1.2 checkouts. The current local
inputs are Power source 0ac874de/player 618C, Base Metals source 331fa469/player
678623 and OreSpawn 6D5C. Exact source, jar and fixture08 bootstrap pins are in
gradle.properties. These are local integration inputs, not a public release or
family qualification. No 1.21.11 dependency is substituted.

This port is not yet qualified or accepted for gameplay. The released 1.21.11
baseline and its receipts remain separate historical evidence. Native 26.1.2
metadata declares server data format 101.1 and client format 84.0; the hidden
client resource aggregate remains Forge's responsibility. Actual native reader,
machine, migration, side-loading and GUI checks are required for the new jars.

The sealed BB0D Mavenizer fixture comes from OreSpawn's OS2612 checkout. Another
layout can use `-Pforge64MavenizerJar=<path>`. The `*DeobfJar` and `*PlayerJar`
properties select separate development and packaged inputs; both must
satisfy the committed checksums.

After the coordinated native dependency pins are available locally:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE/.jdks/jdk-25.0.3+9"
$env:GRADLE_USER_HOME = 'D:/GradleCaches/basesteam-2612-port'
$toolchains = @("-Porg.gradle.java.installations.paths=$env:JAVA_HOME",
  '-Porg.gradle.java.installations.auto-detect=false',
  '-Porg.gradle.java.installations.auto-download=false')
./gradlew.bat @toolchains check build deobfJar verifyReleaseArtifacts writeReleaseChecksums
./gradlew.bat @toolchains verifyEclipseProductionClasspath
./gradlew.bat @toolchains -PsteamGameTestRunId=candidate verifyPlayerDependencies runGameTestServer
./tools/run-packaged-server-smoke.ps1 -ForgeInstall $env:FORGE_2612_SERVER_INSTALL -RunId candidate-with-metals -UseNioTransport -Reopen
```

For a checkout outside the sibling-folder layout, unit tests also need the
verified Power test bootstrap and its three source files. Build Power first,
then supply these paths as well as the dependency-jar overrides:

```text
-PforgeCapabilityBootstrapJar=<Power checkout>/build/unit-bootstrap/forge-native-unit-bootstrap.jar
-PforgeCapabilityBootstrapSource=<Power checkout>/test-rig/unit-bootstrap/ForgeCapabilityBootstrap.java
-PforgeUnitRegistriesSource=<Power checkout>/test-rig/unit-support/zone/moddev/mc/advantageworks/unit/ForgeUnitRegistries.java
-Pforge64UnitLoaderSource=<Power checkout>/test-rig/unit-support/zone/moddev/mc/advantageworks/unit/Forge64UnitLoader.java
```

The atlas test uses the same verified Power jar selected for compilation;
it does not look for a second copy in a fixed local folder. Missing or
changed inputs fail the checks. The current local 26.1.2 source passes 276 unit
tests and a fresh independent ECJ run of the same complete inventory. The
scoped runtime and reproducibility results are in the port status below;
they do not imply family or manual acceptance. Native resource tests disable interactive terminal setup in their
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
See the [26.1.2 port status](docs/PORT2612-STATUS.md). The
[1.21.11 notes](docs/PORT12111-STATUS.md) and the
[1.21.1 notes](docs/PORT1211-STATUS.md) describe the accepted source baseline,
not qualification of this port.
