# Steam on Minecraft 1.21.11

## Accepted For Release

The current `F23DDC4ABB36A1C4C7A4205151FBB6DDFD25397E86909E1C0B146C4089B747E0`
player jar was accepted for release on 11 November 2026 with Power C98D,
Electric 5466, Base Metals 598D and OreSpawn DE39. Publication was authorized
in dependency order, but has not happened yet. The local dependency pins are
unchanged pending the released dependency source, jar hash and public file ID.
basePower's `docs/1.21.11/QUALIFICATION.md` records the full hashes and limits.

The current frozen family passes 132 finite jobs, upgrades, chunk/network and
drill lifecycle tests, recipes, world generation, oils, graphical checks and
two 30-minute endurance worlds with full restarts. Independent replay passes
all 66 checkpoints and exact final stopped machine data. These are new
current-jar results; the C2 evidence below remains historical.

The current forced, uncached source check passes all 252 unit tests with no
failures, errors or skips. This includes the portable Power atlas-test input
controls and two noninteractive native resource-test regressions. The child
uses terminal.jline=false with pipes and TERM=dumb, verifies 161 client and
91 server files, and rejects WARN, ERROR and FATAL diagnostics unchanged.
Its failing pre-fix receipt is preserved.

The earlier clean-path and normal builds, and the fresh real Eclipse compiler
run, passed all 250 tests before those two terminal regressions were added.
Those receipts are not relabelled as 252-test runs. All three jars reproduce
exactly and four fresh native source GameTests pass. No Steam production
change was needed during this rerun. Connected-player interaction
and live-world visual limits remain recorded; acceptance does not relabel
unrun checks as passes.

## Publication Preparation

The following is the preparation status on 11 November 2026.

The local Power source pin remains 41959572b11dce236df98f23e97c6a3868cdda03;
the Base Metals shared-hook pin remains 616d527e50e4bf0137eeb9c44fa586e34a0452a0.
Their public file IDs remain empty. CI and CodeQL deliberately require merged
upstream source and checksum-matching public jars before compilation. Those
gates must stay closed until the dependency releases are verified.

The upstream master-1.21.11 branch still points at merged 1.21.1 tip
b145f8d1a7c2a148f8f259faec7b203cf146b6b5, which is an ancestor of this port.
There is no hosted check for the local Steam HEAD or 2.2.16.121111 tag yet.
The current CI upload list names the correct player, sources and Javadoc
jars. Its out-of-layout build supplies the Power bootstrap and both audited
sources, and the atlas test now uses that same verified dependency jar.

The default-branch publisher needs the 1.21.11 Java 21 route and a sealed
release-registry entry. Only the route is prepared locally; the entry must
wait for the merged source and its successful CI artifact. The tag validator
checks the build job, not CodeQL or wrapper validation. No effective upstream
branch rules were returned by the audit, so all three successful checks must
also be verified before release. No tag, dispatch or publication is part of
this preparation.

## Dependency Metadata Correction History

Base Metals is REQUIRED on both client and server. The local mods.toml
declares it exactly once with mandatory=true, ordering AFTER and the unchanged
version range [3.1.2.121111,4.0.0). Distribution metadata already required it.
The existing recipe conditions remain defensive data guards; they do not make
a no-Base-Metals installation supported.

This correction changes player-jar metadata bytes, not version, production
classes, recipes, configuration or dependency pins. The previous 25EE player
jar is held under build/required-basemetals-41/held-before, with its checksum
recorded before editing. Focused native ModInfo regression/static checks and
any local candidate-byte report are recorded under build/required-basemetals-41.
These local edits are not a frozen release or whole-world qualification.

Power's approved metadata source 41959572b11dce236df98f23e97c6a3868cdda03
and SHA-256 C98D2CF163BE2480D9AFB0023BBD2D92D401AE0D50BEF73191235BF3F83774A7
are now pinned for the consistent default build. The RED attempt used the
preserved, hash-checked D input without changing its committed pins at that
stage. No OreSpawn change, publication, version bump or packaged-world/endurance
campaign is part of this correction. The smoke helper rejects its former
no-Base-Metals mode before starting a runtime.

## Historical C2 Proof

This is historical C2 candidate qualification against Power134F, not an
overall family release claim. The parent reports a later, broader chained
1.13 dimension route exposing a Power dynamic-biome-registry issue; its fix
and replacement pin are outside this preserved candidate's proof.

