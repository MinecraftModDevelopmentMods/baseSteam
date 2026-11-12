# Steam on Minecraft 26.1.2

Local port in progress. The bounded automated results below do not claim complete
family/gameplay qualification or manual acceptance. No push, PR, tag or publication
is authorized.

## Baseline And Toolchain

The checkout starts from released MMD source
ce1601aa6c9f13649ac8ee0b05c4d6c01ef7406e on the seeded master-26.1.2 branch.
The active local branch is feature/basesteam-26.1.2-port. The 1.21.11 source and
publisher remain untouched.

Target: baseSteam 2.2.17.2601021, Forge 64.1.5, MCP 20260409.101008,
ForgeGradle 7.0.34 and Gradle 9.6.1, official runtime names, reobf=false.
Temurin 25.0.3+9 runs Gradle, compilation, Minecraft, Mavenizer and Slime.
The installer SHA-256 is
6E01323837051176762BD466340A601DA122F179FE6A00AC616E05429AE07648.

The sealed Mavenizer and rule-manifest hashes are BB0DACB3 and 21D1F598;
the full checksums are enforced by gradle/forge64-mavenizer.gradle. The exact
64.1.5 SDK generated successfully without target source-repair rules.
Two focused toolchain regressions failed against the inherited build and pass
after the configuration change. These are not a production compile or a full
unit-test result.

## Dependency Gate

Current local integration inputs are Power 2.4.16.2601021 source
e49a2e947e066a0f0e497982c2d5db596dfe11d9/player 3D9ACF8B, the approved Base
Metals 3.1.2.2601021 hook source 13d4c4e38bf74d76f2f69f307dc55d13a81e8674/
player 4911669B, and the exact parent fixture08 jar and three audited Java
sources. The generated unit-support copies are test-only, hash-checked and never
packaged or committed. Old Power/BM jars are not target inputs or substitutes.

The first eight-task default-path releaseJar/deobf build passes all exact
dependency audits and produces identical player/deobf bytes:
B5F2C2C310240FB2E052E7C81E2BB35940C4045643D0F7E2BE1B484073E22999.
An immutable copy is held under build/native2612-first-candidate/held. This is
available for DEVELOPMENT-ONLY profile checks, not yet a family-qualified jar.

Released OreSpawn 4.1.0.2601021 remains unchanged, source
c45ea2a111520ef904e994dd8dcd90babfcef29b, SHA-256
6D5CE2017C3314968083D140309E2F1A54BC239AC65C48944C995E9ECF937948.
Both BM and OS truthfully record their original 26.1.2-64.0.9 builds. Compatibility with
64.1.5 must be proved by new runs, not by changing that manifest or its bytes.

## Native API Work

The actual 64.1.5 SDK confirms Recipe.assemble takes only the input, the
renderer camera lives under renderer.state.level, and pack formats are client
84.0/server 101.1. Source/resource changes preserve all logical IDs and payloads.
The root pack keeps the native data-only range while Forge supplies the hidden
client aggregate. Both real native resource paths and negative controls remain
required; the earlier 1.21.11 receipts are not relabelled.

Forge 64's actual RuntimeDistCleaner reproduced the client type and common-menu
method annotation failures. The three helper type markers and eleven method
markers are removed while retaining all legacy decoration signatures and the
actual distribution-guarded renderer registration. Native cleaner and linkage
checks remain distinct from actual dedicated-server/client startup proof.

Five native trade definitions retain all ten profession/level placements and
legacy inclusive ranges, seven uses, one XP and zero discount. The cleric's
native enchant_with_levels modifier carries the exact generated level as an
additional trade cost. Ten deliberate source-contract corruptions are rejected;
native codec and generated-offer witnesses are recorded in the current results below.

## Candidate01 Checkpoint

The first full native unit run executed 258 tests with seven failures. The
unchanged player sources then passed all 258 after the test fixtures used the
native post-registration component-binding lifecycle and the pack-format oracle
was updated. Both XML inventories and the original failure logs are retained.

After six complete-inventory regressions were added, all 264 tests passed with
zero failures, errors or skips. Fresh ECJ 3.46.0 on Temurin 25.0.3+9 compiled
61 main, 54 test and three GameTest sources, then ran all 264 tests without
failures, ignored tests or assumptions. Its ASM scan compares every compiled
JUnit class/method against Gradle XML, including tests outside conventional
package/filename filters. Only the three reviewed existing Parameterized suites
use JUnit's parameter discovery; other custom runners are rejected. Actual
omitted-suite, omitted-method and stale-method XML controls all fail the guard.
Evidence: build/ecj-native2612-full-inventory-02/receipt.json.

The native Eclipse audit passes with official-name cache/metadata arguments,
no remap flags or mapping archive, literal asset index 30 and assets root,
JavaSE-25 and the pinned Buildship connection.java.home. Eclipse generation does
not download client assets. The original obsolete mappings-archive requirement
failure is preserved in build/native2612-qualification/third-full-unit-eclipse.log.

