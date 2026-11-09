# baseSteam 1.18.2 port notes

The release branch uses the shared Base Metals 3.1.2 migration hook and the
published basePower 2.4.10.118021 build.
See [gradle.properties](../gradle.properties) for the exact Power, Base Metals
and OreSpawn pins.

The first locally tested Steam player jar had SHA-256
`2E7131FD22F9486905212F64323F5E11EEFBE0A538499E92477373D0C5D2A2CC`.
The notes below record earlier port milestones and their original inputs.

- Source: PA117 `feature/basesteam-1.17.1-identity-migration` at
  `c8e3febcd11c6854d8073c9c053c7c162cdd6687`, cloned without hardlinks.
- Branch: `feature/basesteam-1.18.2-port`; candidate `2.2.11.118021`.
- Target: Forge `40.3.0`, official `1.18.2`, MCP `20220404.173914`,
  Temurin `17.0.1+12`, ForgeGradle `7.0.34`, Gradle `9.6.1`.
- The original port was tested locally on disposable worlds; the notes below
  describe that candidate, not the later publication build.

## Port changes

Blocks, items and enchantments are constructed in their own Forge registry
events. Machine GUI definitions run in queued common setup. Power's shared
creative tab, conduit identifiers, machine GUI API and migration API are kept.
Steam creates no Power fluid instance during block registration.

Native block-entity saves now use `saveAdditional` and invoke the Power/Forge
superclass hook. Existing keys and value representations are unchanged. Drill
relocation uses full native metadata; drill-bit updates use native typed packets
and retain the direction-only packet payload. The musket uses native item tag
keys for the same blackpowder ammo and steel tags.

Mod ID, Java packages, registry paths, migration aliases and data/asset files
are unchanged. The only changed production resources are `mods.toml` target
and dependency ranges and `pack.mcmeta` (data 9, resources 8).

## Exact inputs

The Base Metals and OreSpawn hashes below are local inputs. The source commits
identify the observed checkout HEADs, not independent rebuild or public-byte proof.

| Input | Checkout HEAD | Development SHA-256 | Player SHA-256 |
| --- | --- | --- | --- |
| Base Metals 3.1.1.118021 | `5261c13744bbec2c2d394ba90bdce4ff92cc465a` | `CA896792EB3AA09264EF61068CFB6FEAA8AE3975186BE8E10C062CEB971B0354` | `0B72EFB4868CB49436B5DB07EA4606DA4B5EFDF84E510C8A48FD4264F7C110B6` |
| OreSpawn 4.1.0.118021 | `d31f52f296739f34a0c13f554abf3e1ac071ae25` | `34B25B2116FD91B0050C80393F8356A5CA256E707BF443E4882BE5CFAD1335B6` | `BD8D84A96DAFB7668E95D61B9D72C62AEF3440675473E41244C182C64653356F` |
| basePower 2.4.10.118021 | `8bd3b8213554551fc688a46eb1653a0b572b8d0a` | `CDDB8E1BB6998F625BEEF677B4E2FC87847A450D77775DA86CF31566A926D28F` | `3C3E9154BC0F7CC88C4AE4C5F0044527D7F39BBF12C71F83AD99CF2E9646E273` |

Final Power inputs are in
`D:/PowerAdvantage/_verification/base-118-port-20261109/power-source-pin-a/`.
Earlier provisional receipts used API-only SHA-256
`13A8E80FA768E836D7F9998533098AEB3467E56821D11EB595A097A1A068CC26`;
the final pin audit no longer accepts it.
Companion JARs were audited directly from the requested local checkouts.
The IAF118 Mavenizer fixture is checksum-verified and build-only. It corrects
the Forge 40 decompiler's redundant `Holder.Direct.value()` accessor.

## Local receipts

Receipts are in the clone's ignored `evidence/port118/` directory.

- `01-companion-audit.log`: failed before module tasks on the stock Mavenizer
  Forge 40 `Holder.Direct` recompile. `02-provisional-compile.log`: fixed
  recompile, but cold offline injection lacked the exact Forge universal JAR.
- `javac-preflight-a.log`: old musket item-tag calls fail on 1.18.2.
  `javac-preflight-test-b.log`: old fuel test tag loader fails on 1.18.2.
  `junit-preflight-c.log`: standalone Forge registry freeze and old pack
  assertion; retained as a failed preflight.
- `03-provisional-compile.log`: audited four companion JARs and the frozen API,
  processed resources, compiled main/test Java and built a development JAR.
- `06-registry-lifecycle-red.log` and `06-test-results/`: three expected
  pre-fix lifecycle failures. `07-provisional-lifecycle-green.log` and
  `07-test-results/`: 206 tests across 30 suites, zero failures/errors/skips,
  development JAR and Eclipse production classpath/configuration audit.
- Development JAR SHA-256 at that checkpoint:
  `8345A977EA8D40CD393006C9EFA87F4BA66388BC0098510E538124DFC610871A`.
  Mapped Forge development JAR SHA-256:
  `6D3A11ADEEB84F2DCE591603451411F348EC9F5418D61284B3AEDEFAC00ACDF7`.
- The provisional tests used the test-only agent built inside this clone
  from the unmodified Power bootstrap Git blob
  `c9780d80465b3ffe5ef00752abb4a94097233843`. Agent SHA-256:
  `137EFCA4B74C6905DB2ED168551BE32416CE061DF3F45A5AED2D701E1FC9DBCC`.
