# Minecraft 26.2 combined checks passed

> Historical candidate record, 12 November 2026. The exact inputs and pending
> gate wording below are preserved from that checkpoint, not current download
> instructions. See the [13 November 2026 release record](RELEASE262.md) for
> the published source and files. This note adds no gameplay qualification.

Version 2.2.18.2602001 targets Forge65.1.0, MCP20260616.103818, Java25 and
native pack formats107.1/88.0. This candidate is local-only. The combined
checks below passed; the final root cold-cache gate is still pending.
Ready for manual testing once that gate is confirmed. Overall sign-off,
manual acceptance and publication are not claimed.

## Exact inputs

- Steam player/development SHA-256:
  EDD10D6ED4A623F6EAA364D21480FDA3B777FB15C2195D437882DB09C0F415AB.
- Power source f84b2362449e39eff5e76696dd64ce4cb144da4f; player/development
  7E456DE1A45140E86415A701D186FEDE08C4AB63D53C6B7848935DB70CBC68B1.
- Base Metals source 523335a59ecff399e40a7c66eb121a3aabdc45ba; player/development
  15CBD0C8DD95BBA846969929B8767887BA4B8152DFB10C9C58FB314AD39FB9E1.
- OreSpawn4.1.1.2602001 local retained-settings repair, source
  dcaaa933299dd8b1ebc73e31c9e5acd36dd7957c; player/development
  BC0658DF2E149CC23E65E5771F3FF62E2BB13FFDA4E316AB632EB92EF7FA5A1F.
  No public file ID is assigned to this local candidate.
- Power's checksum-pinned unit agent and committed support sources are used
  separately from player bytes. No helper or test classes ship in the mod.

## Passed child gates

- Clean check/build/Javadoc: 47 tasks / 276 unit tests, with no failures or skips.
- Exact source/artifact pins, release archives/checksums, GameTest source
  compilation and Eclipse production-classpath isolation.
- Fresh ECJ main/test/GameTest compilation and the same complete unit inventory,
  including strict inventory-rejection controls.
- Committed-source offline and fresh unseeded-cache builds with paths containing
  spaces reproduce all three release archives. Both regenerated Forge SDK jars
  also match. No rebuild was needed for the later docs-only ancestry merges.
- All 24 recipe-unlock checks. Static source audits remain separate from runtime proof.
- Native screen lookup uses Minecraft.gui.screen(); existing needle delegation
  and no-immediate-GPU contracts remain asserted.
- Failed setup, compiler and test attempts are preserved separately. The
  provisional old-Power/public-OreSpawn runs are not relabelled as this candidate.

## Passed combined checks

These results use frozen profile03, SHA-256
A98D4F9B54A0AE47392D9A3CF81335F5E228F718C7D322DF9838A94AD27D9736,
and basePower's combined-family rig:

- All eight machine campaigns, including moving, partial and bridge networks.
- All eight machine migration routes and the additional 26.1.2 route.
- Oil, resource and all three retained-generation routes.
- Nine actor/dimension routes. Six dimension routes retain raw wolf differences
  backed by independent native controls; no other semantic difference is waived.
- Both actual transformer orders for machines and actors, with the instrumented
  sidecars kept separate from ordinary production qualification. All 39 owned
  game JVMs closed; 117 strict closed logs were audited.
- Real client07: 39 GUIs, 351 text checks, 26 tank gauges and 1,247 frames.
- Two genuine worlds ran for 1,800 seconds each, passed restart and passed
  independent 66-checkpoint replay. The family verification root's
  `endurance-independent-replay-03.json` records `PASS`.

The [family qualification record](../../../PowerAdvantage/docs/26.2/QUALIFICATION.md)
is being finalized. Its remaining root cold-cache gate must be confirmed before
manual testing. Hydroturbine orientation/spin geometry in a fresh client world
remains an explicit manual gap. Historical records and failed receipts remain
unchanged and bound to their original inputs.

Hosted CI still requires merged, public dependency inputs; local candidate
pins do not waive that gate. Base Metals and OreSpawn 26.2 remain unpublished
local inputs. No 26.2 push, tag, publisher dispatch or upload.
