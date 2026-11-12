# Steam on Minecraft 26.1.2

Version 2.2.17.2601021 is accepted for release after automated qualification and
manual testing. See the
[family qualification record](../../../PowerAdvantage/docs/26.1.2/QUALIFICATION.md)
for the exact tested jars, completed checks and scope limits. The usual fork,
MMD merge and CurseForge/GitHub/MMD Maven release workflow is authorized.
Publication has not happened. Source pushes, merges, tags and publisher runs
remain held until the verified public Power and Base Metals dependency handoff.
Power is released first, then Steam, then Electric.

The existing source branch remains feature/basesteam-26.1.2-port. Public file IDs
and final source pins must come from the actual released dependencies; no local
pin or guessed ID satisfies the hosted gate. Earlier checkpoint notes, old holds
and qualification receipts below remain historical and bound to their original
inputs. They are not rewritten as proof of a later public-pin rebuild.

## OreSpawn 4.1.1 Resume

The resumed local qualification pins published OreSpawn 4.1.1.2601021, source
101fd12b598934c353e417c57edc5c2bbefd5f53, CurseForge file 9572926. Player and
development SHA-256 are both
810284ECB32D5249381F246498CEFA57E70FFF7CCC40110F799FC276CBC42BF7.
The actual public jar still declares Built-On 26.1.2-64.0.9 and Java 25;
those provenance checks remain unchanged. Its hash and manifest were checked
locally against both the supplied public download and the parent's frozen copy.

The parent handed off exact Power source
d0bd0bdbde6c499e800e5a5b4ecc7f9ac63d2055, player/development SHA-256
23ABCC1FEFDAA6988BF709437EB165C8948C757E773ADAB738092EB93CA8D2B0.
Those bytes and all four unit-helper inputs were verified and frozen locally.
The clean full release suite executes 45 tasks and 276 tests across 47 suites,
with zero failures, errors or skips. Javadoc, exact dependencies, archive,
checksum and Eclipse production-classpath audits pass. Fresh ECJ compiles
60 main, 54 test and three GameTest sources and passes the complete same
276-test inventory, including the three inventory-rejection controls.

Independent path-with-spaces warm and genuinely empty-cache exports both pass
all 45 tasks and reproduce player, sources and Javadoc archives exactly.
The cold export downloads Gradle and regenerates the native Forge SDK without
seeding its cache. Two failed cache-only export setups and the first cold
attempt's Windows CreateProcess error 267 remain unchanged. The latter had a
268-character decompiler working directory; a new, shorter cache path inside
this checkout passed in 6m33s. Both source and cache paths contain spaces.

Final player/development SHA-256 remains
4A4E7D8C3704DCF17F46EBB55A7A908A6E1B547FCAB93E3B6782EE32E49301CB.
All 333 player entries are identical to candidate09. The profile12 actor/order
campaign is separate; no new game launch, combined-family or manual-acceptance
result is claimed by these child build receipts.
Historical candidate receipts below remain bound to their original inputs.
New Steam evidence is kept under
D:/PowerAdvantage/_verification/base-2612-port-20261112/resume-orespawn411-20261112/steam.
The parent owns the combined profiles and broader family campaigns. The
separately assigned profile12 actor/order task completes all nine routes:
original110, maintained112 and native1211-stopped-source pass strictly; all six
dimension routes retain PASS_WITH_NATIVE_DIFFERENCE for independently proved
native wolf health/max-health scaling. Their original strict differences and
the no-Power native control remain intact; all other fields match strictly.

Both actual machine orders and both strict actor-inclusive orders pass. Each
actor order preserves the two saved musket witnesses through five semantic
phases, rejects all 14 entity mutations and passes three stopped raw audits
matching 15 entities each. Ten guard and eleven woven-code mutations reject
per actual-order campaign. Six strict profile-boot audits and all 39 game
process closures pass; assigned ports 29240-29249 are free. Instrumented
order qualificationAllowed remains false.

The first order attempt was correctly rejected before Minecraft because the
parent regenerated its final Eclipse classpath after sidecar01 was built.
That failure is preserved. Sidecar02 binds the final classpath with no guard
relaxation; no parent source or classpath was edited by this task.
The assigned handoff is actors-orders/final-owned-handoff-01.json under the
resume directory, SHA-256
53E74959A4B3DF78F92652FB4C79C77BC185E326F23F525CB12E1CDBB2B48020.
Source fixtures, ordinary seed12, frozen family jars and all sealed inputs are
unchanged. These assigned scopes do not claim human or global family acceptance.

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
b947b4b81f3508fa220a212b41157ed2f801909f/player 5309E7C4, the approved Base
Metals 3.1.2.2601021 hook source 331fa46914f854db71a8c33500cc559a8878b7d3/
player 67862388, and the exact parent fixture08 jar and three audited Java
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

## Candidate05 Child Results

