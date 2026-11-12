# Minecraft 26.2 local build candidate

Version 2.2.18.2602001 targets Forge65.1.0, MCP20260616.103818, Java25 and
native pack formats107.1/88.0. This candidate is local-only. Child build gates
do not establish combined-family runtime qualification or manual acceptance.

## Exact inputs

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
- All 24 recipe-unlock checks. Static source audits remain separate from runtime proof.
- Native screen lookup uses Minecraft.gui.screen(); existing needle delegation
  and no-immediate-GPU contracts remain asserted.
- Failed setup, compiler and test attempts are preserved separately. The
  provisional old-Power/public-OreSpawn runs are not relabelled as this candidate.

## Remaining qualification

Independent archive reproductions and fresh-cache/offline gates remain pending.
Packaged combined-family gameplay, machine/migration/actor/dimension/order,
retained-generation and GPU/client campaigns are parent-owned and are not
claimed here. Manual acceptance is also pending. Historical 26.1.2 evidence
remains bound to its original source, profile and artifact bytes.

Hosted CI still requires merged, public dependency inputs; local candidate
pins do not waive that gate. No 26.2 push, tag, publisher dispatch or upload.