Candidate C2 pins the local Power registry, BlockItem-name and atlas fixes
and the Base Metals callback fix below. A/B evidence remains preserved under
build/qualified-A-63eef004 and build/native-B-35, not relabelled C2.
The forced 43-task source/release suite, actual Eclipse ECJ scopes, all four
GameTests and both packaged save/reopen profiles pass against these pins.
The ordinary Steam player jar matches the frozen C2 client candidate:
25EE7784EAA180C77B7735B90928801F56A7342B5733B49222E2B0E72E5577F9.
Only assets/minecraft/atlases/items.json differs from the earlier 5B4D jar;
all production class payloads are unchanged.
Exact dependency hashes and source commits are build-audit pins, not embedded
manifest fields; the runtime metadata retains the same dependency versions.

Local source port 2.2.16.121111 targets Forge 61.1.0, official mappings,
MCP 20251223.124241, Java 21, ForgeGradle 7.0.34 and Gradle 9.6.1.
The starting source is manually accepted 1.21.1 commit
9c5bf9b4610e06d3b5a6d5a0bde38d7516327d96. Its 228 unit tests and four
GameTests are a baseline, not qualification of this port.

The approved local basePower 2.4.15.121111 integration candidate is pinned to
a1cd197b4b6c3fc9c5d56da254d0f0ae9929fe8c and native jar SHA-256
134FF55595E86D450DBFB5531F389BF0BE81FE6347F1EB03765B571A1D488B59
with exact native unit-bootstrap and registry-support source hashes. The
default local dependency paths pass without path overrides. Public release
file IDs for local-only candidates remain empty and hosted publication gates
remain closed.
The screen-aware overload uses the native Matrix3x2fStack and retains its
independent pivot and sweep geometry checks. The deprecated global overload
retains its signature and delegates through the current native machine screen
only during its actual decoration callback. No shared Power API or runtime
jar change was needed. Submission and failure paths restore the caller matrix.
The local Base Metals 3.1.2.121111 shared-hook callback fix is pinned to source
616d527e50e4bf0137eeb9c44fa586e34a0452a0 and jar SHA-256
598D3FB0FF956EBCC67FE477A01CE393C75D0013AF0CE00A79E2072B63E9F2BA.
Earlier receipts used Power source 398439074a60bf9b8f8872f92485cffc4aa96be9
and jar 0240E032075AF6B158887FBF98CE038504942329527036B82FD7CF9EEE602503,
with Base Metals source e6b869ffe7d6fd6a011a99f1844d2a7995394daf and jar
6D24D760E8E025960095DE04C51E6B79CFF93071618FC0B6BF64E05C346CC097.
The parent's real-handler regression found a cancellable RETURN ordering
defect in those shared inputs; they and their receipts remain preserved under
build/native-pinned-28, not relabelled as fixed or used for current proof.
OreSpawn 4.1.0.121111 is pinned to the published native jar.
Forge installer, server shim, server and universal hashes were checked against
the authentic local runtime seed and the proven native Base Metals reference.

Persistent IDs, the legacy namespace, recipe identities, configuration
keys and saved machine fields are unchanged. The native port uses keyed
block/item properties, Identifier, Forge event bus groups, value-based
block-entity persistence, server recipe access and render-state submission.
Ingredients use native string/tag selectors. Native item definitions retain
the musket loaded byte and using-item model choices.

The mod root declares data format [94,1]. showAsResourcePack=false keeps the
client assets in Forge's native hidden aggregate, which supplies format 75.x
metadata. Both former codec regressions now exercise actual Forge mod-file,
pack-discovery and resource-reader paths. No fallback metadata, ignored codec
warnings or synthetic replacement APIs are used. Omitting pack.mcmeta does
not synthesize suitable metadata in the native PathPackResources supplier.

Forge 61.1.0 independently labels server mod packs using the client version
75.0, so its discovered server pack reports TOO_NEW even though native
SERVER_DATA metadata correctly accepts 94.1. The receipt records both values;
this platform compatibility-label issue remains open, not silently waived.
See FORGE61-RESOURCE-METADATA.md for parent integration guidance.