Power source at this historical checkpoint: e49a2e947e066a0f0e497982c2d5db596dfe11d9.
Power player: 3D9ACF8B86DD1B443B138C61E5E1C5BAAC3059D83EED83B8E6E927BC501CA446.
Steam player and deobf:
51DE7B6C58DA2DF5116352364B34E5B12FE5A22AA1DD2A487D472EF2E83A0AC3.
All bootstrap/registry/loader source pins are unchanged. This checkpoint used
BM source 13d4c4e38bf74d76f2f69f307dc55d13a81e8674/player 4911669B and the
unchanged OS input. This is local source integration, not publication.

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

## Candidate06 Atlas And Dependency Checks

Power source at this historical checkpoint is 0ac874de66f792c6efca5bccd0536b1b113fb786, player
618C3F96736392CE2853575F5A6A076EAC2A9B9F76F9303D86DEF58E418857D0.
The BM source is 331fa46914f854db71a8c33500cc559a8878b7d3, player
6786238835D4D8A653C769F3B0E61D226959146300E17523799CFDF17540FD19.
OS and the three frozen unit-helper sources remain unchanged.

Steam player and deobf are
4A4E7D8C3704DCF17F46EBB55A7A908A6E1B547FCAB93E3B6782EE32E49301CB.
Only the two atlas JSON entries differ from 51DE; all compiled classes and the
manifest are byte-identical. Steam registers 24 block/entity/material sprites
and three item sprites as explicit native singles. It does not register foreign
namespaces or global directory sources. IDs are disjoint across both atlases.

The obsolete directory-only test and build audit both failed against the new
resources before their validators were updated. The new checks validate exact
source shape, ownership, physical files and disjoint IDs before combining the
native sprite inventory. Actual Forge 64 SpriteSources and Material codecs,
resource managers and source loaders prove all model references resolve; the
native Material identifies a sprite rather than a fixed block/item atlas.
Directory, alias, foreign namespace, missing-file, duplicate, malformed-type and
cross-atlas controls remain strict, as do missing/wrong-hash Power inputs.

The default-path full release suite passes 46 executed tasks and 276 tests with
zero failures, errors or skips. Fresh ECJ compiles all three Eclipse scopes and
runs the complete same inventory with zero ignored tests or assumptions.
Evidence: build/native2612-atlas-context-qualification and
build/ecj-native2612-candidate06-atlas-full-01/receipt.json. The earlier 05
receipts remain historical and are not qualification of candidate06.

The isolated path-with-spaces verification build also passes all 46 tasks and
276 tests. All three release jars match the ordinary build byte-for-byte. It
reuses the verified native engine/dependency cache, not a new cold download.
Each recipe mode passes the four named source GameTests and the ten native
trade placements with 1,280 generated offers. APOCALYPTIC retains all nine
salvage recipe codec/output/negative checks; NORMAL and TECH_PROGRESSION require
their absence. The current packaged Steam jar passes a separate two-process
startup, save, stop and reopen check. JVM caps are explicit command-line options
and all three inherited JVM option variables are absent in these receipts.

The five assigned finite campaigns were rerun against the separately frozen
candidate06 profile 9F4C019B, not relabelled from 05. Harvest checks 271 cases
before and after restart. Lifecycle covers twelve movement/mining cases, five
physical chunk cycles and saved-progress/legacy-read/changed-target controls.
Partial networks and bridge reconnection cover all three power lanes. Extended
drills cover eighteen routes, 72 moves, four partial-unload cycles, both reload
orders, eighteen blocked/energy/redstone cases and 21 restart cases. All pass.
Harvest, lifecycle and extended include cold JVM restarts; partial and bridge
are physical-unload campaigns without an additional JVM restart.

The read-only raw audit covers 471 JSON packets, native ticket geometry, all
nine real JVMs closed and unchanged profile, six jars, seed and imported oracle
sources. Seventeen adapter unit tests pass. The scoped finite receipt is
_verification/base-2612-port-20261112/native2612-machines-networks-06/
finite-candidate06-handoff-01.json, SHA-256
7536216856051A46CD22CF6F12AF39CD12D6CF475C37F2AC27DF405A7B1383D2.
These results do not claim global family qualification or manual acceptance.

## Candidate07 Dependency Qualification

Power source is 9f6ffb30a14efe4360fee0cbdfa52dc8090a9819, player
05031F1A3DCF25A405D9222E65619F7173C37CCF57B76308B72F741CA0E5198C.
The bootstrap jar and all three helper-source hashes are unchanged and were
reverified before retargeting. BM and OS keep their exact candidate06 pins.
This update changes dependency pins and notes only, not Steam gameplay code or
resources. New child qualification is recorded separately from candidate06.

The default-path release build executes all 46 tasks and all 276 unit tests
with zero failures, errors or skips. Native dependency, artifact, checksum,
resource, Javadoc and Eclipse audits pass against the new Power bytes. Fresh
independent ECJ compiles 60 main, 54 test and three GameTest sources and runs
the full 276-test inventory with zero failures, ignored tests or assumptions.
The complete compiled-inventory guard remains enabled.

