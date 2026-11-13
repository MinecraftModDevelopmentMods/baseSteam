# Minecraft 26.2 Published Release

baseSteam 2.2.18.2602001 was published on 13 November 2026. Its public
downloads were checked against the release build; this documentation update
does not rebuild or republish it.

## Downloads

- [CurseForge file 9584145](https://www.curseforge.com/minecraft/mc-mods/basesteam/files/9584145)
- [MMD Maven player jar](https://maven.moddev.zone/releases/zone/moddev/mc/basesteam/baseSteam/2.2.18.2602001/baseSteam-2.2.18.2602001.jar)
- [GitHub release, sources and Javadoc](https://github.com/MinecraftModDevelopmentMods/baseSteam/releases/tag/2.2.18.2602001)

Published source: [9b75f6b7053ccdfdedc21a02ea4c99bb0bde44c2](https://github.com/MinecraftModDevelopmentMods/baseSteam/commit/9b75f6b7053ccdfdedc21a02ea4c99bb0bde44c2).
Tag: `2.2.18.2602001`.

Player SHA-256:
`EDD10D6ED4A623F6EAA364D21480FDA3B777FB15C2195D437882DB09C0F415AB`.
Forge 65 uses the same jar for play and development.

## Dependencies And Evidence

Use published [basePower 2.4.17.2602001](https://www.curseforge.com/minecraft/mc-mods/basepower/files/9583433),
[Base Metals 3.1.2.2602001](https://www.curseforge.com/minecraft/mc-mods/base-metals/files/9583122)
and [OreSpawn 4.1.1.2602001](https://www.curseforge.com/minecraft/mc-mods/orespawn/files/9582731).
These include the shared migration-hook and retained-settings repairs.

The [12 November candidate record](PORT262-STATUS.md) keeps its original
inputs, hashes, failures and gate wording. The published Steam player hash
matches that candidate; the released source commit and public files are
recorded here rather than rewriting the old evidence. The
[family candidate record](https://github.com/MinecraftModDevelopmentMods/basePower/blob/master-26.2/docs/26.2/QUALIFICATION.md)
also keeps its original Electric A7D12A66... input, not Electric's later
published 2928E1C6... jar.

Manual acceptance was recorded on 12 November 2026. This follow-up checks
publication links and documentation hygiene only; it adds no gameplay result.
Keep backups and test disposable world copies, especially with other mods.
