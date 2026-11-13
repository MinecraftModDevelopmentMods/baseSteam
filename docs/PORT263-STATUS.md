# baseSteam 26.3 Port Status

Version `2.2.19.2603001` was published on 13 November 2026. See the
[published release record](RELEASE263.md) for its exact source and verified
CurseForge, Maven and GitHub downloads.

> Dated candidate and implementation history, 12-13 November 2026. Earlier
> pending-gate wording below is retained from those checkpoints, not current
> download instructions. Publication adds no new gameplay qualification and
> does not rewrite the original receipts.

The port branched from accepted 26.2 source
`de4dcf6ed84461843a241bbaf93cf03e43e83eb2`.

The matching basePower checkout's `docs/26.3/QUALIFICATION.md` records the
completed combined machine, recipe, migration, client, restart and endurance
checks and exact jar hashes. The
detailed entries below are implementation history; their earlier pending
gates are superseded by that completed local test record.

## Release Preparation: 13 November 2026

The user accepted manual testing of the profile04 family and authorized the
fork/PR/merge and CurseForge, Maven and GitHub release sequence. The accepted
Steam player remains `770E46CF87FEE3CC1EFF73C666858BFC03D6922E866B4C07C0A093B746B2CF44`.
The final automated family index records 825 tests and remains unchanged;
its earlier manual/publication flags are historical, not the new authorization.

Upstream 26.2 release documentation and public pin metadata were reviewed from
`cfdfabef1972ca36102c8fff4fe4f8e3624f9196`. Its gameplay sources did not change.
The upstream and fork `master-26.3` base refs were created at that exact commit;
this does not push the local 26.3 port. The published 26.2 history is retained.

Power is publicly verified at source `4fae0a51feb3e49875f09c500b869266122a711a`,
CF file 9589309; Base Metals and OreSpawn are public at files 9589195 and
9589145. All three player hashes match the accepted profile04 inputs.
The parent public verification receipt SHA-256 is
`ABC318097FDFF71953CF6CAE9572E3EE34B3459090326C6A6FEC1A3C1A55297C`.
The post-pin full rebuild passes all 45 tasks and 291 units with no skips.
All three Steam archives remain byte-identical, including all 425 player
entries, the manifest and mod metadata. The exact external delta receipt is
`post-pin-01/delta.json` in the publication evidence directory.

Public API source and unit support use separate pins: unit-support source
bytes remain at the `c0eb83f7` checkpoint with their original hashes. The three
installed Forge 66.0.0 runtime pins now come from proof05's sealed inventory,
checked against the actual server files, never 26.2 values.

All required hosted checks, including CodeQL, must pass before merge. A
changed player hash needs updated combined-family qualification. The existing
publisher must pass check-only with the actual merged source, tag and sealed
CI artifact, and live publication must wait for the explicit Steam gate.
An accepted CurseForge file ID must be resumed rather than uploaded again.

Current release coordination is recorded outside the checkout in
`D:/PowerAdvantage/_verification/base-263-publication-20261113/steam/HANDOFF.json`.

## Target

- Minecraft 26.3, Forge 66.0.0, official MCP 20260918.230105.
- Temurin 25.0.3+9, ForgeGradle 7.0.34, checksum-pinned Gradle 9.6.1.
- Data 121.0 and client 97.1. Forge supplies hidden client pack metadata.
- Asset index 34, SHA-1 `e99f9973047785b5a9d52eaf72020b97545053c9`.
  The current Mojang version/index documents were downloaded and hashed;
  the strict Eclipse guard rejected the earlier stale index pin.
- Forge 66.1.5 has separate bounded dedicated-server compatibility evidence.
  It is not an automatic change to the full 66.0.0 baseline.

## Inputs And Boundaries

Required public inputs are basePower 2.4.18.2603001, Base Metals
3.1.2.2603001 with its shared hook, and OreSpawn 4.1.1.2603001 with the
retained-settings repair. All compiler inputs are pinned below. BM and OS
have passed the combined runtime tests and public-byte verification. Neither 26.2
artifacts nor public 4.1.0 are substitutes.

- BM source `ac07e9cd960c4b48308fd3f245e06f108e4ac437`, JAR SHA-256
  `B03388D10E5B83046A5D2D540E7D956202A8A6CC78B2302A412D3092192CAAE8`.
- OS source `bbdd80a9c1f7972d81bedf8e6c2b1d4338ec9fa6`, JAR SHA-256
  `7FBAFEFF97683DA0E7ABD0035B85C3A19276B3AD9CB50DBC071F9993B33E490D`.
- Power public source `4fae0a51feb3e49875f09c500b869266122a711a`, JAR SHA-256
  `97525CE9C75509C55225E0061D43BAF9D29BDBDE6DF853E1552A64A54C14C3DE`.