The default local check/build/Javadoc/dependency/archive/checksum/Eclipse
metadata suite passes (43 tasks), with 244 unit tests and zero failures,
errors or skips. All three real Eclipse .classpath scopes compile using ECJ
3.46.0; those ECJ-compiled unit tests also pass with zero skips/assumptions.
Both source compilers include production, unit and GameTest Java sources.
All four native GameTests pass, selected by basesteam:* with exact named XML
results and no failures/skips. Forge 61's test-function registry and native
test_instance resources are confined to the isolated GameTest source scope.
The first launch's built-in-only one-test result correctly failed the gate.
Historical pre-clarification dedicated startup/save/reopen evidence exists
for both the with-Base-Metals and without-Base-Metals inputs. The latter is
retained only as old evidence: it is not supported by the current REQUIRED
dependency policy and does not qualify the new metadata bytes. Every old process verifies
the exact copied candidate hashes and checks latest/stdout/stderr without
filtering loader/data errors. These profiles explicitly select native NIO
transport on Windows. The earlier default native-availability selection failed
the strict stderr gate on the platform's Log4j/Netty unsupported kqueue/epoll
probe; its failed receipt is retained, not waived as a clean default run.
The parent's pure-platform receipt independently shows the same diagnostic.
The empty-cache platform proof is preserved under build/repro path with
spaces 34. C2's independent path-with-spaces reproduction is recorded under
build/repro path with spaces 39, with a fresh source stage and the same
native-engine cache, not another cold download. It uses C2's exact pins and
no dependency path overrides. Combined migration/endurance and manual acceptance remain
separate parent/user gates; these isolated checks do not establish them.
Nothing has been pushed, tagged or published.

Merged 1.21.1 source b145f8d1a7c2a148f8f259faec7b203cf146b6b5 changes only
release pins and documentation from 9c5bf9b4610e06d3b5a6d5a0bde38d7516327d96.
Historical release notes were carried forward without importing 1.21.1 pins
or transferring its runtime acceptance to this port.

The native item atlas contains only the items directory with prefix items/.
The drill single source remains exclusively in Steam's block atlas. Native
SpriteSources/resource-reader tests cover every model texture and reject
block/material sources in ITEMS and any item-atlas resolution of world-block
textures. Power's block atlas intentionally retains legacy items/ lookup;
true item-icon overlap is tested positively, not mistaken for the block/material
shadowing defect. The omitted-Power and malformed-source controls remain strict.
Both native BlockItem description and hover-name keys are checked through
the actual registration factory for all 17 blocks, with a missing-prefix
negative control. These proofs run in child JVMs so intrusive holders cannot
leak into later registry-freeze tests. The initial isolation failure is retained.

The parent's actual frozen combined client run 20261111-182839-413cf9f7
passes with this exact Steam jar, Power134F, BM598D and the other frozen
profile inputs: 94 items, 905 states, 39 GUIs, 351 caption checks, 355 rejected
negative controls, 26 fluid-gauge pixel cases, recipe-book grouping and zero
resource errors. The frozen profile hash is
9109C8904BE3D42185F5A636EC5F0EF6ED36D5DD5986F14E4B607EC03704D700.
Input copies/origins and the receipt hashes were checked independently;
build/native-C2-38/frozen-client-proof-receipt.json records the binding.
Worldless renderer geometry retains its reported manual gaps; automated
client resource proof is not manual gameplay or world-renderer acceptance.

Independent checks pass for the source/resource contracts, 24 recipe unlocks,
33 normalized recipe payloads and 113 unchanged protected resource files.
The two companion native jar audits pass. Eclipse metadata generation passes.
The earlier 27-test receipt with two pack-codec failures is retained. Current
native resource-reader tests verify 161 client and 91 server files byte for
byte from a resource-only fixture jar, with no WARN/ERROR diagnostics.
The refreshed independent batch compiles with javac and actual Eclipse ECJ and
passes 32 tests. Native Ingredient.CODEC controls cover strings, tags and
alternative arrays, rejecting legacy object ingredients, empty lists and
malformed selectors. ItemPredicate.CODEC controls prove the native discovery
predicates remain restrictive and expose the ignored old item field.
The generator's 24 checked-in unlocks remain unchanged and --check passes.
Earlier evidence is under ignored build/native-independent-26 and
build/steam-native-compile-22. Current C2 evidence is under
build/native-C2-38, build/ecj-native61-C2-38-final,
build/steam-gametest-native61-C2-38-final and
evidence/packaged-server-native61-C2-38-{with,without}-metals-nio.
The C2 archives are compared against their exact graphics/client-bound bytes.
Earlier source/archive/ECJ and old-pin cold receipts remain under
build/native-pinned-28 and build/repro path with spaces 32. Failed attempts remain
alongside later runs, including the old ValueOutput namespace false positive
and initial atlas-fixture setup failures. C2 also retains the initially
over-broad global atlas-disjointness failure; the approved native contract
forbids block/material shadowing while preserving legacy true-item lookup.
No gameplay or world-upgrade
qualification is implied by source/asset checks.

The parent's migration-probe module remains an unchanged 1.10/1.12 source
fixture writer and was not rebuilt by this child. Native C2 profiles instead
use the pinned native AdvantageWorksTestHarness; they do not load that legacy
probe. No native variant or unrun legacy-probe build is claimed as passing.
