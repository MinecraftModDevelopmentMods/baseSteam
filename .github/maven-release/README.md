# MMD Maven releases

## New Forge releases

Use **Release baseSteam** on the MMD repository's default branch. After the
version branch passes CI, tag the exact tested commit and add the CI run, artifact
ID, archive SHA-256 and tested main-jar SHA-256 to `releases.json`. Run
**check-only** first, then **publish**. The workflow uploads the checked player
jar to CurseForge, verifies the public download, and publishes those same bytes
to MMD Maven before creating the GitHub release.

If CurseForge has accepted the file but it isn't public yet, wait for approval.
Start a new run with the recorded CurseForge file ID so it verifies that upload
without submitting the jar again. The release workflow sets Java 8 through
Minecraft 1.16.5, Java 16 for Minecraft 1.17.1, and Java 17 for Minecraft 1.18.2
through 1.20.1, Java 21 for Minecraft 1.20.6, 1.21.1 and 1.21.11, and Java 25
for Minecraft 26.1.2, 26.2 and 26.3. The pure target helper in verify.py uses an
explicit whitelist; unqualified or malformed targets, including 26.4 and
26.3.0, are rejected.

The dispatcher runs its offline tests before staging an artifact. Release
records still require the actual merged source, successful version-branch CI
run, artifact ID and checksums. A prepared route is not an approved release
record and does not publish anything.

The 26.3 route is prepared locally after manual acceptance of the exact
candidate. It adds no approved 26.3 release entry. Wait for verified public
Power and companion pins, the exact upstream merge, every required check
including CodeQL, the matching tag and sealed CI artifact before recording
the release. Run check-only first; live publication also requires the
coordinator's explicit Steam gate. Resume an accepted CurseForge file ID.

The GitHub release targets the exact version-branch source recorded in
`releases.json`, not the publication workflow's default branch.

Before publishing, check that basePower and Base Metals have public files
before publishing. Use the release's required dependency relations on CurseForge.

## Existing CurseForge releases

Use **Actions > Publish to MMD Maven** on the MMD repository's default branch.
Select a version listed in `releases.json` and run **check-only** first. Once it
passes, run **publish** for that same version.

This workflow publishes the exact main, sources and Javadoc jars retained by the
successful CI run, plus a POM. It checks the archive and jar checksums and
compares the main jar with the existing CurseForge download. It does not build
the mod, move tags, change versions or upload to CurseForge. An identical Maven
release is a no-op; different bytes or a partial upload need investigation.

To add a release, record its CurseForge file ID, CI run, source commit, artifact
ID and archive checksum in `releases.json` after checking the public download.
Include its tag when one exists. The CI archive must still be available.
