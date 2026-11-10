# baseSteam

**Minecraft 1.20.6 (Forge), version 2.2.14.120061.**

baseSteam, formerly Steam Advantage, adds boilers, steam-powered machines,
pumps, storage, elevators and a blackpowder musket to basePower. Items and
blocks use the shared basePower creative tab.

## Build Target

Minecraft 1.20.6, Forge 50.2.0, official mappings and MCP 20240429.135109.
ForgeGradle 7.0.34 and the Gradle 9.6.1 wrapper use Temurin 21.0.7+6.
Mavenizer uses Java 25 and the checksum-verified fixture from the local OreSpawn
1.20.6 checkout. Set `-Pforge50MavenizerJar=<path>` for a different layout.
ForgeGradle's Slime Launcher also locates a Java 8 toolchain while generating
runs. The game itself runs on Java 21.

Forge 50 uses official names at runtime. Steam does not run an SRG renamer.
Install [basePower 2.4.13.120061](https://www.curseforge.com/minecraft/mc-mods/basepower/files/9545972)
for Minecraft 1.20.6. CurseForge also installs
[Base Metals](https://www.curseforge.com/minecraft/mc-mods/base-metals); this
build was tested with 3.1.2.120061 and OreSpawn 4.1.0.120061. The same native
jars are used for play and development, and none are bundled.
The mod ID, data namespace, and registry paths remain `basesteam`.

## Local Verification

The default paths point to the coordinated local 1.20.6 checkouts, including
the Base Metals shared-hook checkout. The
`*DeobfJar` project properties remain accepted path overrides even though the
target uses native jars. For packaged checks, the `*PlayerJar` overrides
are separate. Both paths must satisfy the pins in `gradle.properties`.
The unit gate also verifies the Power bootstrap jar and both source hashes.
Absent or mismatched inputs fail closed.

From this checkout in PowerShell, with the pinned local JDKs installed:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE/.jdks/temurin-21.0.7+6"
$jdk8 = "$env:USERPROFILE/.jdks/temurin-8.0.502+7/jdk8u502-b07"
$jdks = "$env:JAVA_HOME,$env:USERPROFILE/.jdks/jdk-25.0.3+9,$jdk8"
$toolchains = @("-Porg.gradle.java.installations.paths=$jdks",
  '-Porg.gradle.java.installations.auto-detect=false',
  '-Porg.gradle.java.installations.auto-download=false')
./gradlew.bat @toolchains check build deobfJar verifyReleaseArtifacts writeReleaseChecksums
./gradlew.bat @toolchains -PsteamGameTestRunId=candidate verifyPlayerDependencies runGameTestServer
./tools/run-packaged-server-smoke.ps1 -ForgeInstall $env:FORGE_1206_SERVER_INSTALL -RunId candidate-with-basemetals
```

The GameTest run uses `build/steam-gametest-<id>` and a validation-only source
set. It checks drill-bit packets, legacy musket componentization, the shared
durability migration, and loaded recipe, advancement, and loot resources.
The packaged dedicated smoke is a separate gate. It checks the official Forge
server shim and the pinned player jars, then boots a fresh loopback-only world
under `evidence/packaged-server-<RunId>`. Use a new run ID on each attempt.
The optional no-Base-Metals profile uses `-WithoutBaseMetals` and retains
OreSpawn because basePower requires it.
`node tools/verify-native-port.js` and
`node tools/generate-recipe-unlocks.js --check` are independent source checks.
See the [1.20.6 notes](docs/PORT1206-STATUS.md) for save compatibility and test coverage.

## Upgrading a World

Back up the world and upgrade a disposable copy. Do not install both Steam
Advantage and baseSteam or reopen an upgraded world on an older Minecraft
version. Use basePower's coordinated migration rig for whole-world checks.
Existing `basesteam-common.toml` settings take precedence over legacy settings.

Vanilla's data fixer moves 1.20.1 item wear into the damage component and
retains the musket's loaded byte in custom data. The shared migration hook
also recognizes the old `steamadvantage:musket` ID and top-level wear.
Steam's load cycle changes only its loaded byte. Machines retain their
registry IDs, persistent keys, aliases and native factory coordinates.

Before accepting the port, check boilers, crusher/furnace restart and
redstone, drill movement in all six directions, pumps, stills, elevator rides,
GUI captions, track transparency, and musket load, wear, and effects in-game.
Client presentation and multiplayer behavior need manual acceptance even
when automated checks pass. Earlier-version receipts do not qualify this port.
