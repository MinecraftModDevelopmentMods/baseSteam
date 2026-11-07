# baseSteam 1.17.1 Port Status

## Current Identity Candidate

`2.2.10.117011`, branch `feature/basesteam-1.17.1-identity-migration`,
based on the native Steam Advantage port at `f16b4d3`. This changes the mod,
package, archive and resource identities without changing registry paths or
machine behavior. The shared basePower migration hook recognizes the old
`steamadvantage` namespace and dotted block-entity IDs for both namespaces.
The existing state codecs and NBT repairs remain in place.

The exact basePower 2.4.9.117011 source and development jar pins are pending.
The earlier pins below cannot be reused.

Base Metals 3.1.1.117011 is pinned to local, unpushed source
`0deeeeecdbbc1fbcdecf910c59477de8efe164d7`. The dependency owner reports that
its Gradle release and Eclipse suite passed. The supplied development jar was
independently checked against SHA-256
`83A8C32C633B56BB7E4405E8B3B24A4DEA32B9119216E5097B884056D679CAD1`.
This is not a public runtime hash. Hosted builds still require checkout of
the exact pinned commit; no remote fallback is allowed.

Source checks passed without Gradle: 179 JSON resources, 12 native factories,
17 harvest entries and 24 recipe unlocks. The baseline comparison found only
identity edits in 61 production Java files and 178 JSON files; all 39 binary
assets were byte-identical. Workflow YAML and shell syntax passed, and both
dependency setup workflows rejected the pending basePower pin. An isolated
XML fixture reproduced the original Eclipse repeat-refresh failure and passed
with the fix, retaining the launcher, classpath order and arguments.

Gradle compilation, Java tests, repeat Eclipse refresh against real inputs,
packaged runtime, copied-world upgrade and manual acceptance are pending.

## Historical Steam Advantage Record

The remaining sections document the native Advantage-named candidate and its
retained evidence. Their artifacts, build results and pins do not qualify
baseSteam 2.2.10.117011.

Local source candidate: `2.2.9.117011`, branch
`feature/steamadvantage-1.17.1-port`, baseline
`ed808f9fe9a2398d2334c0cadb1546284a02a22e`.

The parent locally committed the original source checkpoint as
`e5dd65cd59b14cba4d072d2a141cc4d89f224eef`. This update records the coordinated
advancement correction, regression tests and coordinated Power repins.
The user approved a local-only Steam commit after the new full gates pass,
excluding the local agent file list. No port code push, PR, merge, tag or
publication is authorized before manual testing and separate approval.

## Completed Source Work

- Ported native imports in production and tests; checked the target classes
  and changed native signatures against mapped Forge 37.1.1 and the published
  Base Metals 1.17 source.
- Ported all twelve block-entity factories to native position/state suppliers.
  The eleven machines use the agreed Power constructors. Detached convenience
  constructors use the corresponding real block's default state, never a
  production factory or saved-position replacement.
- Used native tag-only loading. Drill movement retains the destination's
  constructor position and state. The drill-bit helper has an explicit ticker;
  machine ticking remains owned by Power's shared GUI parents.
- Ported renderer registration, PoseStack/vertex consumers, shader texture
  binding, item model predicates, config loading, merchant listings, slot
  callbacks and source-fluid pickup without replacing waterlogged hosts.
- Preserved all boiler water guards and dirty-owner callbacks, loaded-tick
  crusher/furnace redstone sampling, persistence, machine costs and GUI rows.
- Bounded musket terrain clipping to loaded chunks using native voxel traversal;
  retained loaded-server-entity targeting and loaded-player custom sound packets.
- Used native Forge lava temperature with the existing vanilla heat formula.
- Added native pickaxe tags for seventeen blocks without introducing new tool
  tiers or mandatory drop tools. Ordinary self-drops and hidden helper behavior
  are unchanged.
- Updated build metadata, class-major audit and Eclipse environment audit to
  Java 16 / class major 60. CI uses Java 17 Gradle, Java 25 Mavenizer, Java 16
  production and Java 8 only for legacy build utilities. Hosted CI is not run.

## Dependency Coordination

Base Metals 3.1.0.117011 source:
`dacfaa3af2e3310d5cb6b081da79f09260ae103d`.
Development jar SHA-256:
`4EB49B12ED17211296F5FEC67FC4307174702BE096D3F1C02DD67B45AED587FA`.