- Independent Power test-support source remains `c0eb83f7ba91cf8c83caea75e63057407a345949`;
  its hashes are pinned in `gradle.properties`.
  The unchanged bootstrap agent SHA-256 is
  `088FC137BEBB9B2E75C596DE868499E6099CF228BECFCF07EAE36791245FD1B6`.

Only this existing Steam checkout is owned by this port. Power's shared
APIs and dependency hooks are coordinated separately. No parent output is
built by Steam. Release writes follow the separate authorization gates above.
The parent's redundant AT is removed; the final SDK is bare Forge 66,
matching Base Metals and OreSpawn. Do not copy the historical Power AT.
Parent preserves the existing APIs and adds contextual
`FuelRegistry.getActualBurntimeForItem(ItemStack, BlockEntity)`.

The dedicated cache is `D:/GradleCaches/basesteam-263-port`. Evidence and the
current handoff contract live in
`D:/PowerAdvantage/_verification/base-263-port-20261112/children/steam`.

## Qualification Gates

Source and resource audits, compilation, unit tests, native GameTests,
packaged startup/migration and real-client proof are distinct gates.
The inherited unit inventory starts at 276 tests. Fifteen regressions were
added, for 215 annotated methods and 291 executed expanded tests in 49 suites.
`full-07-ba2` preserved one failure: the unchanged shared-API Forge item-override
assertion expected 237 and received zero. The parent fixed the confirmed API
bug in handoff03. `full-08-d03` reran all 44 tasks and passed all 285 tests with
zero failures, errors or skips. No assertion was waived. Power04 then passed
`full-09-power04` with the same 285-test inventory. The added native modifier
canonicalization regression brings `full-11-power04-oracle` to 286 passing tests.
`full-12-power04-final` reruns all 44 tasks on the expanded mode oracle and
passes the same 286 tests. After the whole-table scope regression, the exact
Power05 handoff passes `full-13-power05-initial`: all 44 tasks rerun, 287 tests,
zero failures/errors/skips.
The accepted-golden corpus regression brings `full-14-power05-golden` to 288
tests with zero failures/errors/skips; all 45 actionable tasks rerun and pass.
`full-15-power06-initial` then reruns all 45 tasks against Power06 and passes
all 288 tests, zero failures/errors/skips. No Steam production behavior changes
were needed for the parent actor-memory compatibility repair.
`full-16-power07-discovery` reruns all 45 tasks against Power07 and passes
291 tests, zero failures/errors/skips, including optional-tag discovery regressions.

Main, test and GameTest source scopes compile. Javadoc, exact dependency and
artifact audits, checksums and Eclipse production-only classpath checks pass
against handoff07. Installed ECJ 3.46.0 independently compiled all three scopes
and executed the complete 288-test inventory in `ecj-263-07-power05-golden`, with zero
failures, ignored tests or assumptions. Omitted-suite, omitted-method and
stale-method controls all fail as required. The earlier ECJ run correctly
remained red while the dependency regression was red.
That historical ECJ proof uses frozen Power05 and the complete compiled/XML
inventory, not a selected subset. Power06 also passed its 288-test ECJ proof.
Current Power07 proof `ecj-263-09-power07` independently compiles all three
scopes and passes all 291 units with zero failures, ignored tests or assumptions.
All three exact inventory negative controls remain required and pass.
Spaced-input `ecj-263-10-power07-cold-spaces` repeats the complete result.
The complete output directories are preserved under `pre-cold-build-03` and
`pre-repro-build-01` respectively, not overwritten by later clean builds.

