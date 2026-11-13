# baseSteam 26.3 Port Status

Local work in progress. Version `2.2.19.2603001`, branched from accepted
26.2 source `de4dcf6ed84461843a241bbaf93cf03e43e83eb2`.

## Target

- Minecraft 26.3, Forge 66.0.0, official MCP 20260918.230105.
- Temurin 25.0.3+9, ForgeGradle 7.0.34, checksum-pinned Gradle 9.6.1.
- Data 121.0 and client 97.1. Forge supplies hidden client pack metadata.
- Asset index 34, SHA-1 `e99f9973047785b5a9d52eaf72020b97545053c9`.
  The current Mojang version/index documents were downloaded and hashed;
  the strict Eclipse guard rejected the earlier stale index pin.
- Later compatibility qualification targets Forge 66.1.5. It is not a pass
  or an automatic change to the baseline.

## Inputs And Boundaries

Required local inputs are basePower 2.4.18.2603001, Base Metals
3.1.2.2603001 with its shared hook, and OreSpawn 4.1.1.2603001 with the
retained-settings repair. All compiler inputs are pinned below. BM and OS
remain compiler-only inputs, not runtime-qualified releases. Neither 26.2
artifacts nor public 4.1.0 are substitutes.

- BM source `ac07e9cd960c4b48308fd3f245e06f108e4ac437`, JAR SHA-256
  `B03388D10E5B83046A5D2D540E7D956202A8A6CC78B2302A412D3092192CAAE8`.
- OS source `bbdd80a9c1f7972d81bedf8e6c2b1d4338ec9fa6`, JAR SHA-256
  `7FBAFEFF97683DA0E7ABD0035B85C3A19276B3AD9CB50DBC071F9993B33E490D`.
- Power handoff05 source `9d6e31a9c9d116b72a3758edfb18e81a25d939e3`, JAR SHA-256
  `F225B3EC1B9B272CCBA82716B6D105D3BF0A613E31437BBFA0F9CAB39E4FE9BB`.
- Frozen Power test-support source hashes are pinned in `gradle.properties`.
  The unchanged bootstrap agent SHA-256 is
  `088FC137BEBB9B2E75C596DE868499E6099CF228BECFCF07EAE36791245FD1B6`.

Only this existing Steam checkout is owned by this port. Power's shared
APIs and dependency hooks are coordinated separately. No parent output is
built by Steam. Local edits do not authorize a push, PR, tag or publication.
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
The inherited unit inventory starts at 276 tests. Eleven regressions were
added, for 211 annotated methods and 287 executed expanded tests in 49 suites.
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

Main, test and GameTest source scopes compile. Javadoc, exact dependency and
artifact audits, checksums and Eclipse production-only classpath checks pass
against handoff05. Installed ECJ 3.46.0 independently compiled all three scopes
and executed the complete 286-test inventory in `ecj-263-06-power04-final`, with zero
failures, ignored tests or assumptions. Omitted-suite, omitted-method and
stale-method controls all fail as required. The earlier ECJ run correctly
remained red while the dependency regression was red.
That ECJ proof uses Power04. The expanded inventory requires fresh ECJ on Power05.

The three current artifacts and source snapshot are frozen in evidence
`initial-power05-01`; the player/compiler JAR SHA-256 is
`D1027BE8E9A9DFB5187411BE944A58E165B6DC828581C206EEB817A4255F2DED`.
Cold inputs with spaces and reproducibility remain pending. The first cold
run regenerated the bare SDK and reached unit/artifact tasks, but was stopped
during Eclipse generation at the user's request to hold cold work until the
shared tag-condition diagnosis. Its interrupted receipt is preserved and is
not a complete cold pass. Passing compiler gates do not qualify packaged
runtime or gameplay.

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
and actual whole-production-table server checks are still being added. No global
Forge loot-modifier `conditions` arrays are converted.

The standalone fixture registers real Forge HolderSet types through native
NewRegistry/Register phases. Steam has no custom tag condition: it delegates
those to Power and retains mode/musket condition assertions. No Forge patch,
recipe-mode waiver or ConditionContext substitute is introduced.

All 24 recipe unlocks now use native `recipe_unlocked.recipes`. Their generator
copies each recipe's `forge:condition` intact onto its unlock advancement, while
retaining ingredient OR requirements, rewards and IDs. Two new tests exercise
the actual native criterion CODEC, every generated recipe ID and roundtrip,
and negative controls for the old field, missing references and empty criteria.
These isolated criterion tests do not claim actual recipe-mode availability.

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

Fresh TECH execution exposed the former oracle's unconditional NORMAL unlock
lookup. The runtime oracle now checks all 24 recipe/unlock pairs against their
exact condition trees and live bound tags, checks exact native recipe references
and ingredient OR groups, and retains the ingredient-positive/negative checks
even for intentionally absent variants by parsing their actual native criterion
data. The final native matrix is green in `native-matrix-power04-01`:

- NORMAL: four tests, 19 unlocks present and five absent.
- TECH_PROGRESSION: four tests, 18 unlocks present and six absent.
- APOCALYPTIC: four tests, 16 unlocks present and eight absent, all nine salvage
  recipes present with native codec roundtrips and unrelated-input negatives.

Each mode executes 1280 production trade offers, all eight codec negatives,
the provider witness and the legacy-plural semantic-loss witness. That is
12 successful native GameTests and 3840 offers, with no ERROR/FATAL log entries
or skipped cases. No availability assertion is removed or waived. These are
native Forge userdev checks, not packaged/client/endurance/manual qualification.
The matrix uses Power04 and is preserved as historical proof; Power05 requires
new server runs after the whole-table/golden oracle is complete.

`source-05` passes release configuration, hygiene, recipe guards, datapack and
source-layout checks and exact BM/OS development/player byte audits. The Node
source/data checks pass for 224 JSON inputs, 12 factories, 17 mining identities,
four enchantments, five trade definitions, fourteen trade negative controls,
fourteen block loot tables, the six-entry chest and its generated template,
three loot negative controls and 24 unlocks.
The earlier `compiler-01` missing-pin failure is preserved. No Power API stub
or legacy library was substituted to bypass it or the later failures.

Machine IDs, recipe and unlock IDs, drill directions, configuration precedence,
musket state/durability/enchantments, inventory and fluid state remain protected.
The historical qualification documents are unchanged and apply only to their
original candidates. This port is not qualified or ready for publication;
manual acceptance is not claimed.