OreSpawn 4.1.0.117011 source:
`b56e2b62a1a6ea4d1dd96f8646a87801a817dbdf`.
Development jar SHA-256:
`CB7A736314842B110B2E4788FBD5E21D4AE8803C5ED34A49199B034BE572DA39`.

Both development jars were independently SHA-256 checked at their supplied
campaign API source paths. Public runtime hashes remain separate in
`gradle.properties`. No dependency is embedded.

Power 2.4.8.117011 is pinned to the parent's local checkpoint
`5b9abe578b053d2ef59b1962cf9e04f6880c91b9`, API jar SHA-256
`799B59B06EB2AC3FC4682AA64BF42C3A2A2C1D89D7FFFE9C72745DEC7DFBE0D8`.
The corresponding Power production jar SHA-256 is
`336D9A11F5488EBD63F208554A039A53A398ABC58F632880B8420AF10F5AFBF0`.
The parent's HEAD and both actual jar hashes were independently verified before
this explicit repin. The new API input is
`evidence/inputs/PowerAdvantage-2.4.8.117011-api-checkpoint-799B59B0.jar`.
The verified input is copied under `evidence/inputs` so later parent builds
cannot silently change the bytes used by Steam. This is a local checkpoint,
not final runtime acceptance. No old-target Power jar has been used, and no
extra shared API or constructor workaround was needed.
Power marks loaded chunks unsaved when persistent machine state
changes through server ticking or energy updates. The current checkpoint also
corrects two exact native registry aliases and the Block registry listener, with
no public API change. Its 201-test parent build is not a full packaged runtime
pass. Steam has no local timer or weapon-code fix.

Plain JUnit uses Power's verified test-only native capability bootstrap, SHA-256
`0F75844C30D3E0B4FAF43EE0905FABE5553D9CC32177FF0BB3A690088208701A`.
It invokes Forge's own token transformer; it is not a capability replacement
and is absent from production artifacts and launch classpaths.

The coordinated campaign serialized builds through its own cache-lease wrapper.
That internal arrangement is not a build prerequisite for other workspaces;
the README provides portable wrapper commands, explicit JDK paths and API overrides.

## Checks Actually Run

- `node tools/verify-native-port.js`: source pass, 179 JSON resources,
  63 production Java files, 12 factories and 17 harvest entries.
- `node tools/generate-recipe-unlocks.js --check`: 24 unlocks verified.
- Baseline comparison: all 24 original test classes retain their 111 declared
  test methods. Thirteen new methods bring the declared total to 124. This is not
  an executed-test count and does not expand parameterized cases.
- Literal-key comparison: all 99 NBT/key accesses across the 63 production
  Java files retain their original names.
- The parent corrected 21 ingredient predicates in 17 advancement files from
  ignored singular `item` fields to native singleton `items` arrays. Recipe
  ingredients, processing quantities and loot were not changed. Four new unlock
  tests reject the old shape, broad or malformed arrays, and native ANY matching.
- `git diff --check`: pass.
- Historical checkpoint run `steam-checkpoint-build-f`: `check build javadoc
  verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums
  verifyEclipseProductionClasspath` passed against the exact checkpoint inputs.
  All 191 executed tests in 27 suites passed, with zero failures, errors or skips.
- `steam-checkpoint-repro-g`: the same full gate passed with `--offline
  --rerun-tasks`, all 32 tasks executed, and all 191 tests passed again. Production,
  sources, Javadoc and development jars are byte-for-byte identical to run F.
  This is a forced offline repeat build, not an empty-cache qualification.
- The artifact audit verified Java class major 60, metadata, resources, exact
  dependencies, no embedded dependencies and canonical deterministic archives.
- Eclipse configuration checks passed for Java 16 source, target, compliance
  and JRE, test-code exclusion, native Forge 37 bootstrap and module access.
  This is not an Eclipse client-launch or visual acceptance result.
- Previous coordinated runs `steam-advancement-repin-h` and
  `steam-advancement-repro-i` each passed the same full task list with
  `--offline --rerun-tasks`: 32 tasks executed, 195 tests in 27 suites, zero
  failures, errors or skips. These results use the earlier Power source/API pin
  `5aaae99596c0891def8072192e0addb73c278fce`, not the shared-persistence pin.
  The four jars are identical across H and I; all 322 frozen build/source inputs
  were rechecked unchanged. This is not an empty-cache qualification.
