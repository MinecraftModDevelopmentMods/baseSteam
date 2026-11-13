# baseSteam

**Minecraft 26.3 Forge, version 2.2.19.2603001. Published.**

baseSteam continues DrCyano's Steam Advantage and uses basePower's shared
API, machines and creative tab. Existing `basesteam` registry and saved-data
identities are retained.

The 26.3 release is available from
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/basesteam/files/9589874),
[MMD Maven](https://maven.moddev.zone/releases/zone/moddev/mc/basesteam/baseSteam/2.2.19.2603001/baseSteam-2.2.19.2603001.jar)
and [GitHub Releases](https://github.com/MinecraftModDevelopmentMods/baseSteam/releases/tag/2.2.19.2603001).
The [release record](docs/RELEASE263.md) keeps the exact published source,
download hashes and earlier candidate evidence separate. This documentation
update does not rebuild or republish the release.

The target is Forge **66.0.0**, official Minecraft **26.3** names, MCP
**20260918.230105**, Java **25.0.3+9**, ForgeGradle **7.0.34** and the
checksum-pinned Gradle **9.6.1** wrapper. Native data format is **121.0**;
Forge supplies hidden client metadata for resource format **97.1**.

## Build

Required versions are [basePower **2.4.18.2603001**](https://www.curseforge.com/minecraft/mc-mods/basepower/files/9589309),
[Base Metals **3.1.2.2603001**](https://www.curseforge.com/minecraft/mc-mods/base-metals/files/9589195)
and [OreSpawn **4.1.1.2603001**](https://www.curseforge.com/minecraft/mc-mods/orespawn/files/9589145).
Base Metals is REQUIRED on both client and server. These public downloads
match the exact migration-hook and retained-settings builds used by the
combined tests. Source, file IDs and hashes are pinned in `gradle.properties`.
Power's public API source is `4fae0a51`; unit-support source stays independently
pinned at `c0eb83f7` with its original hashes. The exact dependency bytes pass
Steam's complete 291-test suite and build/Eclipse/ECJ gates. Discovery
advancements remain loaded in every mode through exact optional recipe tags,
preserving future unlocks and
saved discovery progress. All four native GameTests pass in all three recipe
modes on this repaired artifact. Whole-table loot uses SHA-checked accepted
goldens. Fresh cold/spaced-input and independent clean builds pass all 291
tests; all three local archives are byte-identical to warm. The published
Linux CI Javadoc has only the documented JDK SVG line-ending difference.
Packaged qualification also passes against the matching three-mod test kit. Manual
testing was accepted on 13 November 2026. Public downloads were independently
verified against the approved release artifacts.
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

See [26.3 port status](docs/PORT263-STATUS.md) for the exact inputs and history.
The [family qualification record](https://github.com/MinecraftModDevelopmentMods/basePower/blob/master-26.3/docs/26.3/QUALIFICATION.md) records the
completed machine, recipe, old-world upgrade, graphical, full-restart and
two-world 30-minute endurance checks. No 26.2 result was substituted for a
26.3 test. Forge 66.1.5 has a separate bounded server check, not the full
baseline suite. Manual acceptance is recorded separately from those automated
results; it does not rewrite the original evidence.

Power and the dependency patches were publicly verified before Steam. Source,
security, artifact, tag and publisher checks passed before release. The public
Steam player is byte-identical to the accepted candidate, so its runtime
qualification was explicitly reused. Steam and Electric are independent
siblings that both require public Power; neither requires the other.
Unit-support sources remain sealed at Power's `c0eb83f7` checkpoint unless
the released source is independently verified to provide the same bytes.

The [published 26.2 record](docs/RELEASE262.md) links its CurseForge, Maven and
GitHub downloads. The [26.1.2 release](https://www.curseforge.com/minecraft/mc-mods/basesteam/files/9577519)
remains available for that Minecraft version.
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