The three current artifacts and source snapshot are frozen in evidence
`initial-power07-01`; the player/compiler JAR SHA-256 is
`770E46CF87FEE3CC1EFF73C666858BFC03D6922E866B4C07C0A093B746B2CF44`.
Sources JAR SHA-256 is
`120E5088E7E3A6CCDF5108D9CC6FD86697350A00E1575DCDDE1FCF4B006BAAAA`;
Javadoc JAR SHA-256 is
`5EAC97764A468523DBA718A195CBCBB89AD0FDA9EDBF810DFFD2BB52C0A818B5`.
Tested code is locally pinned at `c6b5db5fef401d62f11f535a236ca5e1999ff55d`;
only human qualification text changes afterward. Final source and all 182
changed-file hashes are listed in `HANDOFF-current-power07-01.json` and
`CHANGED-PATHS-current-power07-01.md` in the external evidence directory.
The prior D1027 artifact is preserved but does not qualify cross-mode discovery.
Power05 cold/spaced inputs pass in `cold-spaces-02-power05`: all 46 tasks and
288 tests, with all three release archives and both SDK JARs byte-identical to
warm. Current Power07 `cold-spaces-03-power07` passes all 46 tasks and all
291 tests in 49 suites with zero failures/errors/skips, in 9m 56s. Its Steam-only
Gradle home and project SDK were initially absent; seven hash-identical ordinary
input copies use paths containing spaces. The official bare SDK rebuild applies
zero repairs. All three release archives and both SDK JARs match warm exactly.
`clean-repro-01-power07` independently repeats all 46 tasks and 291 tests in
55s with build caching disabled, and all five JAR hashes match again. Complete
comparison receipts are `cold-spaces-03-power07-comparison` and
`clean-repro-01-power07-comparison`. Path-bearing metadata ZIP cross-path
reproducibility is not claimed. The earlier first cold
run was stopped at the user's request; that interrupted receipt is preserved,
not relabelled green. The parent repaired direct-26.2 actor-memory compatibility
in Power06, then repaired future recipe discoveries in Power07. Current
ECJ/native/cold/clean-repro proofs all use Power07.
Older input results remain historical
and cannot qualify the new input. Passing child gates do not qualify packaged runtime,
endurance, real-client behavior or manual acceptance.

The SDK removes `FuelValues` in favor of contextual cooking-fuel components.
Steam now resolves them with the actual machine's `CONTAINER_PROCESS` context,
retains Forge item/event overrides, and leaves speed multipliers and all
fuel-remainder assertions unchanged. Client/detached slot eligibility follows
the native component-presence policy; server slots still require positive fuel.
Reference-based burn durations require a server world and cannot silently fall
back to zero. The standalone fixture uses real vanilla world and reloadable
registry lookups, not fabricated fuel values.

The bare SDK research probe passes 12 value assertions and three slot assertions,
including wood 300, coal 1600, lava 20000, an unclamped 70000 constant, item override
237 and event override 73. The retained old helper fails compilation on the
removed native APIs; the new helper, native bootstrap and fixture compile.
This proof has no Power JAR or agent and is not the guarded Steam test suite.
Earlier failed controls and harness receipts are retained.

The oil boiler now passes `this` to Power's new contextual overload. Its global
fluid-result cache is removed because contextual providers and reloads cannot
share a cached value. Configured fuel precedence, quantities and conversion
factors remain unchanged. The coal/blast helper retains its existing integer
duration and null-recipe-type behavior rather than adopting Power's short cap.
The shared-API item-override regression passes unchanged against Power03 and Power04;
ForgeEventFactory only fires the event and does not invoke the item override.

Trades use singular `given_item_modifier`, native `type` function dispatch and
int/float provider holders. Block loot uses singular `condition` with predicate
`type`; chest entry functions use `modifier`. All three chest uniform ranges
now specify `type: minecraft:uniform`, including generated helper loot files.
The bare-SDK trade/loot research passes 1024 offers over four vanilla-item
fixtures, eight codec negatives, three silently ignored plural-field witnesses,
and native explosion predicate positive/negative controls. It intentionally
does not impersonate Steam item registrations or qualify the enchanted musket.
The actual five-trade GameTest retains 1280 offers and now requires eight codec
negatives, a provider witness and the old-plural semantic-loss witness. A new
actual DIRECT_CODEC regression verifies that a singleton modifier sequence
canonically encodes as a function object, replays the old array-cast failure,
and rejects unknown/legacy function discriminators.

The whole-table native fixture additionally preserves modifiers at table, pool
and entry scope, single and multi-term conditions, default weight one and exact
counts. Five controls prove that old plural fields can decode successfully yet
silently lose only the affected scope. Steam's 15 production tables and six
generated templates already use singular native fields. Independent goldens
from the accepted Steam 2.2.18 JAR (SHA-256
`EDD10D6ED4A623F6EAA364D21480FDA3B777FB15C2195D437882DB09C0F415AB`)
are checked byte-for-byte in separate unit/GameTest-only resource scopes. The
manifest SHA-256 is
`DDA294A1509FA8ED837F5EBF9F6DDD9F7BBBAE857A824212B6D74F489999C094`.
Unit whole-table checks use a reversible builtin-item bijection and assert that
no item IDs are registered; server checks use actual mod IDs and actual loaded
tables. Native canonicalization permits only singleton modifier flattening,
inert defaults and type-identifier namespace normalization. All other fields,
IDs, counts, weights and effect paths must match. Generated ALT comments and
entry labels are checked verbatim before their native projection. No global
Forge loot-modifier `conditions` arrays are converted.

The standalone fixture registers real Forge HolderSet types through native
NewRegistry/Register phases. Steam has no custom tag condition: it delegates
those to Power and retains mode/musket condition assertions. No Forge patch,
recipe-mode waiver or ConditionContext substitute is introduced.

