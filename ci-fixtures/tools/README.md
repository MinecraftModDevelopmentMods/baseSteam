# Forge Mavenizer compatibility fixtures

## Forge 66 (Minecraft 26.3)

The active build selects the sealed fixture and all five provenance inputs
from the read-only sibling `OS263/OreSpawn/ci-fixtures/tools` directory.
The historical `-Pforge64MavenizerJar` property is a path alias, not a choice
of Minecraft target. Every selected input must match the hashes in
`gradle/forge64-mavenizer.gradle`; there is no stock-tool fallback.

JAR SHA-256:
`4150926F33A696B2088AB2675C1C3B45745CE4F9F2DEB31CCD88C9ADC834062F`.
Rules SHA-256:
`829400D74ECB1BF0CC6107D9980B8328365E3F56D9A5342002D8DF340571BDB7`.
The Forge 66.0.0 target applies exactly zero source repairs. The older tracked
rule, patch and licence files below remain historical fixtures; their hashes
do not qualify them as the active Forge 66 tool.

## Forge 45 (Minecraft 1.19.4)

The source patch and target-rule manifest match the Base Metals 1.19.4
fixture. The binary derivative is not tracked here. The build requires the
verified JAR from the local OreSpawn 1.19.4 checkout, or an explicit
`-Pforge45MavenizerJar=<absolute-path>`. CI gets it from the pinned OreSpawn
checkout. There is no stock Mavenizer fallback.
The derivative is based on MinecraftForge MinecraftMavenizer 0.5.21 at
`6968241ce7a0a902cdc1c534b976e8373a423091`. Its LGPL-2.1 license is in
`LICENSE-MAVENIZER.txt`.

Forge 45 decompilation emits eight redundant record accessors in
`Holder.Direct`, `OptionInstance` and `MemoryCondition`. The target rules
remove those accessors, which javac regenerates. Missing or ambiguous rule
matches stop the build. The fixture also forwards
`ORESPAWN_MAVENIZER_OFFLINE=true` as the native Mavenizer offline flag.
The checked-in source patch rebuilds against the upstream commit with
Temurin 25.0.3+9 and the upstream Gradle 9.1.0 wrapper.

The derivative is build-only; no fixture bytes ship in baseSteam artifacts.
JAR SHA-256: `341B3FB10ABA6EA7DC612FBE519DC290A1C81867AED5DD320ACD783688AC49DE`.

## Forge 40 (historical, Minecraft 1.18.2)

`minecraft-mavenizer-0.5.21-orespawn-1.18.jar` is a build-only derivative of
MinecraftForge MinecraftMavenizer `0.5.21`, source commit
`6968241ce7a0a902cdc1c534b976e8373a423091`.

Forge 40's 1.18.2 decompiler emits a redundant explicit `Holder.Direct#value`
record accessor. The complete patched Minecraft source tree is rejected by
modern `javac`; removing that method is safe because the record generates the
same public accessor automatically. The derivative also honors
`ORESPAWN_MAVENIZER_OFFLINE=true` by adding Mavenizer's native `--offline`
argument. This closes ForgeGradle 7's failure to propagate Gradle offline mode
to the subprocess; it does not alter normal online builds.

`minecraft-1.18-holder-accessor.patch` documents the complete two-file source
change.

The fixture is used only while ForgeGradle prepares the development dependency.
This copy comes from IAF118. It is checksum-verified by the baseSteam build
and excluded from every baseSteam release artifact.
MinecraftMavenizer remains licensed LGPL-2.1-only; see `LICENSE-MAVENIZER.txt`.

SHA-256:
`28A6697BDF6D9500EC0AF9C1C949B9F3ED7DDFDA649CB5CAC4BEEA2E9567570A`
