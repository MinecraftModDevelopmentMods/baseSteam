# Steam on Minecraft 1.21.11

Local source port 2.2.16.121111 targets Forge 61.1.0, official mappings,
MCP 20251223.124241, Java 21, ForgeGradle 7.0.34 and Gradle 9.6.1.
The starting source is manually accepted 1.21.1 commit
9c5bf9b4610e06d3b5a6d5a0bde38d7516327d96. Its 228 unit tests and four
GameTests are a baseline, not qualification of this port.

The approved local basePower 2.4.15.121111 integration candidate is pinned to
398439074a60bf9b8f8872f92485cffc4aa96be9 and native jar SHA-256
0240E032075AF6B158887FBF98CE038504942329527036B82FD7CF9EEE602503
with exact native unit-bootstrap and registry-support source hashes. The
default local dependency paths pass without path overrides. Public release
file IDs for local-only candidates remain empty and hosted publication gates
remain closed.
The screen-aware overload uses the native Matrix3x2fStack and retains its
independent pivot and sweep geometry checks. The deprecated global overload
retains its signature and delegates through the current native machine screen
only during its actual decoration callback. No shared Power API or runtime
jar change was needed. Submission and failure paths restore the caller matrix.
The local Base Metals 3.1.2.121111 shared-hook candidate is pinned to source
e6b869ffe7d6fd6a011a99f1844d2a7995394daf and jar SHA-256
6D24D760E8E025960095DE04C51E6B79CFF93071618FC0B6BF64E05C346CC097.
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
Packaged runtime, combined migration/endurance,
cold/path-with-spaces reproducibility and manual acceptance remain open.
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
native resource-reader tests verify 160 client and 91 server files byte for
byte from a resource-only fixture jar, with no WARN/ERROR diagnostics.
The refreshed independent batch compiles with javac and actual Eclipse ECJ and
passes 32 tests. Native Ingredient.CODEC controls cover strings, tags and
alternative arrays, rejecting legacy object ingredients, empty lists and
malformed selectors. ItemPredicate.CODEC controls prove the native discovery
predicates remain restrictive and expose the ignored old item field.
The generator's 24 checked-in unlocks remain unchanged and --check passes.
Earlier evidence is under ignored build/native-independent-26 and
build/steam-native-compile-22. Current full-source/archive evidence is under
build/native-pinned-28 and build/ecj-native61-first. Failed attempts remain
alongside later runs, including the old ValueOutput namespace false positive
and initial atlas-fixture setup failures. No gameplay or world-upgrade
qualification is implied by source/asset checks.
