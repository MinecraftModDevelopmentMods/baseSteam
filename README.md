# baseSteam

**Minecraft 26.3 Forge, version 2.2.19.2603001. Local port in progress.**

baseSteam continues DrCyano's Steam Advantage and uses basePower's shared
API, machines and creative tab. Existing `basesteam` registry and saved-data
identities are retained.

The target is Forge **66.0.0**, official Minecraft **26.3** names, MCP
**20260918.230105**, Java **25.0.3+9**, ForgeGradle **7.0.34** and the
checksum-pinned Gradle **9.6.1** wrapper. Native data format is **121.0**;
Forge supplies hidden client metadata for resource format **97.1**.

## Build

Required local versions are basePower **2.4.18.2603001**, Base Metals
**3.1.2.2603001** and OreSpawn **4.1.1.2603001**. Base Metals is REQUIRED on
both client and server. Base Metals and OreSpawn are pinned to the verified
frozen compiler-only handoff; their runtime qualification remains pending.
Power source, JAR and test-support inputs are pinned to the immutable local
handoff recorded in `gradle.properties`. Power handoff07 passes Steam's complete
291-test suite and build/Eclipse gates. Discovery advancements remain loaded in
every mode through exact optional recipe tags, preserving future unlocks and
saved discovery progress. Older ECJ/native/cold results remain historical until
retested on this repaired artifact. Whole-table loot uses SHA-checked accepted
goldens; final cold/repro and packaged qualification remain separate.
Do not substitute 26.2 inputs or the published OreSpawn 4.1.0.

The default sibling Power source root is `../../PowerAdvantage`. Base Metals
and OreSpawn use the existing `BaseMetals 263/BaseMetals` and `OS263/OreSpawn`
folders. Dependencies stay separate mods. Explicit `*DeobfJar` and
`*PlayerJar` flags change paths, not the required bytes.

The Mavenizer fixture is sealed together with its rules, patch, licence and
provenance from the existing OS263 checkout. The historical
`-Pforge64MavenizerJar` property remains a path alias for this Forge66 fixture.
It applies exactly zero source repairs to Forge 66.0.0. The SDK is bare
Forge66; do not restore historical Power access transformers.

Use Temurin 25.0.3+9 without `JAVA_TOOL_OPTIONS`, `JDK_JAVA_OPTIONS` or
`_JAVA_OPTIONS`. The independent Steam cache is
`D:/GradleCaches/basesteam-263-port`. With the exact frozen input paths supplied,
the required commands include:

```text
./gradlew clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums --no-build-cache --no-daemon
./gradlew verifyEclipseProductionClasspath --no-daemon
node tools/verify-native-port.js
node tools/generate-recipe-unlocks.js --check
```

## Qualification

See [26.3 port status](docs/PORT263-STATUS.md) for the current boundaries and
remaining gates. Compilation, full unit/native suites, Eclipse/ECJ,
reproducibility, cold inputs with spaces, packaged runtime, real-client and
manual acceptance are separate proofs. No 26.2 result qualifies this candidate.
Forge 66.1.5 compatibility remains a later gate, not an automatic baseline bump.

This work is local only: no 26.3 push, PR, tag or publication is authorized.
The [26.2](docs/PORT262-STATUS.md), [26.1.2](docs/PORT2612-STATUS.md),
[1.21.11](docs/PORT12111-STATUS.md) and [1.21.1](docs/PORT1211-STATUS.md)
records remain historical proof for their original candidates.

## Old Worlds

Back up worlds and upgrade disposable copies. Do not install both Steam
Advantage and baseSteam or reopen an upgraded world on an older Minecraft
version. Existing `basesteam-common.toml` settings take precedence.

Steam retains machine IDs and saved fields, drill directions, musket wear
and loading, and all four musket enchantments. Singular datapack folders
do not change logical recipe or advancement identities. Recipes, loot,
mining tags, models, languages and unlocks retain their existing contracts.

Use basePower's migration rig for whole-world checks. Check machines,
restarts, redstone, all drill directions, elevators, GUI captions, whole
fluid gauges, track transparency and musket effects in-game. Automated
checks do not establish manual acceptance or qualify every modpack.