All 24 recipe unlocks use native `recipe_unlocked.recipes` with
`#basesteam:discovery/<stem>`. Each generated recipe tag contains exactly the
matching recipe ID with explicit `required:false`. Recipe conditions stay on
recipes only. Advancements remain unconditional so mode changes cannot discard
future discovery state. Native tests cover 24 singleton active tags and 24 empty
tags with no recipe holders, exact roundtrips, unrelated tags and required-flag
identity guards. Ingredient OR requirements, rewards and logical IDs are unchanged.
The earlier condition-parity design prevented dangling holders but failed actual
mode-change restore; it is superseded, not relabelled as accepted behavior.

`gametest-normal-01` exposed the removed singular criterion field. After the
Steam fix and Power handoff02, fresh `gametest-normal-02-ba2` gets beyond that
failure but stops before executing tests: Power's material condition accesses
unbound item tags while loading advancements and recipes. Fresh
`gametest-normal-03-d03` confirms the same blocker on the historical Power03
JAR. All three logs and disposable run directories are retained.
Power04's scoped pending-tag lookup fixes this actual startup failure without
a Steam production change. `gametest-normal-04-power04` starts and executes all
four tests, exposing an old array-cast oracle in trade negative controls.
After the native canonical-object adaptation, `gametest-normal-05-power04`
passes all four tests, 1280 production trade offers, eight codec negatives,
provider/plural witnesses and NORMAL salvage absence checks.

Historical TECH execution exposed the former oracle's unconditional NORMAL unlock
lookup. That oracle checked all 24 recipe/unlock pairs against their
exact condition trees and live bound tags, checks exact native recipe references
and ingredient OR groups, and retains the ingredient-positive/negative checks
even for intentionally absent variants by parsing their actual native criterion
data. The historical matrix is green in `native-matrix-power04-01`:

- NORMAL: four tests, 19 unlocks present and five absent.
- TECH_PROGRESSION: four tests, 18 unlocks present and six absent.
- APOCALYPTIC: four tests, 16 unlocks present and eight absent, all nine salvage
  recipes present with native codec roundtrips and unrelated-input negatives.

Each mode executes 1280 production trade offers, all eight codec negatives,
the provider witness and the legacy-plural semantic-loss witness. That is
12 successful native GameTests and 3840 offers, with no ERROR/FATAL log entries
or skipped cases. No availability assertion is removed or waived. These are
native Forge userdev checks, not packaged/client/endurance/manual qualification.
That Power04 matrix is preserved as historical proof. Fresh Power05 runs
`gametest-normal-07-power05-whole`, `gametest-tech-04-power05-whole` and
`gametest-apocalyptic-02-power05-whole` pass the same complete four-test/mode
inventory plus 15 accepted-golden production tables, six generated templates,
42 semantic fields and five whole-table silent-loss controls per mode. The
sealed historical matrix is `native-matrix-power05-01`. Power06 also passed
that oracle. Those results do not qualify future discoveries across mode changes.

Current Power07 runs `gametest-normal-09-power07`, `gametest-tech-06-power07`
and `gametest-apocalyptic-04-power07` pass all four tests per mode. The sealed
receipt is `native-matrix-power07-01`. All 24 advancements remain loaded in
every mode, with 19/18/16 singleton active recipe tags and 5/6/8 empty recipe
tags respectively. Each mode proves 24 optional-tag identities and 24 actual
AdvancementProgress codec roundtrips, ingredient OR requirements and exact
recipe rewards. Trade, salvage, golden whole-table and all negative controls
remain mandatory. There are no skipped tests or ERROR/FATAL log entries.

`source-05` passes release configuration, hygiene, recipe guards, datapack and
source-layout checks and exact BM/OS development/player byte audits. The Node
source/data checks pass for 224 JSON inputs, 12 factories, 17 mining identities,
four enchantments, five trade definitions, fourteen trade negative controls,
fourteen block loot tables, the six-entry chest and its generated template,
three loot negative controls and 24 unlocks.
Current source/data audits pass 248 production JSON inputs, 60 Java sources,
215 annotated test methods and all 24 unlock/optional-tag pairs. Recipe bodies,
assets, mining tags and all historical qualification documents remain unchanged
from the accepted source.
The earlier `compiler-01` missing-pin failure is preserved. No Power API stub
or legacy library was substituted to bypass it or the later failures.

Machine IDs, recipe and unlock IDs, drill directions, configuration precedence,
musket state/durability/enchantments, inventory and fluid state remain protected.
The historical qualification documents are unchanged and apply only to their
original candidates. This port is not qualified or ready for publication;
manual acceptance is not claimed.