- The packaged-content delta from the retained F production jar contains
  exactly the 17 parent-corrected advancement JSON entries, with no added or
  removed entries. All 71 production class entries, including weapon code,
  are byte-identical. Javadoc remains identical too.
- Earlier runs `steam-power-fc4c47f-j` and `steam-power-fc4c47f-repro-k` each
  passed `check build deobfJar javadoc verifyReleaseDependencies
  verifyReleaseArtifacts writeReleaseChecksums verifyEclipseProductionClasspath`
  with `--offline --rerun-tasks`, using the exact `fc4c47f2` Power source/API pin.
  Each executed all 32 tasks and all 195 tests in 27 suites with zero failures,
  errors or skips. All 322 frozen build/source inputs stayed unchanged.
  Production, development, sources and Javadoc jars are identical across J/K
  and to the preceding H/I advancement-fix candidate. Steam production Java,
  recipes and loot remain unchanged from `e5dd65c`.

## Optional Provider Guard Candidate

The absent-Base-Metals APOCALYPTIC worldgen attempt found nine unsupported
`basemetals:crushing` recipe errors. Their resource bytes were unchanged from
1.16; they had only a recipe-mode condition. Each now also requires Forge's
native `forge:mod_loaded` condition for `basemetals`, matching the Power and
Electric resource pattern. Inputs, outputs, quantities and the tank recipe's
implicit single output are unchanged. No production Java changed.

Four new regression tests cover all nine resources, malformed-provider negative
controls, the actual Forge 37 condition serializer and exact processing payloads.
`steam-optional-crushing-red-b` retained the expected missing-guard failure;
`steam-optional-crushing-release-c` passed all 199 tests in 28 suites and the full
32-task release, Javadoc and Eclipse configuration gate, without build-cache reuse.
The earlier red-A receipt also remains: its additional failure was a new-test
assumption about an explicit tank count, corrected to preserve the real omitted
default-one representation rather than changing the recipe.

This fix changes resource/archive hashes. Per-commit exports, native absent-provider
startup, ECJ execution and fresh runtime reports must identify these new bytes;
the old frozen-J equality below does not qualify them. Combined current runtime
status belongs to Power Advantage's `docs/1.17/CURRENT-QUALIFICATION.md`, whose
exact artifact pins and scoped results are authoritative. Manual acceptance and
publication approval remain separate.

## Native-Registry Archived Candidate

The following build and equality record describes the earlier `f9fd1ec` source,
before the optional-provider guard fix. It is retained evidence, not the current
artifact's resource hash.

`steam-power-5b9abe5-m` passed the complete Steam build, release, Javadoc and
Eclipse configuration suite with `--offline --no-build-cache --rerun-tasks`:
32 tasks executed, 195 tests in 27 suites, no failures, errors or skips.
The exact native-registry Power source/API pin is recorded above. All four
Steam jars remain byte-identical to frozen J: production `A07B14C1...`,
development `F352BC04...`, sources `3A5CF129...` and Javadoc `122F4666...`.
Their complete hashes and JUnit XML/HTML are retained in
`evidence/steam-power-5b9abe5-m`.

The source commit's independent clean exports are a separate campaign gate,
recorded under the campaign's `Steam Repro <commit>` directory with a dedicated
empty Gradle user home, command receipts, all four byte comparisons and JUnit
snapshots. A primary-checkout rerun is not an independent export qualification.

The preceding `f4818bf` / Power `fc4c47f2` checkpoint independently passed both
spaced-path exports, including its initially empty Gradle cache: 195 tests and
33 tasks per export, all four jars byte-exact to J. Its immutable handoff is
`Steam Repro f4818bf/handoff.json`; it does not qualify a newer source/API pin.

## Persistence-Fix Archived Candidate

This retained candidate used Power `fc4c47f2ed8cfebbaa010e6a0e807137a3329f95`,
API SHA-256 `BDF486E16C1FE3B4CE2FE1ED78670AABA594063BCD2D4251265124B17FA9E767`.
It does not qualify the newer native-registry Power pin.

