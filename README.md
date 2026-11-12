# baseSteam

**Minecraft 26.2 Forge, version 2.2.18.2602001. Local build candidate.**

baseSteam continues DrCyano's Steam Advantage and uses basePower's shared
API, machines and creative tab. Existing `basesteam` registry and saved-data
identities are retained.

This port targets Forge **65.1.0**, official Minecraft **26.2** names,
MCP **20260616.103818**, Java **25.0.3+9**, ForgeGradle **7.0.34** and the
checksum-pinned Gradle **9.6.1** wrapper. Native pack formats are data
**107.1** and client **88.0**.

## Build

The coordinated local dependency versions are basePower **2.4.17.2602001**
and Base Metals **3.1.2.2602001**. The Base Metals hook source is
`523335a59ecff399e40a7c66eb121a3aabdc45ba`, player SHA-256
`15CBD0C8DD95BBA846969929B8767887BA4B8152DFB10C9C58FB314AD39FB9E1`.
Base Metals is REQUIRED on both client and server, not an optional integration.
The approved local Power262 handoff is source
`f84b2362449e39eff5e76696dd64ce4cb144da4f`, player SHA-256
`7E456DE1A45140E86415A701D186FEDE08C4AB63D53C6B7848935DB70CBC68B1`.

OreSpawn **4.1.1.2602001** is the approved local retained-settings repair,
source `dcaaa933299dd8b1ebc73e31c9e5acd36dd7957c`, SHA-256
`BC0658DF2E149CC23E65E5771F3FF62E2BB13FFDA4E316AB632EB92EF7FA5A1F`.
It is not published; the public file-ID pin is deliberately empty. The older
4.1.0 public download is not substituted for these bytes.

The Mavenizer fixture and its source-rule, patch, licence and provenance files
are checksum-sealed from the existing OS262 checkout. The established
`-Pforge64MavenizerJar` property remains a path alias; it selects the Forge65
fixture here. Dependencies stay separate mods and must match the source and
artifact pins in `gradle.properties`.

Use Temurin 25 and clear `JAVA_TOOL_OPTIONS`, `JDK_JAVA_OPTIONS` and
`_JAVA_OPTIONS` before qualification. The prepared sibling layout is the
default; explicit `*DeobfJar` and `*PlayerJar` flags change
paths, not the required bytes. With these exact coordinated inputs:

```text
./gradlew clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums --no-build-cache --no-daemon
./gradlew verifyEclipseProductionClasspath --no-daemon
node tools/verify-native-port.js
node tools/generate-recipe-unlocks.js --check
```

See [26.2 port status](docs/PORT262-STATUS.md). The accepted 26.1.2 family
qualification remains historical proof of those original bytes, not this port.
There is no 26.2 manual acceptance, hosted qualification or publication yet.

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
The [26.1.2 port status](docs/PORT2612-STATUS.md) is historical. The
[1.21.11 notes](docs/PORT12111-STATUS.md) and the
[1.21.1 notes](docs/PORT1211-STATUS.md) describe the accepted source baseline,
not qualification of this port.
