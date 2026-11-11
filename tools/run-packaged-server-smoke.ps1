[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$ForgeInstall,
    [Parameter(Mandatory)][ValidatePattern('^[a-zA-Z0-9_-]+$')][string]$RunId,
    [string]$JavaExe = "$env:USERPROFILE/.jdks/temurin-21.0.7+6/bin/java.exe",
    [string]$PowerJar,
    [string]$BaseMetalsJar,
    [string]$OreSpawnJar,
    [switch]$WithoutBaseMetals,
    [switch]$UseNioTransport,
    [switch]$Reopen
)

$ErrorActionPreference = 'Stop'
if ($WithoutBaseMetals) {
    throw 'Base Metals is required by baseSteam; a no-Base-Metals runtime is not supported.'
}
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$forge = (Resolve-Path -LiteralPath $ForgeInstall).Path
$pins = Get-Content -Raw -LiteralPath (Join-Path $project 'gradle.properties') | ConvertFrom-StringData
if ($pins.minecraft_version -ne '1.21.11' -or $pins.forge_version -ne '61.1.0' -or
        $pins.poweradvantage_source_ref -cnotmatch '^[0-9a-f]{40}$') {
    throw 'An exact Forge 61.1.0 candidate and committed Power source pin are required.'
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
    $BaseMetalsJar = Join-Path $project "../../../../BaseMetals/.verification-basemetals12111/Advantage shared hook/build/libs/BaseMetals-$($pins.basemetals_version).jar"
}
if (!$OreSpawnJar) {
    $OreSpawnJar = Join-Path $project "../../../../MinecraftMineralogy/OS12111/OreSpawn/build/libs/OreSpawn-$($pins.orespawn_version).jar"
}
$mods = [ordered]@{
    $steamName = @{ Path = (Join-Path $project "build/libs/$steamName"); Sha256 = $steamSha }
    "basePower-$($pins.poweradvantage_version).jar" = @{ Path = $PowerJar; Sha256 = $pins.poweradvantage_player_sha256 }
    "OreSpawn-$($pins.orespawn_version).jar" = @{ Path = $OreSpawnJar; Sha256 = $pins.orespawn_player_sha256 }
}
$mods["BaseMetals-$($pins.basemetals_version).jar"] = @{ Path = $BaseMetalsJar; Sha256 = $pins.basemetals_player_sha256 }
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
        throw "Supply the coordinated Forge 61.1.0 runtime hash in gradle.properties: $file"
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
    transport = $(if ($UseNioTransport) { 'NIO' } else { 'native-availability-selection' })
    processes = @()
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
        "use-native-transport=$((!$UseNioTransport).ToString().ToLowerInvariant())",
        'level-type=minecraft:flat', 'generate-structures=false', 'view-distance=2', 'simulation-distance=2',
        'generator-settings={"layers":[{"height":1,"block":"minecraft:bedrock"},{"height":2,"block":"minecraft:dirt"},{"height":1,"block":"minecraft:grass_block"}],"biome":"minecraft:plains"}'
    )
    foreach ($phase in $(if ($Reopen) { @('startup', 'reopen') } else { @('startup') })) {
        foreach ($name in $mods.Keys) {
            if ((Get-FileHash -LiteralPath (Join-Path $run "mods/$name")).Hash -cne $mods[$name].Sha256) {
                throw "Runtime jar changed before $phase : $name"
            }
        }
        $phaseReceipt = [ordered]@{ phase = $phase; status = 'FAILED'; pid = $null; exitCode = $null; levelDatSha256 = $null }
        $receipt.processes += $phaseReceipt
        $process = $null
        try {
            $latest = Join-Path $run 'logs/latest.log'
            if ($phase -eq 'reopen' -and (Test-Path -LiteralPath $latest)) {
                Move-Item -LiteralPath $latest -Destination (Join-Path $run 'startup-original-latest.log')
            }
            $start = [System.Diagnostics.ProcessStartInfo]::new($java)
            $start.WorkingDirectory = $run
            $start.UseShellExecute = $false
            $start.CreateNoWindow = $true
            $start.RedirectStandardInput = $true
            $start.RedirectStandardOutput = $true
            $start.RedirectStandardError = $true
            foreach ($argument in @('-Xms512m', '-Xmx3g', '-XX:ActiveProcessorCount=4', '-Djava.net.preferIPv6Addresses=system', '-jar', $shim, '--nogui')) {
                $start.ArgumentList.Add($argument)
            }
            $process = [System.Diagnostics.Process]::Start($start)
            $phaseReceipt.pid = $process.Id
            $stdout = $process.StandardOutput.ReadToEndAsync()
            $stderr = $process.StandardError.ReadToEndAsync()
            $latest = Join-Path $run 'logs/latest.log'
            $deadline = [DateTime]::UtcNow.AddMinutes(3)
            $log = ''
            do {
                if ($process.HasExited) { throw "Forge exited before $phase ready: $($process.ExitCode)" }
                if (Test-Path -LiteralPath $latest) { $log = Get-Content -Raw -LiteralPath $latest }
                if ($log -match 'Done \([^)]+\)!') { break }
                Start-Sleep -Seconds 1
            } while ([DateTime]::UtcNow -lt $deadline)
            if ($log -notmatch 'Done \([^)]+\)!') { throw "Timed out waiting for Forge $phase." }
            $process.StandardInput.WriteLine('stop')
            if (!$process.WaitForExit(60000) -or $process.ExitCode -ne 0) { throw "Forge $phase did not stop cleanly." }
            $phaseReceipt.exitCode = $process.ExitCode
            $log = Get-Content -Raw -LiteralPath $latest
            Copy-Item -LiteralPath $latest -Destination (Join-Path $run "$phase-latest.log")
            $allLogs = $log + $stdout.GetAwaiter().GetResult() + $stderr.GetAwaiter().GetResult()
            if ($log -notmatch "baseSteam $([regex]::Escape($pins.mod_version)) common setup complete" -or
                    $allLogs -match '\[[^\]\r\n]*/(?:ERROR|FATAL)\]|NoClassDefFoundError|Exception in thread') {
                throw "Missing Steam setup or a loader/data error appeared during $phase."
            }
            $levelDat = Join-Path $run 'steam-smoke/level.dat'
            if (!(Test-Path -LiteralPath $levelDat) -or (Get-Item -LiteralPath $levelDat).Length -eq 0) {
                throw "Forge $phase did not save its world."
            }
            $phaseReceipt.levelDatSha256 = (Get-FileHash -LiteralPath $levelDat -Algorithm SHA256).Hash
            $phaseReceipt.status = 'START_SAVE_STOP_PASS'
        } finally {
            if ($process) {
                if (!$process.HasExited) { $process.Kill($true); $process.WaitForExit() }
                $suffix = if ($phase -eq 'startup') { '' } else { "-$phase" }
                Set-Content -LiteralPath (Join-Path $run "stdout$suffix.log") -Value $stdout.GetAwaiter().GetResult()
                Set-Content -LiteralPath (Join-Path $run "stderr$suffix.log") -Value $stderr.GetAwaiter().GetResult()
                $phaseReceipt.exitCode = $process.ExitCode
                $process.Dispose()
                $process = $null
            }
        }
    }
    $receipt.status = if ($Reopen) { 'PACKAGED_DEDICATED_SERVER_REOPEN_PASS' } else { 'PACKAGED_DEDICATED_SERVER_PASS' }
    Write-Output "$($receipt.status): $run"
} finally {
    $receipt.finishedUtc = [DateTime]::UtcNow.ToString('o')
    $receipt | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $run 'receipt.json')
}