Full evidence for the shared-persistence Power pin is retained in
`evidence/steam-power-fc4c47f-j`; the independent forced offline repeat is in
`evidence/steam-power-fc4c47f-repro-k`. The frozen archive is
`evidence/steam-power-fc4c47f-j.zip`, with a separate SHA-256 report alongside it.
The committed source checkpoint and input pins are recorded in `candidate.json`.
The local agent file list is excluded from both the source snapshot and commit.

Artifact SHA-256 values:

- Production `artifacts/SteamAdvantage-2.2.9.117011.jar`:
  `A07B14C1442D6A9B2FEF86C1892564BBBD94561D4E9711539A992CB56D7BE2C6`.
- Development `SteamAdvantage-2.2.9.117011-deobf.jar`:
  `F352BC0400E27742652073ABB5A49A621E5A6892CEA7D82254216AE54F8629A7`.
- Sources `artifacts/SteamAdvantage-2.2.9.117011-sources.jar`:
  `3A5CF1296418A6A4F03B1AB7ABE5191A5819EB38CC3F072A6734512FABEDCD85`.
- Javadoc `artifacts/SteamAdvantage-2.2.9.117011-javadoc.jar`:
  `122F466637427788D3EB3FC52990F7E930546AD3EFFCFE9C45F842C79F4316DB`.

The archive binds the local source commit, exact Power API/bootstrap inputs,
Base Metals/OreSpawn APIs, configuration/source snapshots, artifact comparisons,
JUnit XML/HTML, Gradle command receipts and generated Eclipse files. These are
local build and reproducibility results, not runtime or publication acceptance.

## Advancement-Fix Archived Candidate

This candidate used Power `5aaae99596c0891def8072192e0addb73c278fce`, API
SHA-256 `38162B8B1D3B5117E9764A29F04C5A2482FD016D5700D728F9DC12ECCAEB0963`.
It does not qualify the new shared-persistence Power pin. Full evidence is retained in
`evidence/steam-advancement-repin-h`; the independent repeat is retained in
`evidence/steam-advancement-repro-i`. The frozen candidate archive is
`evidence/steam-advancement-repin-h.zip`, with its SHA-256 recorded separately
alongside it. No publication or runtime acceptance is implied by this archive.

Artifact SHA-256 values:

- Production `artifacts/SteamAdvantage-2.2.9.117011.jar`:
  `A07B14C1442D6A9B2FEF86C1892564BBBD94561D4E9711539A992CB56D7BE2C6`.
- Development `SteamAdvantage-2.2.9.117011-deobf.jar`:
  `F352BC0400E27742652073ABB5A49A621E5A6892CEA7D82254216AE54F8629A7`.
- Sources `artifacts/SteamAdvantage-2.2.9.117011-sources.jar`:
  `3A5CF1296418A6A4F03B1AB7ABE5191A5819EB38CC3F072A6734512FABEDCD85`.
- Javadoc `artifacts/SteamAdvantage-2.2.9.117011-javadoc.jar`:
  `122F466637427788D3EB3FC52990F7E930546AD3EFFCFE9C45F842C79F4316DB`.

The archive includes exact API/bootstrap inputs, source and configuration
snapshots, the uncommitted patch/change list, hashes, JUnit XML/HTML, Gradle
command receipts and generated Eclipse files. `candidate.json` and
`artifact-delta.json` record the input pins and packaged-content boundary.

## Earlier Archived Candidate

This retained F/G candidate used Power checkpoint
`28247f936ac99e7a417da1bc1c89bc8394773a26`, API SHA-256
`7B2B2676CDB9A0B933DA04CC743787803C68E1466FD44E2EFBAEFB3ED47ABB3D`.
It does not qualify the coordinated advancement correction or the new Power pin.

Production jar:
`evidence/steam-checkpoint-build-f/artifacts/SteamAdvantage-2.2.9.117011.jar`

SHA-256:
`8270BE3399EEA1F3E63446EA9E256C7C227FAA2BA78410B439942FA4FD067F72`

Development jar:
`evidence/steam-checkpoint-build-f/SteamAdvantage-2.2.9.117011-deobf.jar`

SHA-256:
`43FE3EC41392142B0ACAC01961E4E96A7E295AB058CE369B2E2058BB6DFDFCF4`

