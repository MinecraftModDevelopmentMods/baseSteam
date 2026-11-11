[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$ForgeInstall,
    [Parameter(Mandatory)][ValidatePattern('^[a-zA-Z0-9_-]+$')][string]$RunId,
    [string]$JavaExe = "$env:USERPROFILE/.jdks/temurin-21.0.7+6/bin/java.exe",
    [string]$PowerJar,
    [string]$BaseMetalsJar,
    [string]$OreSpawnJar,
    [switch]$WithoutBaseMetals
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$forge = (Resolve-Path -LiteralPath $ForgeInstall).Path
$pins = Get-Content -Raw -LiteralPath (Join-Path $project 'gradle.properties') | ConvertFrom-StringData
if ($pins.minecraft_version -ne '1.21.1' -or $pins.forge_version -ne '52.1.0' -or
        $pins.poweradvantage_source_ref -cnotmatch '^[0-9a-f]{40}$') {
    throw 'An exact Forge 52.1.0 candidate and committed Power source pin are required.'
}
$java = (Resolve-Path -LiteralPath $JavaExe).Path
$javaVersion = (& $java -version 2>&1 | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $javaVersion -notmatch 'Temurin-21\.0\.7\+6') {
    throw "Unexpected test JVM: $javaVersion"
}

$steamName = "baseSteam-$($pins.mod_version).jar"
$checksums = Join-Path $project 'build/release/SHA256SUMS'
$checksum = Get-Content -LiteralPath $checksums | Where-Object { $_.EndsWith("  $steamName") }
if (@($checksum).Count -ne 1 -or $checksum -cnotmatch '^[A-F0-9]{64}  ') {
    throw 'Run writeReleaseChecksums for the audited Steam candidate first.'
}
$steamSha = $checksum.Substring(0, 64)
if (!$PowerJar) {
    $PowerJar = Join-Path $project "../../PowerAdvantage/build/libs/basePower-$($pins.poweradvantage_version).jar"
}
if (!$BaseMetalsJar) {
    $BaseMetalsJar = Join-Path $project "../../../../BaseMetals/.verification-basemetals1211/Advantage shared hook/build/libs/BaseMetals-$($pins.basemetals_version).jar"
}
if (!$OreSpawnJar) {
    $OreSpawnJar = Join-Path $project "../../../../MinecraftMineralogy/OS1211/OreSpawn/build/libs/OreSpawn-$($pins.orespawn_version).jar"
}
$mods = [ordered]@{
    $steamName = @{ Path = (Join-Path $project "build/libs/$steamName"); Sha256 = $steamSha }
    "basePower-$($pins.poweradvantage_version).jar" = @{ Path = $PowerJar; Sha256 = $pins.poweradvantage_player_sha256 }
    "OreSpawn-$($pins.orespawn_version).jar" = @{ Path = $OreSpawnJar; Sha256 = $pins.orespawn_player_sha256 }
}
if (!$WithoutBaseMetals) {
    $mods["BaseMetals-$($pins.basemetals_version).jar"] = @{ Path = $BaseMetalsJar; Sha256 = $pins.basemetals_player_sha256 }
}
$forgeVersion = "$($pins.minecraft_version)-$($pins.forge_version)"
$shim = "forge-$forgeVersion-shim.jar"
$forgeLib = "libraries/net/minecraftforge/forge/$forgeVersion"
$forgeJars = [ordered]@{
    $shim = $pins.forge_server_shim_sha256
    "$forgeLib/forge-$forgeVersion-server.jar" = $pins.forge_server_sha256
    "$forgeLib/forge-$forgeVersion-universal.jar" = $pins.forge_universal_sha256
}
foreach ($file in $forgeJars.Keys) {
    if ($forgeJars[$file] -cnotmatch '^[A-F0-9]{64}$') {
        throw "Supply the coordinated Forge 52.1.0 runtime hash in gradle.properties: $file"
    }
    if ((Get-FileHash -LiteralPath (Join-Path $forge $file)).Hash -cne $forgeJars[$file]) {
        throw "Installed Forge runtime hash mismatch: $file"
    }
}
foreach ($name in $mods.Keys) {
    $inputJar = $mods[$name]
    $inputJar.Path = (Resolve-Path -LiteralPath $inputJar.Path).Path
    if ((Get-FileHash -LiteralPath $inputJar.Path).Hash -cne $inputJar.Sha256) {
        throw "Pinned player hash mismatch: $name"
    }
}

$run = Join-Path $project "evidence/packaged-server-$RunId"
if (Test-Path -LiteralPath $run) { throw "Run directory already exists: $run" }
New-Item -ItemType Directory -Path (Join-Path $run 'mods') | Out-Null
$receipt = [ordered]@{
    status = 'FAILED'
    steamCommit = (& git -C $project rev-parse HEAD)
    steamStatus = @(& git -C $project status --porcelain)
    powerCommit = $pins.poweradvantage_source_ref
    forge = $forgeVersion
    forgeJars = $forgeJars
    java = $javaVersion
    mods = $mods
    world = (Join-Path $run 'steam-smoke')
    finishedUtc = $null
}
$process = $null
try {
    Copy-Item -LiteralPath (Join-Path $forge 'libraries') -Destination $run -Recurse
    Copy-Item -LiteralPath (Join-Path $forge $shim) -Destination $run
    foreach ($name in $mods.Keys) {
        $destination = Join-Path $run "mods/$name"
        Copy-Item -LiteralPath $mods[$name].Path -Destination $destination
        if ((Get-FileHash -LiteralPath $destination).Hash -cne $mods[$name].Sha256) {
            throw "Player jar changed while copying: $name"
        }
    }
    Set-Content -LiteralPath (Join-Path $run 'eula.txt') -Value 'eula=true' -Encoding ascii
    Set-Content -LiteralPath (Join-Path $run 'server.properties') -Encoding ascii -Value @(
        'server-ip=127.0.0.1', 'server-port=0', 'level-name=steam-smoke',
        'level-type=minecraft:flat', 'generate-structures=false', 'view-distance=2', 'simulation-distance=2',
        'generator-settings={"layers":[{"height":1,"block":"minecraft:bedrock"},{"height":2,"block":"minecraft:dirt"},{"height":1,"block":"minecraft:grass_block"}],"biome":"minecraft:plains"}'
    )
    $start = [System.Diagnostics.ProcessStartInfo]::new($java)
    $start.WorkingDirectory = $run
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardInput = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    foreach ($argument in @('-Xms512m', '-Xmx3g', '-Djava.net.preferIPv6Addresses=system', '-jar', $shim, '--nogui')) {
        $start.ArgumentList.Add($argument)
    }
    $process = [System.Diagnostics.Process]::Start($start)
    $stdout = $process.StandardOutput.ReadToEndAsync()
    $stderr = $process.StandardError.ReadToEndAsync()
    $latest = Join-Path $run 'logs/latest.log'
    $deadline = [DateTime]::UtcNow.AddMinutes(3)
    do {
        if ($process.HasExited) { throw "Forge exited before ready: $($process.ExitCode)" }
        $log = if (Test-Path -LiteralPath $latest) { Get-Content -Raw -LiteralPath $latest }
        if ($log -match 'Done \([^)]+\)!') { break }
        Start-Sleep -Seconds 1
    } while ([DateTime]::UtcNow -lt $deadline)
    if ($log -notmatch 'Done \([^)]+\)!') { throw 'Timed out waiting for a fresh Forge world.' }
    $process.StandardInput.WriteLine('stop')
    if (!$process.WaitForExit(60000) -or $process.ExitCode -ne 0) { throw 'Forge did not stop cleanly.' }
    $log = Get-Content -Raw -LiteralPath $latest
    if ($log -notmatch "baseSteam $([regex]::Escape($pins.mod_version)) common setup complete" -or
            $log -match '\[[^\]\r\n]*/(?:ERROR|FATAL)\]|NoClassDefFoundError') {
        throw 'Missing Steam setup or a relevant loader/data error appeared in the server log.'
    }
    $receipt.status = 'PACKAGED_DEDICATED_SERVER_PASS'
    Write-Output "$($receipt.status): $run"
} finally {
    if ($process) {
        if (!$process.HasExited) { $process.Kill($true); $process.WaitForExit() }
        Set-Content -LiteralPath (Join-Path $run 'stdout.log') -Value $stdout.GetAwaiter().GetResult()
        Set-Content -LiteralPath (Join-Path $run 'stderr.log') -Value $stderr.GetAwaiter().GetResult()
        $process.Dispose()
    }
    $receipt.finishedUtc = [DateTime]::UtcNow.ToString('o')
    $receipt | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $run 'receipt.json')
}
