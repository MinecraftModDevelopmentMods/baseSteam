# Minecraft 1.21.1 NeoForge

baseSteam 2.2.20.121012 brings the Forge 1.21.1 implementation to NeoForge.
The native port preserves the existing machine balance and saved identities.

## Compatibility

- NeoForge 21.1.265, Java 21, ModDevGradle 2.0.148 and Gradle 9.8.0.
- basePower 2.4.19.121012 and MMDLib 2.0.2.121012+Neo are required.
- Steam's direct Base Metals integration is optional. The coordinated Power
  stack requires published Base Metals 4.0.1.121012+Neo and OreSpawn 4.1.1.121012.
  The default build resolves Base Metals from MMD Maven, not a local hook candidate.
- `basesteam` registry IDs, NBT keys, recipes, processing rates and drops are retained.
- Material integration uses `c:` tags and `neoforge:conditions`.
- Crusher and drill recipes use the shared Power lookup. Removed recipes have
  no runtime replacement. basePower owns the shared vanilla rock-processing data.

## Qualification

The native port passed manual testing. Automated requalification of the
published dependency stack completed on 15 November 2026 with the frozen inputs below:
231 JUnit cases (no failures or skips), all 10 required native GameTests, build,
Javadoc, dependency and artifact audits, resources, Eclipse classpath/sync, and
ECJ compilation of 60 production and 42 test sources with three isolation
controls. A forced offline Gradle rerun reproduced all three release jars.
The final public-pin suite used all four Maven jars downloaded by the strict
network guard through the hosted-input hook; all 62 offline pin guards passed.
The subsequent hosted checks passed, and all three Linux-built archives match
the local Windows builds. MMD PR #46 merged the port; the guarded publisher then
released those exact CI artifacts after basePower became public.

[CurseForge file 9626794](https://www.curseforge.com/minecraft/mc-mods/basesteam/files/9626794),
[the GitHub release](https://github.com/MinecraftModDevelopmentMods/baseSteam/releases/tag/2.2.20.121012)
and the MMD Maven jars were downloaded independently and matched the approved
hashes. Raw test worlds, logs and dependency jars remain local, outside Git.

The published basePower input is `basePower-2.4.19.121012.jar`, SHA-256
`E9ED662E15C3510E2A48CE85448D3D39C6EF2667208F29EB281791200507F77E`.
Power PR #51 merged as `c5550ce133ce0f5d3fe3e5227f021b390cdc7953`, with release
tag `2.4.19.121012`. Steam independently downloaded the public Maven player jar
without authentication and verified its hash, native metadata, merged ancestry
and exact tag-to-commit identity. The coordinating agent also verified public
CurseForge file `9626484` and the GitHub release; their publication receipts are
retained separately from Steam's own checks.
Published Base Metals 4.0.1.121012+Neo is from source
`39b240bb0d9f80997847ab4df70a2dd23e6ea826`, SHA-256
`B4BB1CBCD54DFCF2EC8465ACE82715EDCCC5BF1E5C15E14C627AE7CC58E8FED1`.
MMDLib 2.0.2.121012+Neo is pinned to SHA-256
`EE417C82D588B0050A49709B6430CF836315E71E4DB2DF4036D3735454BC5311`.
New campaign evidence is kept at
`D:/PowerAdvantage/_verification/base-1211neo-release-20261115/steam`;
earlier reports and failures are retained.

The frozen Steam player SHA-256 is
`AD4BACD5158252F4AE257DF694098E47E7C309B21BFD9F583AF6A18E569FABB9`.
The coordinating agent selected this jar for the combined family profile.
The coordinating agent also reported 132 finite machine jobs passing, conversion,
operation and cold restart passing for `original110`, `released110` and
`released112`, and 5 unlock plus 18 progression checks passing. The coordinating
agent subsequently reported the 30-minute endurance and restart checks passing
with all three frozen players unchanged.
Those shared results are reported separately from the locally executed Steam
checks above; no Steam production change was needed for them.
The final public-pin rerun preserved all three canonical archive hashes, so the
existing frozen-player gameplay/endurance evidence remains applicable without
another endurance run. CurseForge requirements include `mmdlib` alongside
`basepower` and `base-metals`.

## Hosted Delivery

CI, CodeQL, wrapper validation and release-tag validation target
`master-1.21.1-neo`. No Forge tool checkout, Mavenizer, Java 8 or Java 25 is used.
CI runs native unit/build/Javadoc/resource/package/Eclipse gates; GameTests,
ECJ and the shared packaged/client qualification remain local. CodeQL performs
a fresh, uncached native Java compilation.
Javadoc archives include the full notices from the pinned JDK rather than
Windows pointer stubs. Text entries, including `element-list`, use LF; the
artifact audit verifies the three notices and rejects remaining CR line endings.

`.github/native-dependencies.json` is `HOSTED_INPUTS_READY`, with all four inputs
marked `PUBLISHED`. Power's exact merged commit and release tag are pinned in
both the lock and project properties. The strict network guard downloaded all
four public Maven jars and verified the required source ancestry, Power tag
identity, SHA-256 values, NeoForge metadata and required APIs.

Offline negative controls still reject bootstrap status, uncommitted source,
wrong tags, mismatched tag commits, wrong branches, changed jars and missing APIs.
The hosted-input hook compiles exactly the verified files, not substitute local
candidates. Hosted Steam CI and Steam publication remain separate parent-owned
gates; this local qualification does not claim either has happened.

The CI candidate archive retains `build/libs/` for all three jars,
`build/release/SHA256SUMS`, and root `CHANGELOG.txt` for the default dispatcher.
The frozen player digest is recorded in `release_player_sha256`; tag validation
also requires successful push CI for that exact SHA on the native release branch.
Tag validation does not publish. Only the coordinating agent handles the guarded
dispatcher and dependency-order publication.

## Runtime Checks

Run source/resource checks, the full JUnit suite, artifact audits and Eclipse
classpath checks before GameTests and packaged dedicated-server checks. Keep
each failed attempt's logs and use a fresh run directory for another attempt.

The packaged-server script requires an installed NeoForge 21.1.265 server,
the exact candidate Power and Steam jars, MMDLib, and any optional companion
jars. It writes hashes, Git state, runtime details and the outcome to an ignored
evidence directory. Run the complete dependency stack for a positive startup
check. Absence of Base Metals is a loader-rejection check while Power requires
it, not a supported positive startup configuration. The script checks actual
server startup and a clean shutdown, not merely a successful launcher exit.

Historical-world migration is qualified by the coordinated Power test rig on
copies of protected fixtures. A source check or clean startup alone does not
establish world-upgrade safety. Manual client acceptance covers rendering,
machine interfaces, controls, elevators and musket effects; it is not inferred
from automated test results. The shared Power rig records the combined-stack
migration, operating and endurance checks separately from these isolated tests.
