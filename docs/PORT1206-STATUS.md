# Steam 1.20.6 Local Status

- Checkout: `D:/PowerAdvantage/PA1206/SteamAdvantage/SteamAdvantage`.
- Baseline: `master-1.20.6` at accepted Steam `17c6b8467d5c488e101b53b992537d6d9b251b87`.
- Work branch: `feature/basesteam-1.20.6-port`; no configured remotes, matching the source checkout.
- Target: baseSteam 2.2.14.120061, Forge 50.2.0, Java 21, official 1.20.6 mappings.
- Scope: Steam only. No push, tag, PR, deployment, publication, or use of existing worlds.

## Coordinated Inputs

| Input | Commit | Local native jar SHA-256 |
| --- | --- | --- |
| basePower 2.4.13.120061 | `75297fd9f4ce679aca95c2948e81bbef7633b976` | `29C1BD5EF384F8BB2441029061615E52A4B7302B9FC5480F2CE9C84C357A3181` |
| Base Metals 3.1.2.120061 (local shared hook) | `0c2cdd4d0e8c3bf2e5fa234d49e57960bbf80bde` | `FC6DE01771ED9CD799D946A0ED4B6782B289C47AA8CFCA7753F9E102DD0F0417` |
| OreSpawn 4.1.0.120061 | `4085315345ff043448b316d75e03f4b8da45402d` | `FAA7107B9FB84E2C62ED847CAFD880AA3C4238A37799BAD955EA9602A944AA26` |

The native jars serve as both development and packaged inputs. The Power
JUnit bootstrap is pinned to
`96752A443070E117EED9308ABC9C2009BD3533F121C9594A7108908E104673C9`;
both bootstrap source hashes are recorded in `gradle.properties`.
No dependency is embedded or promoted to a publication claim.

## Repinned Candidate Verification

Steam commit `23cb29f9328bc52ebb0a95ae4df7f7e765cf71b7` pins the Power
datapack loot fix. Steam production sources, the bootstrap, Base Metals and
OreSpawn did not change. A forced rebuild and direct byte comparison confirmed
that the Steam jar is identical to the first exact candidate:
`5DDABC83B01A8CC8BAE3188BAAACE01BDCF6F756B7049DADF1ADAA3F9CF480FD`.

The current frozen artifact is
`evidence/steam1206-exact-75297fd/candidate/baseSteam-2.2.14.120061.jar`.
Its receipt records both Power and Steam hashes and the byte comparison.

| Gate | Result | Receipt |
| --- | --- | --- |
| Forced rebuild, exact development and player inputs, archive audit | Passed | `evidence/steam1206-exact-75297fd/build-audit.log` and `input-checksums.json` |
| Full Gradle and Eclipse checks | 221 JUnit tests passed; no failures or skips | `evidence/steam1206-exact-75297fd/full-suite.log` and `junit-summary.json` |
| Forge development GameTests | All four passed | `evidence/steam1206-exact-75297fd/gametest-server.log` |
| Packaged dedicated server | Clean boot and shutdown in both profiles | `evidence/packaged-server-exact-75297fd-with-basemetals/receipt.json` and `evidence/packaged-server-exact-75297fd-without-basemetals/receipt.json` |
| Source parity | 33 recipes and 24 unlocks verified; block loot and harvest tags unchanged | `evidence/steam1206-exact-75297fd/static-parity.log` |

## First Exact Snapshot

These receipts apply to Power commit `5fdbb2c2b2d9bbc760aca3919e5b5b648f4545e2`
and jar SHA-256 `E192657CC426BC8CD0E3809F6CBF53A135E5C49C6E34A8936DE2DD034D701A04`.
The Steam checks below passed for that snapshot. Integrated results on that
Power jar remain provisional because live progression found that an overridden
dungeon loot table was augmented. No old receipt was promoted or replaced.

The frozen `baseSteam-2.2.14.120061.jar` is in
`evidence/steam1206-exact-5fdbb2c/candidate/`. It was built from Steam commit
`756fd62385806644c2475fe6a4d9cbe9290b12d7` and has SHA-256
`5DDABC83B01A8CC8BAE3188BAAACE01BDCF6F756B7049DADF1ADAA3F9CF480FD`.
Before the repin, later Steam commits changed only verification, Eclipse
generation, and documentation.
The ordinary `build/libs` jar matched the frozen bytes before and after both
packaged runs.

| Gate | Result | Receipt |
| --- | --- | --- |
| Exact development and player dependencies; archive audit | Passed | `evidence/steam1206-exact-5fdbb2c/build-audit-b.log` and `input-checksums.json` |
| Full Gradle suite | 221 JUnit tests passed, none failed or skipped; asset, recipe, and repository checks passed | `evidence/steam1206-exact-5fdbb2c/full-suite.log` and `junit-summary.json` |
| Forge 50 development GameTests | All four required tests passed; no ERROR/FATAL records | `evidence/steam1206-exact-5fdbb2c/gametest-c-server.log` |
| Packaged Forge dedicated server, with Base Metals | Clean boot and shutdown | `evidence/packaged-server-exact-5fdbb2c-with-basemetals/receipt.json` |
| Packaged Forge dedicated server, without Base Metals | Clean boot and shutdown; OreSpawn retained | `evidence/packaged-server-exact-5fdbb2c-without-basemetals/receipt.json` |
| Eclipse generation and production classpath | Passed; no Eclipse client was launched | `evidence/steam1206-exact-5fdbb2c/eclipse-b.log` |
| Source parity | All 33 recipes and 24 unlocks verified; block loot and harvest tags unchanged | `evidence/steam1206-exact-5fdbb2c/static-parity.log` |