The isolated path-with-spaces build also executes 46 tasks and 276 tests and
reproduces all three release archives exactly. Its verified native engine and
dependency cache are reused; this is not an unseeded cold-download claim.
Player SHA-256 remains
4A4E7D8C3704DCF17F46EBB55A7A908A6E1B547FCAB93E3B6782EE32E49301CB,
sources FFA80BE16EF72CAEF38B16B6A79FB46952C7369E2F1E3770F2A184E805F6A03B,
and Javadoc 4C9BFA077DF88E78C480ED058FF8C02F85C31DFB1834E78D9DA7B1777D6DE085.
All 333 player entries, including every class, resource and manifest, match06.
The fresh pin-bound receipts are under build/native2612-candidate07-qualification
and build/ecj-native2612-candidate07-full-01/receipt.json.

No new GameTest, packaged-world or physical campaign is claimed for07 here.
The five physical campaigns remain held until the parent supplies the sealed07
profile. The06 receipts remain unchanged historical evidence.

## Candidate08 Dependency Qualification

Power source is e565f99284de5e6c7860d378abc43b96d9faa5ee, player
CC94656A75B3E6311E5F9F7CB4E4E60014B94A87665743D5A5D3C84CEA849720.
The bootstrap and three helper-source hashes are unchanged and were reverified.
BM and OS pins remain unchanged. Only dependency pins and notes change in Steam.

The fresh default-path full release run executes all 46 tasks and 276 tests,
with zero failures, errors or skips. Dependency, artifact, checksum, resource,
Javadoc and Eclipse metadata audits pass. Fresh independent ECJ compiles all
60 main, 54 test and three GameTest sources, then executes the complete 276-test
inventory with zero failures, ignored tests or assumptions.

An isolated path-with-spaces verification build also passes 46 tasks and 276
tests and reproduces the player, sources and Javadoc archives byte-for-byte.
It reuses the verified native engine/dependency cache; no unseeded cold-download
claim is made. All three hashes remain the exact values in the07 child results,
including player
4A4E7D8C3704DCF17F46EBB55A7A908A6E1B547FCAB93E3B6782EE32E49301CB.
All 333 player entries match07: no class, resource or manifest changed.
Evidence is under build/native2612-candidate08-qualification and
build/ecj-native2612-candidate08-full-01/receipt.json. These are fresh pin-bound
child checks, not relabelled07 tests or an08 family/world qualification.

The parent found a real provider-schema regression in07 legacy generation.
The five07 finite campaigns finished normally with 470 raw JSON packets and
nine closed JVMs, but those scoped passes do not qualify07 or waive that failure.
All07 results and failures remain preserved. No further07 campaign or preseal
is permitted. The08 physical reruns await both its frozen profile and the short
legacy-generation gate.

## Candidate09 Dependency Qualification

Power source is b947b4b81f3508fa220a212b41157ed2f801909f, player
5309E7C433933F1426543310611292BF80388A476836139F0117E6A7E5C9AFC1.
The bootstrap and three helper-source hashes were reverified and are unchanged.
BM and OS retain their exact existing pins. Steam changes only its dependency
pin and notes, not production code, resources, version or gameplay.

The default-path full release run passes all 46 executed tasks and 276 unit
tests with zero failures, errors or skips. Dependency, resource, artifact,
checksum, Javadoc and Eclipse metadata audits pass. Fresh independent ECJ
compiles all 60 main, 54 test and three GameTest sources, verifies the complete
compiled JUnit inventory, and executes all 276 tests with zero failures,
ignored tests or assumptions.

The isolated path-with-spaces build also passes 46 tasks and 276 tests and
reproduces all three release archives exactly. The verified native engine and
dependency cache are reused; no unseeded cold-download claim is made. Player
SHA-256 stays
4A4E7D8C3704DCF17F46EBB55A7A908A6E1B547FCAB93E3B6782EE32E49301CB.
Every one of its 333 entries matches08, including all classes, resources and
the manifest. Sources and Javadoc retain the same exact hashes recorded above.
Evidence: build/native2612-candidate09-qualification and
build/ecj-native2612-candidate09-full-01/receipt.json.

The five08 physical campaigns subsequently passed with 469 audited raw JSON
packets, nine closed JVMs and all 34 imported oracle files unchanged. Those
bounded results remain historical. The parent's08 released110/112 first-boot
quartz-import failures are preserved; restart passes do not waive them.
New09 physical campaigns await the parent's frozen profile and early gate.
The optional Mineralogy assessment is read-only. Core and versioned endurance
guards still require exactly six jars. The three Mineralogy-only scopes remain
explicitly SKIPPED; no full interoperability claim includes them.

## Remaining Qualification

The parent owns the combined family graphical, operating-machine, migration and
endurance campaigns, including every machine UI and the unified creative tab.
Those receipts must bind the frozen family jars; no global pass or manual-only
status is inferred from this child handoff. No publication is authorized.
