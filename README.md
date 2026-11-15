# baseSteam

**baseSteam 2.2.20.121012 for Minecraft 1.21.1 and NeoForge 21.1.265.**

[CurseForge download](https://www.curseforge.com/minecraft/mc-mods/basesteam/files/9626794)
and [GitHub release](https://github.com/MinecraftModDevelopmentMods/baseSteam/releases/tag/2.2.20.121012)
are available. The same release jars are published to MMD Maven.

baseSteam adds boilers, steam-powered machines, pumps, storage, elevators
and a blackpowder musket to basePower. Items and blocks share the basePower
creative tab. The mod ID and resource namespace remain `basesteam`.

## Dependencies

Use Java 21, basePower 2.4.19.121012 and MMDLib 2.0.2.121012+Neo.
Steam's direct Base Metals integration is optional, but the coordinated basePower
stack requires Base Metals 4.0.1.121012+Neo and OreSpawn 4.1.1.121012.
Keep each mod in its own jar and use the NeoForge versions throughout.
This port uses the existing Advantage machine implementation, not MMDLib's
machine framework. Machine speeds, quantities and saved identities are unchanged.

The crusher and drill use basePower's shared MMDLib crushing lookup. The nine
Steam recycling recipe IDs and their APOCALYPTIC mode restrictions are retained.
Shared rock-processing recipes belong to basePower. Removing a recipe through a
datapack removes its behavior; there is no hard-coded recipe fallback.

## Build

The checksum-pinned Gradle 9.8.0 wrapper uses ModDevGradle 2.0.148 and official
Minecraft mappings. Select the published Power jar with
`-PpowerAdvantageJar=<path>`; the build checks its version, loader, API and hash.
The default local development layout uses the checksum-identical jar from a
sibling `../../PowerAdvantage` checkout. Release qualification uses the public
Maven files downloaded by `.github/verify-native-dependencies.ps1` and bound by
`.github/pinned-native-inputs.gradle`.
MMDLib, Base Metals and OreSpawn resolve from MMD Maven. The build verifies the
SHA-256 pins in `gradle.properties`; Base Metals defaults to the published jar.

```powershell
$env:JAVA_HOME = "$env:USERPROFILE/.jdks/temurin-21.0.7+6"
$env:GRADLE_USER_HOME = 'D:/PowerAdvantage/.gradle-steam1211neo'
./gradlew.bat check build verifyReleaseArtifacts writeReleaseChecksums
./gradlew.bat verifyEclipseProductionClasspath verifyEclipseEcj
./gradlew.bat -PsteamGameTestRunId=release-01 runGameTestServer
```

Only run one build at a time against that cache. Use a fresh GameTest run ID
for each attempt so earlier logs are preserved. An explicit `-PbaseMetalsJar=<path>`
override must still match the published filename and checksum. The Power jar
must match its frozen checksum as well. Removing Base Metals
from this Power stack must be rejected by the loader, not treated as a supported
runtime. Independent source checks are:

```powershell
node tools/verify-native-port.js
node tools/generate-recipe-unlocks.js --check
```

Import the checkout as an existing Gradle project in Eclipse/Buildship using
the wrapper and Java 21. `eclipse` prepares processed resources and separate
production/test outputs; ModDevGradle generates the native run configurations.
Use the Gradle `runClient` task to launch the development client.
`verifyEclipseEcj` also compiles the generated project with Eclipse's compiler.
For all three mods together, use Power's `runClientAllAdvantage` task and follow
the sibling Power checkout's `docs/1.21.1-neoforge/TEST-RESULTS.md`.

Local qualification and publication are separate. See the
[NeoForge release notes](docs/PORT1211NEO.md) for the current qualification status.
Hosted CI and CodeQL use only Java 21 and verified public native jars. Power's
merged source, release tag and public Maven hash are recorded; the guards reject
source/tag drift, unpublished inputs and changed bytes. GameTests and ECJ remain
local checks. Hosted input readiness is not Steam publication. The Gradle wrapper
jar and distribution are checksum-pinned.

## Old Worlds

Back up worlds and upgrade disposable copies only. Do not install both Steam
Advantage and baseSteam, mix Forge and NeoForge jars, or reopen an upgraded
world on an older version. Existing `basesteam-common.toml` settings take
precedence. Registry IDs, saved machine fields, drill directions, musket wear
and loading, and all four musket enchantments are retained.

Use basePower's migration rig for whole-world qualification. Automated tests,
packaged server startup, historical-world migration and manual client acceptance
are separate gates. In-game checks cover restarts, redstone, all drill
directions, elevators, GUI captions, whole fluid gauges, track transparency
and musket effects. See [the NeoForge notes](docs/PORT1211NEO.md).
