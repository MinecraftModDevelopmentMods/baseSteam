# baseSteam for Minecraft 1.12.2

baseSteam continues DrCyano's Steam Advantage, adding boilers, brass pipework, storage and steam-powered machines to basePower.

## Upgrading a world

Install [basePower 2.4.4 for Forge 1.12.2](https://www.curseforge.com/minecraft/mc-mods/basepower/files/9452029) and Base Metals. Use the 1.12.2 builds of all mods in your workshop.

baseSteam replaces Steam Advantage on 1.12.2 and can read worlds from Steam Advantage or baseSteam on 1.10.2. Back up your world and config, then replace the old Advantage mods together on a copy of the world. Check your machine inventories, drill directions, steam pressure and unfinished work. Save, close Minecraft completely, and check again after reopening the copy.

[World upgrade guide](https://github.com/MinecraftModDevelopmentMods/basePower/blob/master-1.12.2/docs/1.12-upgrade.md) | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/basesteam)

## Building Minecraft 1.12.2

The `master-1.12.2` build uses ForgeGradle 7.0.34 and Gradle 9.6.1. Run Gradle with Java 17; Gradle resolves the Java 8 toolchain used for compilation.

First build the pinned OreSpawn and sibling basePower development jars:

```text
cd ..\..\PowerAdvantage
gradlew.bat deobfJar
```

Back in the baseSteam checkout, build and audit the release:

```text
gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums
```

Release jars are written to `build/libs`. The deobfuscated development jar is written to `build/libs-dev` by `deobfJar` or `build`.

For Eclipse, import this repository as an existing Gradle project and run:

```text
gradlew.bat cleanEclipse verifyEclipseProductionClasspath
```

basePower is required at compile time and runtime but is never bundled. For a different checkout layout, supply `-PpowerAdvantageDeobfJar=<path>` and `-PoreSpawnDeobfJar=<path>`. The expected commits and hashes are in `gradle.properties`. Base Metals remains a distribution dependency, but its integrations do not require it at compile time.