The initial archive audit lacked a task dependency on the development jar.
The two initial GameTest runs stopped before tests on scratch-directory and
batch selection issues; the final run seeded server properties and used the
explicit `basesteam` batch. Initial Eclipse generation omitted `MOD_CLASSES`.
These are verification fixes, not production changes. Failed attempts remain
in the evidence directory alongside the passing receipts.

## Provisional Evidence

- `build-steam1206-companions.log`: initial offline cache-only rejection.
- `build-steam1206-companions-normal.log`: Forge configured, then the stale
  Base Metals migration-class marker failed. Steam only consumes the crushing API.
- `build-steam1206-static.log`: companion development/player and recipe guard
  audits passed; the full check rejected the pending Power source pin as intended.
- `evidence/steam1206-native-data-third.log`: 15 independent Forge codec and
  resource checks passed, plus the recipe guard, Java layout and hygiene tasks.
  Earlier failures and their XML remain under `evidence/`.
- The source verifier passed and all 24 generated recipe unlocks matched.
- `evidence/steam1206-resource-parity.log`: all 33 recipes retain their exact
  inputs, output counts, and ordered guards. Block loot and harvest tags
  are byte-identical to the accepted source.
- `evidence/steam1206-provisional-compile-d.log`: Java 21 production, unit,
  and GameTest sources compiled against the coordinated provisional Power
  jar. Its SHA-256 before and after was
  `FABB0EE5B742F5D7F36752C1730BE8253632D2761B27E28E307A08662372A4AC`.
  The final Power audit was explicitly excluded for this compile only.
  The compile-a through compile-c failures are retained.
- `evidence/steam1206-provisional-side-compile.log`: all three source sets
  compiled after the side-only changes against provisional Power SHA-256
  `AF198E46C86FFC3F684C213F3A2A01F12D1A8D6AF12CD048877D10FBCE70DA6B`.
- `evidence/steam1206-independent-native-side.log`: 21 checks passed,
  including the native drill renderer geometry and the client-only GUI
  signature regression. Neither receipt is a packaged runtime pass.
- `evidence/steam1206-provisional-unit-d.log` and its XML results: 219 passed,
  none failed or skipped. Only the two final pin-contract tests were excluded.
  Inputs included provisional Power `82B1554AF83DF6FF2D737FA22D8548736B7F23BC182659A12DEAB254ED4CA6C6`
  and the local Base Metals shared-hook candidate
  `FC6DE01771ED9CD799D946A0ED4B6782B289C47AA8CFCA7753F9E102DD0F0417`.
  The a through c fixture failures and XML are preserved.
- `evidence/steam1206-provisional-unit-f.log` and its XML results: the same
  219 tests passed after the fragile-block fix, using the updated shared
  Forge 50 JUnit agent
  `96752A443070E117EED9308ABC9C2009BD3533F121C9594A7108908E104673C9`.
  The e-run exposed overlapping loader initialization, so Steam now lets
  the shared agent own that setup and retains only its fluid registry fixture.
- `evidence/steam1206-provisional-gametest-c.log`: Forge reached the dedicated
  development loader, but provisional Power's `MachineGUIRegistry` auto
  subscriber was rejected for carrying `@OnlyIn` methods. No Steam tests ran,
  despite the launcher returning zero. The new task gate requires the actual
  four-test pass marker and a fresh run ID. The crash is under
  `build/steam-gametest-provisional-c/crash-reports`.
- `evidence/packaged-server-provisional-a`: the real packaged Forge server
  reached Steam common setup, `Done`, and a clean stop without a client-side
  class error. Its original startup-only receipt is preserved, but the full
  log rejects this as a clean data smoke: Forge could not bind the fragile
  block tag because Minecraft renamed `grass` to `short_grass`. The smoke
  now fails on error logs and the registry test checks every required ID.
- `evidence/packaged-server-provisional-b/receipt.json` and
  `evidence/packaged-server-provisional-c-no-basemetals/receipt.json`:
  the corrected packaged Steam jar
  `6AF7260CF4950998816246029FB9F5C3E5A424512A173E46DD6A80E41ED76A8C`
  booted and stopped cleanly on Forge 50.2.0 in both profiles, with no
  ERROR/FATAL records. These runs used the frozen provisional Power jar
  `82B1554AF83DF6FF2D737FA22D8548736B7F23BC182659A12DEAB254ED4CA6C6`,
  not a final Power source pin.

The isolated data checks use Forge 50.2.0 classes but do not start the Forge
mod loader or qualify Steam's production classes. The GameTest fixtures are
separate from packaged content. The empty test structure is a copy of the
minimal generated fixture from the local Base Metals 1.20.6 validation build
(SHA-256 `732772B538B1B647EACD1B7E5F27C74F5CB556B1C6BD91744008575331E7C9E2`).

Steam's ten machine-menu decoration overrides were already client-only.
Client renderer registration is now also isolated in a client-only class.
The ASM side contract checks this without pretending to substitute for a
packaged dedicated launch. The smoke script copies the checked Forge install
and pinned player artifacts into a new, disposable Steam evidence directory.

The `75297fd` candidate is ready for shared endurance, not manual
acceptance. Combined Power/Steam/Electric validation, manual client and
multiplayer checks, and historical world acceptance require their own
evidence. No release or publication was performed.
