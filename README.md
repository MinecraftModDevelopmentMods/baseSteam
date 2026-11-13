# baseSteam

**Minecraft 26.2 Forge, version 2.2.18.2602001. Automated and manual testing complete.**

baseSteam continues DrCyano's Steam Advantage and uses basePower's shared
API, machines and creative tab. Existing `basesteam` registry and saved-data
identities are retained.

The earlier [26.1.2 release](https://www.curseforge.com/minecraft/mc-mods/basesteam/files/9577519)
remains available on CurseForge, MMD Maven and GitHub Releases. The 26.2
release is being prepared separately; the version and dependency pins below
apply to this branch.

This port targets Forge **65.1.0**, official Minecraft **26.2** names,
MCP **20260616.103818**, Java **25.0.3+9**, ForgeGradle **7.0.34** and the
checksum-pinned Gradle **9.6.1** wrapper. Native pack formats are data
**107.1** and client **88.0**.

## Build

The coordinated published dependency versions are basePower **2.4.17.2602001**
and Base Metals **3.1.2.2602001**. The Base Metals hook source is
`523335a59ecff399e40a7c66eb121a3aabdc45ba`, player SHA-256
`15CBD0C8DD95BBA846969929B8767887BA4B8152DFB10C9C58FB314AD39FB9E1`.
Base Metals is REQUIRED on both client and server, not an optional integration.
The released Power262 source is
`98863570ae84c5658a6b8ec2f8878e1f10009192`, player SHA-256
`7E456DE1A45140E86415A701D186FEDE08C4AB63D53C6B7848935DB70CBC68B1`.
Its public CurseForge file is [9583433](https://www.curseforge.com/minecraft/mc-mods/basepower/files/9583433);
Base Metals is [9583122](https://www.curseforge.com/minecraft/mc-mods/base-metals/files/9583122).

OreSpawn **4.1.1.2602001** is the published retained-settings repair,
qualified source `dcaaa933299dd8b1ebc73e31c9e5acd36dd7957c`, SHA-256
`BC0658DF2E149CC23E65E5771F3FF62E2BB13FFDA4E316AB632EB92EF7FA5A1F`.
Its public CurseForge file is [9582731](https://www.curseforge.com/minecraft/mc-mods/orespawn/files/9582731).
The qualified Base Metals and OreSpawn source pins are ancestors of their
released `master-26.2` merges. Their public CurseForge, Maven and GitHub
downloads have been verified against these exact hashes.

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

## Qualification

Combined profile03 passed the machine, migration, actor/dimension, actual
transformer-order and real-client checks, plus two 30-minute worlds, restart
and independent 66-checkpoint replay. See [26.2 port status](docs/PORT262-STATUS.md)
for the counts and exact inputs. The [family qualification record](https://github.com/MinecraftModDevelopmentMods/basePower/blob/master-26.2/docs/26.2/QUALIFICATION.md)
keeps the completed checks and their scope limits together.

The final root build and cold-cache checks passed, and manual testing was
accepted on 12 November 2026. Publication follows the dependency order:
OreSpawn and Base Metals, then basePower, then this expansion. The accepted
26.1.2 records remain historical proof of their original bytes, not a
substitute for the 26.2 results.

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
