# baseSteam

baseSteam continues DrCyano's Steam Advantage, adding boilers, steam networks and steam-powered machines to basePower for Minecraft 1.10.2.

Version `2.2.6.110021` renames Steam Advantage to baseSteam, with mod ID `basesteam` and
implementation namespace `zone.moddev.mc.basesteam`. It requires basePower `2.4.4.110021` or newer.
Replace both old jars together; do not install the old and new mods side by side.
The rename changes no recipes, machine ratings, fuel values, or musket balance.

## Release Status

This version is not published. The local checks include 135 Steam unit tests, original-release
migration fixtures, operating and full-restart checks, and finite-supply Steam endurance runs.
The [cross-mod qualification notes](https://github.com/MinecraftModDevelopmentMods/basePower/blob/master-1.10.2/docs/1.10/base-qualification.md)
record the tested jar checksums and limits. These checks do not replace manual gameplay acceptance.

The cross-mod [1.10.2 Handbook](https://github.com/MinecraftModDevelopmentMods/basePower/blob/master-1.10.2/docs/1.10/README.md) includes the Steam Engineer's Handbook, rated figures, commissioning layouts, and persistence tests. The [MMD material and fluid compatibility specification](https://github.com/MinecraftModDevelopmentMods/basePower/blob/master-1.10.2/docs/1.10/mmd-material-fluid-compatibility.md) defines crude-oil aliases and OreSpawn ownership.

## Existing Worlds

Back up worlds and configuration before migration, and test a copy first.
Original `steamadvantage` blocks, items, and tile IDs are accepted; new tile saves use `basesteam`.
`steamadvantage.cfg` is copied to `basesteam.cfg` only when the latter is absent; the original is retained.
Old Power oil fuel-setting keys still work, with raw fluid names and current keys taking priority.
Custom additional-loot-table files are imported without replacing existing baseSteam files, and the old
bytes are retained as inactive `*.json.before-basesteam` backups. Existing loot files are never replaced by defaults.

The four enchantment registry keys deliberately remain `steamadvantage:*`, preserving the original
numeric allocation order and Forge's saved enchantment-registry snapshot. This is a persistence
exception, not a second legacy mod container. This 1.10 version uses hitscan muskets, not saved projectile entities.
See [migration details and test coverage](docs/1.10-identity-migration.md).

## Drill Reload Fix

Version `2.2.5.110021` keeps unfinished drill work when a chunk or world reloads.
Existing inventories, energy, orientation and saved-world names are unchanged.
The regression tests cover resumed progress and full server restarts.

## Building Minecraft 1.10.2

The `master-1.10.2` build uses ForgeGradle 7.0.34 and Gradle 9.6.1. Run Gradle with Java 17; Gradle resolves the Java 8 toolchain used for compilation.

Build the pinned OreSpawn commit `5a50df1158e948c8db55814e819090b34c12d765` first. Then build the pinned sibling basePower development jar (the local checkout directory remains `PowerAdvantage`):

```text
cd ..\..\PowerAdvantage
gradlew.bat deobfJar
```

The dependency locations can be overridden with `-PoreSpawnDeobfJar=<path>` and `-PpowerAdvantageDeobfJar=<path>`.

Then build and audit baseSteam:

```text
gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums
```

Release jars are written to `build/libs`. The deobfuscated development jar is written to `build/libs-dev` by `deobfJar` or `build`.

For Eclipse, import this repository as an existing Gradle project and run:

```text
gradlew.bat cleanEclipse verifyEclipseProductionClasspath
```

The expected development artifact is `basePower-2.4.4.110021-deobf.jar`. Its SHA-256 is pinned in
`gradle.properties`, the build audit, CI and CodeQL, together with source commit
`425af862ef5b41b61d3e1f7e665d1d691852ff4b` from the MMD basePower repository.
The build checks both pins. Do not substitute the old Power Advantage jar or a moving branch.

basePower is required at compile time and runtime but is never bundled. OreSpawn is required transitively
by basePower. Base Metals remains a required distribution dependency while its integrations stay reflection-based
and absent from the compile classpath. The stable `cyano.poweradvantage.api` and required `com.mcmoddev`
compatibility types are not renamed.

CurseForge relations are `basepower` and `base-metals`. No new CurseForge project is assigned:
`curseforge_project_id=0` deliberately blocks publication and release validation until
a new project is assigned and the identity audit is updated. The former project ID must not be reused.
