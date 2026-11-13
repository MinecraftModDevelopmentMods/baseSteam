# Minecraft 26.3 Published Release

baseSteam 2.2.19.2603001 was published on 13 November 2026. Its public
downloads were independently checked against the sealed release artifacts.
This follow-up changes documentation only, not the jars, version or tag.

## Downloads

- [CurseForge file 9589874](https://www.curseforge.com/minecraft/mc-mods/basesteam/files/9589874)
- [MMD Maven player jar](https://maven.moddev.zone/releases/zone/moddev/mc/basesteam/baseSteam/2.2.19.2603001/baseSteam-2.2.19.2603001.jar)
- [GitHub release, sources and Javadoc](https://github.com/MinecraftModDevelopmentMods/baseSteam/releases/tag/2.2.19.2603001)

Published source: [5e50614d4f5d7c162ca3394f3530c0b3201f5254](https://github.com/MinecraftModDevelopmentMods/baseSteam/commit/5e50614d4f5d7c162ca3394f3530c0b3201f5254).
Tag: `2.2.19.2603001`.

Player/development JAR SHA-256:
`770E46CF87FEE3CC1EFF73C666858BFC03D6922E866B4C07C0A093B746B2CF44`.
Forge 66 uses official names and the same jar for play and development.

Sources JAR SHA-256:
`120E5088E7E3A6CCDF5108D9CC6FD86697350A00E1575DCDDE1FCF4B006BAAAA`.

Published CI Javadoc SHA-256:
`5ADE8721F79BE4DF4AC52251EFA7E4674F5202C7C1B23D42402F04B572B1D37A`.
Its only difference from the Windows candidate is CRLF versus LF in four
bundled JDK SVG icons, independently checked and approved. No runtime, API
or documentation content changed.

## Dependencies And Qualification

Use published [basePower 2.4.18.2603001](https://www.curseforge.com/minecraft/mc-mods/basepower/files/9589309),
[Base Metals 3.1.2.2603001](https://www.curseforge.com/minecraft/mc-mods/base-metals/files/9589195)
and [OreSpawn 4.1.1.2603001](https://www.curseforge.com/minecraft/mc-mods/orespawn/files/9589145).
The public Power API source remains `4fae0a51feb3e49875f09c500b869266122a711a`;
later Power documentation commits do not replace that release pin. Unit
support remains independently sealed at `c0eb83f7` and its original hashes.

The player and all three dependency jars are wholly byte-identical to the
manually accepted profile04 family. Reuse of its machine, recipe, migration,
client, restart and two-world endurance evidence was explicitly authorized.
The post-public-pin rebuild passed all 45 local tasks and 291 Steam units
without failures or skips. Hosted full unfiltered build/audit, wrapper and
CodeQL checks also passed. Successful CI retains the release jars, checksums
and changelog; the exact local XML inventory is a separate retained proof.

The [candidate history](PORT263-STATUS.md) and
[family qualification record](https://github.com/MinecraftModDevelopmentMods/basePower/blob/master-26.3/docs/26.3/QUALIFICATION.md)
keep their original inputs, failures and scope limits. Forge 66.1.5 has
bounded server evidence, not the full 66.0.0 client/endurance suite on a
second Forge version. Keep backups and test disposable world copies.

## Publication

The [merged-source CI run](https://github.com/MinecraftModDevelopmentMods/baseSteam/actions/runs/43416653415)
sealed artifact `14032147042`. Its ZIP SHA-256 is
`017b39dce10434c8ac9879f5f95021eb281da6df8bc63c8bbfb490d2a357615d`.
Source PR #41 and publisher PRs #42/#43 passed their normal checks; the
exact source and publisher CodeQL analyses had no errors or open high/critical
alerts. Tag validation also passed.

One CF upload was accepted as 9589874. The first publisher stopped while
that file was still Processing. After public approval and an exact anonymous
download check, [the successful resume](https://github.com/MinecraftModDevelopmentMods/baseSteam/actions/runs/43426019887)
used the same file ID and skipped upload. No duplicate file was submitted.

Independent verification checked seven anonymous jar downloads, Maven POM
coordinates and its version index, and the exact public tag. CF, Maven and
GitHub player hashes match; Maven/GitHub sources and Javadoc match sealed CI.
The evidence receipt is `steam/public-verification-01/verified.json` beneath
the retained `base-263-publication-20261113` verification directory, SHA-256
`EA1438D10AEC2F804C4202B0A071B1E44238E3BA6056334843323AD7777D78F1`.
