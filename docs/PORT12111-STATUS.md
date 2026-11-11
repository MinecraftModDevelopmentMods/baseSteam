# Steam on Minecraft 1.21.11

The coordinated local Power/Base Metals callback fixes are pinned below.
The forced 43-task source/release suite, actual Eclipse ECJ scopes, all four
GameTests and both packaged save/reopen profiles pass against these pins.
The ordinary Steam player jar remains byte-identical to the frozen candidate:
5B4D7743B5507060D4D11E77944814E66257AAC9089C47E423E3E3A165912648.
Exact dependency hashes and source commits are build-audit pins, not embedded
manifest fields; the runtime metadata retains the same dependency versions.

Local source port 2.2.16.121111 targets Forge 61.1.0, official mappings,
MCP 20251223.124241, Java 21, ForgeGradle 7.0.34 and Gradle 9.6.1.
The starting source is manually accepted 1.21.1 commit
9c5bf9b4610e06d3b5a6d5a0bde38d7516327d96. Its 228 unit tests and four
GameTests are a baseline, not qualification of this port.

The approved local basePower 2.4.15.121111 integration candidate is pinned to
1f5085f1cc7adbcb1bd8cd1062601d490e5749f3 and native jar SHA-256
D8BA2501148845FFE1659569D1CB00C242C462123CB5A6B89D68FC346CABA7E9
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
metadata suite passes (43 tasks), with 242 unit tests and zero failures,
errors or skips. All three real Eclipse .classpath scopes compile using ECJ
3.46.0; those ECJ-compiled unit tests also pass with zero skips/assumptions.
Both source compilers include production, unit and GameTest Java sources.
All four native GameTests pass, selected by basesteam:* with exact named XML
results and no failures/skips. Forge 61's test-function registry and native
test_instance resources are confined to the isolated GameTest source scope.
The first launch's built-in-only one-test result correctly failed the gate.
Packaged dedicated startup/save/reopen passes in two separate JVMs for both
the with-Base-Metals and without-Base-Metals profiles. Every process verifies
the exact copied candidate hashes and checks latest/stdout/stderr without
filtering loader/data errors. These profiles explicitly select native NIO
transport on Windows. The earlier default native-availability selection failed
the strict stderr gate on the platform's Log4j/Netty unsupported kqueue/epoll
probe; its failed receipt is retained, not waived as a clean default run.
The parent's pure-platform receipt independently shows the same diagnostic.
Cold/path-with-spaces reproducibility is recorded separately under
build/repro path with spaces 34, using the repaired inputs and no dependency
path overrides. Combined migration/endurance and manual acceptance remain
separate parent/user gates; these isolated checks do not establish them.
Nothing has been pushed, tagged or published.

Merged 1.21.1 source b145f8d1a7c2a148f8f259faec7b203cf146b6b5 changes only
release pins and documentation from 9c5bf9b4610e06d3b5a6d5a0bde38d7516327d96.
Historical release notes were carried forward without importing 1.21.1 pins
or transferring its runtime acceptance to this port.

The native item atlas now explicitly includes basesteam:entity/drill_bit in
addition to the block atlas. A native SpriteSources/resource-reader test
checks every Steam model texture against the exact Power and Steam packs,
including the legacy material IDs, and an omitted-Power negative control.

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
build/steam-native-compile-22. Current repaired-pin evidence is under
build/native-repaired-33, build/ecj-native61-repaired-33,
build/steam-gametest-native61-repaired-33 and
evidence/packaged-server-native61-repaired-33-{with,without}-metals-nio.
All three rebuilt Steam archives match the frozen artifacts byte for byte.
Earlier source/archive/ECJ and old-pin cold receipts remain under
build/native-pinned-28 and build/repro path with spaces 32. Failed attempts remain
alongside later runs, including the old ValueOutput namespace false positive
and initial atlas-fixture setup failures. No gameplay or world-upgrade
qualification is implied by source/asset checks.