- `08-provisional-release-rejected.log` confirms release audits reject
  the API-only input. `09-companion-variant-rejected.log` confirms the
  development audit rejects the otherwise valid Base Metals player JAR.
- Source-only verification: 179 resource JSON files, 12 block-entity
  factories, 17 harvest entries and 24 generated recipe unlocks checked.

## Packaged server diagnostic

`10-diagnostic-remap.log` built a Forge-remapped Steam intermediate, not a
release JAR. The compile input remained the frozen API-only Power JAR.
The separate Power player input was copied from its existing successful
`smoke-power-c` receipt and independently hashed before staging.

- Steam diagnostic SHA-256: `4B381E6F548696649328C57FF2DCAC4C83E51C6D214E94BA32C0D2D04E9D4BD8`.
- Power diagnostic player SHA-256: `FE7F0EDD01F16A7FA33DD748E0395FEDE8DEC32FE77E5B27A6F13EED7D712C55`.
- With the above Base Metals and OreSpawn player JARs, a new disposable
  Forge server reached `Done`. Both fresh boot and cold restart read back
  `basesteam:steam_boiler_coal`, `Tank={Empty: 1b}` and the drill-bit
  `dir=5b`. Both stopped cleanly with no error-level console entries.
  Exact staged hashes and console receipts are in `packaged-server-a/`.
- A separate new world in APOCALYPTIC mode with no Base Metals JAR reached
  `Done`, passed the same fresh state checks, saved and stopped with no
  error-level console entries. OreSpawn stayed installed because Power
  requires it. Exact receipts are in `packaged-server-no-basemetals-a/`.
  This checks startup without the optional serializer, not crafting balance.

## Matched Power release and reproducibility

The full local suite passed with the exact Power source and JARs above.
Both the primary checkout and a clean source export in a path with spaces
executed 35 Gradle tasks and passed 206 JUnit tests in 30 suites, with no
failures, errors, or skips. The Eclipse production classpath and launch
configuration audit passed in both paths; Eclipse itself was not launched.

```text
check build deobfJar javadoc verifyReleaseDependencies verifyReleaseArtifacts
writeReleaseChecksums verifyPlayerDependencies verifyEclipseProductionClasspath
```

Both builds used `--offline --no-daemon --no-build-cache --rerun-tasks`
and `ORESPAWN_MAVENIZER_OFFLINE=true`, with explicit Temurin 17/8/25
toolchain paths and no automatic toolchain detection or download. The clean
export had no previous build or project cache. Both used the same warmed
dependency cache, so this is a clean-source reproducibility check, not a
cold dependency-cache build.

- Exact source Git tree: `9fbf09d19d9324429d5dce448addda54ac478607`.
  The source archive was captured before this results section was added.
- `source.zip` SHA-256:
  `C86B1C2597B0B71D2B5BBD37284BE5534A2374E4E50BD3E672F00EE1EF044DEB`.
  The exported Git index independently reconstructed the same tree.
- Test-only Power capability agent SHA-256:
  `A71C3FB1B83DEAF13E3D40A533055DB2917B21EA3335C7D06164173EBB5B0F35`.

All four JARs and the generated `SHA256SUMS` file matched byte for byte:

| Frozen artifact in `evidence/port118-release-a/artifacts/` | SHA-256 |
| --- | --- |
| `baseSteam-2.2.11.118021.jar` | `C35DB2E2F1012184F2CC1EBF01032290EC520868F0A0DB24E98F9C88CFE57942` |
| `baseSteam-2.2.11.118021-deobf.jar` | `8345A977EA8D40CD393006C9EFA87F4BA66388BC0098510E538124DFC610871A` |
| `baseSteam-2.2.11.118021-sources.jar` | `525F2E149F8C64752EF6E8B77EEF7175DE42BDC7F7395B727BAB7B9776FA8F12` |
| `baseSteam-2.2.11.118021-javadoc.jar` | `718C301FD737E3DC1BC7C13D48AD70027E15D7B662E7C673C12FB07990B24C24` |
| `SHA256SUMS` | `EC071EB6709784B27699AD989DB0ED38FEE221353C30F15DC971FDE511DD02E2` |

Primary Gradle, JUnit and source archive receipts are in
`evidence/port118-release-a/`; the independent repeat and
`comparison.json`, `byte-comparison.txt` and `test-count-comparison.json`
are in `evidence/port118-release-repro-a/`. The matched pin audit rejects the
old API-only JAR by SHA-256, as recorded in `old-api-rejected.log`.
The historical provisional and failing receipts remain in `evidence/port118/`.

## Matched packaged server smoke

The frozen Steam release JAR and the matched Power, Base Metals and OreSpawn
player JARs above were copied into a fresh loopback-only Forge 40.3.0 server
inside this clone. The fresh boot and cold restart passed with exit code zero
and no error-level console entries. Both runs read the same coal boiler ID,
`Tank={Empty: 1b}`, and drill-bit `dir=5b`. The staged hashes, commands, and
both receipts are in `evidence/port118-release-a/server-smoke/`.

## Pending gates

The combined harness, Eclipse launch execution, packaged client/GUI,
legacy-world migration, and manual gameplay acceptance are not covered
by this local release suite and narrow server smoke. No earlier version's
results qualify this candidate.