The first actual source GameTest launch ran the four named Steam cases: three
passed and the native-resource case failed on an added number-provider negative
control. All 1,280 generated trade offers had already passed their exact range,
enchantment and correlated-cost checks. The native NumberProviders codec has an
explicit untyped uniform fallback when min/max remain present. That case is now
an exact round-trip witness, while an unknown provider without fallback bounds
must be rejected. Three focused toolchain/codec tests pass and the updated
GameTest source compiled; its runtime rerun was not yet claimed at that checkpoint.
The compiled full inventory then contained 265 tests, not a new full-run result.

The original four-case XML and logs remain under
build/steam-gametest-native2612-first-20261112-01 and
build/native2612-qualification/gametest-first.log. No failure is overwritten or
relabelled. The source GameTest launch is not a frozen player-jar proof.

Parent found an obsolete SRG InventoryLocks field lookup in Power candidate01.
Production qualification, packaged launches and endurance were held for the
repaired candidate02 source/hash handoff. Steam's held B5F2 player bytes were
unchanged; no further production source or resource edits were needed for these
fixture, adapter and verification changes. No local port commit existed at that checkpoint.

## Candidate02 Inputs

The repaired Power source is 934bf3fa287c17f38a24e89f754087722c2c0ad0,
player SHA-256 F39FF5EC90706AE9109D29A254FDBCC5CA47D333E8589BD33FF98061EAF0FAB7.
The fixture08 bootstrap jar and all three source checksums are unchanged and
were reverified before repinning. Base Metals and OreSpawn retain the exact
inputs above. The old property file and player jar are held under
build/native2612-candidate02-qualification/held-inputs. Candidate02-bound child
results are recorded separately; candidate01 failures are not relabelled.

## Frozen 51DE Child Results

Current local Power source: e49a2e947e066a0f0e497982c2d5db596dfe11d9.
Power player: 3D9ACF8B86DD1B443B138C61E5E1C5BAAC3059D83EED83B8E6E927BC501CA446.
Steam player and deobf:
51DE7B6C58DA2DF5116352364B34E5B12FE5A22AA1DD2A487D472EF2E83A0AC3.
All bootstrap/registry/loader source pins are unchanged. BM and OS retain the
exact inputs recorded above. This is local source integration, not publication.

Actual family operations exposed a nullable crafting-remainder NPE in the coal
boiler and blast furnace. Four actual-method cases failed with that NPE before
the fix; all nine focused fuel tests pass afterward. Single coal becomes EMPTY,
larger stacks consume exactly one per call, valid lava fuel returns one fresh
bucket, and water buckets remain nonfuel. Remainders are captured before replacement.

APOCALYPTIC common setup also attempted to build ItemStacks solely for nine
obsolete no-op crushing calls. The internal bridge and setup call are removed.
The public compatibility methods remain, and all nine authoritative JSON files,
conditions, inputs, output identities and quantities are byte-identical to B5.
The initial lifecycle-test failure expecting the removed class is retained;
its replacement requires that neither the class nor the unsafe setup call exists.

The final default-path release run executes 46 tasks and 273 tests across 47
suites, with zero failures, errors or skips. Dependency, archive, checksum,
resource, recipe, Javadoc and Eclipse metadata audits pass. Fresh ECJ compiles
60 main, 54 test and three GameTest sources and executes the same complete 273-test
inventory with zero failures, ignored tests or assumptions. Its omitted-suite,
omitted-method and stale-method negative controls remain strict.

Each of NORMAL, APOCALYPTIC and TECH_PROGRESSION passes the four named native
GameTests, ten villager-trade placements, 1,280 generated offers and five trade
codec negatives. APOCALYPTIC additionally proves nine loaded salvage recipes,
native recipe-codec round trips, exact outputs and unrelated-input negatives;
the other two modes require all nine salvage recipes to be absent.

The actual native JVMs assert explicit four-processor/1536-MiB caps and absence
of JAVA_TOOL_OPTIONS, JDK_JAVA_OPTIONS and _JAVA_OPTIONS. ECJ and the packaged
startup/save/stop/reopen receipt record those variables as absent. Both packaged
processes save a nonempty world, stop with exit zero and are closed. These are
child lifecycle checks, not the family's operating-machine/endurance proof.

An isolated path-with-spaces verification checkout executes the same full
46-task/273-test suite and reproduces player, source and Javadoc jars exactly.
It reuses the checksum-verified native engine/dependency cache and makes no
unseeded cold-download claim. All failed and intermediate B5/7E9 receipts remain.
Current evidence is under build/native2612-apocalyptic-repair, the ECJ receipt is
build/ecj-native2612-frozen51de-final-02/receipt.json, and the packaged receipt is
evidence/packaged-server-native2612-frozen51de-final-01/receipt.json.

## Remaining Qualification

The parent owns the combined family graphical, operating-machine, migration and
endurance campaigns, including every machine UI and the unified creative tab.
Those receipts must bind the frozen family jars; no global pass or manual-only
status is inferred from this child handoff. No publication is authorized.