The full artifact family, checksums, JUnit XML/HTML, generated Eclipse files
and pinned properties are retained in the F evidence directory. Run G's tests
and checksums are retained separately. Command logs and machine-readable run
receipts are in the parent campaign directory.

## Retained Failed Runs

- `steam-native-test-a`: main/test compilation passed, but standalone registry
  bootstrap failed because the native game version was not initialized.
  The fixtures now call `SharedConstants.tryDetectVersion()` before bootstrap;
  the failed XML and HTML reports remain in `evidence/steam-native-test-a`.
- `steam-native-test-b`: rejected a mutable Power jar whose hash changed from
  the approved provisional `5D0B363F...` to `7B2B2676...`. No silent repin was
  made. Run C used an independently rechecked copy of the originally approved
  provisional bytes and passed 191 tests. That result is not checkpoint proof.
- `steam-native-audit-d`: cancelled while queued for the lease when the pinned
  checkpoint arrived; no Gradle audit tasks were executed under this label.
- `steam-checkpoint-build-e`: all tests, packaging, release and checksum tasks
  passed, but the new Eclipse JRE audit rejected its valid trailing slash.
  The audit now checks the final path segment and all three JDT compiler levels.
  Run E's original reports and generated files remain preserved.

Base Metals' published copper recipes yield one powder from raw copper and two
from copper ore. Steam reads those native recipes and does not override their
ratios. This is a dependency-version distinction, not a Steam balance adjustment.

## Required Next Gates

Use the coordinated Power Advantage 1.17.1 test record for current packaged
server/client, migration, operation and endurance results. Every result is bound
to exact Steam, Power, dependency and test-harness hashes; older F/G/J results do
not qualify a changed archive merely because its version is unchanged. Preserve
failed receipts and use a new evidence label for every run and explicit repin.

Earlier operating failures remain archived. G is diagnostic-only because its
finite-Float32 protocol changed during restart; its pre-existing raw-save receipt
remains red and unchanged. Those historical failures are not a statement that the
current native-registry runtime is still waiting for its first qualification.
The optional-provider fix needs its own exact-byte follow-up in the central
record. Module build proof does not imply manual gameplay or visual acceptance.

The generated client tokens are expected launcher inputs, not a confirmed
generator defect: bytecode inspection of the pinned Slime Launcher 0.2.2 shows
`Main` replacing `{asset_index}` and `{assets_root}` and calling
`LegacyDev.setupNatives`, which extracts natives and sets `java.library.path`.
Forge 37's `CommonDevLaunchHandler.fixNatives` appends its extra native-directory
entry rather than removing that prepared path. No workaround or generated
launch edit was made. An actual Eclipse client launch, production packaged
client qualification and visual acceptance are still required.

Manual gaps include all boiler variants; first-loaded-tick heat and redstone;
six drill facings, target hardness, track movement, inventory and energy;
helper animation and cleanup; pumps and stills; elevator rides; GUI captions
and controls; musket last-cartridge firing, chunk boundaries, sounds and
enchantments; multiplayer synchronization and capability unload/revival.
Use disposable copied worlds only.

## Changed File Groups

- `src/main/java/zone/moddev/mc/steamadvantage`: native source changes across
  entry point, blocks, machines, inventories, client graphics/GUI, musket,
  enchantments, villages, migration, config and recipe helpers.
- `src/main/resources/META-INF/mods.toml`, `pack.mcmeta` and
  `data/minecraft/tags/blocks/mineable/pickaxe.json`: target metadata and tools.
- `src/test/java`: retained native regression tests plus
  `NativeBlockEntityContractTest`, `MusketLoadedChunkBoundaryTest` and
  `GeothermalNativeTemperatureTest`.
- `build.gradle`, `gradle.properties`, the three build/analysis workflows,
  `tools/verify-native-port.js`, `README.md`, `CHANGELOG.txt` and this record.

The current candidate's tracked changes from `e5dd65c` are archived separately
as `tracked-change-list.txt`. The local agent file list is not part of the commit.
All Steam-owned build sessions have finished.
This module qualification did not open or modify a world.

Native ticker reference: [Forge 1.17 block entities](https://docs.minecraftforge.net/en/1.17.x/blockentities/blockentity/).
