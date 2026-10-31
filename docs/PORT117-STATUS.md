# Steam 1.17.1 Port Status

Local source candidate: `2.2.9.117011`, branch
`feature/steamadvantage-1.17.1-port`, baseline
`ed808f9fe9a2398d2334c0cadb1546284a02a22e`.

No commit, push, PR, merge, tag or publication has been made for this port.
The parent must approve a frozen module commit separately. Manual testing is
required before any port push or publication action.

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
`28247f936ac99e7a417da1bc1c89bc8394773a26`, API jar SHA-256
`7B2B2676CDB9A0B933DA04CC743787803C68E1466FD44E2EFBAEFB3ED47ABB3D`.
The verified input is copied under `evidence/inputs` so later parent builds
cannot silently change the bytes used by Steam. This is a local checkpoint,
not final runtime acceptance. No old-target Power jar has been used, and no
extra shared API or constructor workaround was needed.

Plain JUnit uses Power's verified test-only native capability bootstrap, SHA-256
`0F75844C30D3E0B4FAF43EE0905FABE5553D9CC32177FF0BB3A690088208701A`.
It invokes Forge's own token transformer; it is not a capability replacement
and is absent from production artifacts and launch classpaths.

Every local Gradle invocation must use the campaign's `Invoke-Gradle117.ps1`,
exclusive `gradle-shared.lease` and `D:/GradleCaches/advantage-117-20261031`.

## Checks Actually Run

- `node tools/verify-native-port.js`: source pass, 179 JSON resources,
  63 production Java files, 12 factories and 17 harvest entries.
- `node tools/generate-recipe-unlocks.js --check`: 24 unlocks verified.
- Baseline comparison: all 24 original test classes retain their 111 declared
  test methods. Nine new methods bring the declared total to 120. This is not
  an executed-test count and does not expand parameterized cases.
- Literal-key comparison: all 99 NBT/key accesses across the 63 production
  Java files retain their original names.
- All 72 tracked recipe, advancement and loot files are byte-for-byte unchanged.
- `git diff --check`: pass.
- Checkpoint run `steam-checkpoint-build-f`: `check build javadoc
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

## Archived Candidate

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

No main/test compilation or JUnit errors remain against the pinned checkpoint.
Any later Power change requires an explicit coordinated repin and fresh
qualification; the existing F/G results apply only to their recorded inputs.
Preserve all failed receipts and use a new evidence label for each run.

The parent's shared packaged runtime and migration rig must qualify the exact
Steam jar with the exact public Base Metals/OreSpawn runtime bytes. Automated
proof, fresh/reload lifecycle, gameplay and visual acceptance remain separate.
No packaged runtime pass is claimed by this module's build and unit-test proof.

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

The complete current edit list is in [PORT117-FILES.txt](PORT117-FILES.txt).
All Steam-owned build sessions have finished. No world was opened or modified.

Native ticker reference: [Forge 1.17 block entities](https://docs.minecraftforge.net/en/1.17.x/blockentities/blockentity/).
