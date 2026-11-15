[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$NeoForgeInstall,
    [Parameter(Mandatory)][ValidatePattern('^[a-zA-Z0-9_-]+$')][string]$RunId,
    [Parameter(Mandatory)][string]$MmdLibJar,
    [string]$JavaExe = "$env:USERPROFILE/.jdks/temurin-21.0.7+6/bin/java.exe",
    [string]$PowerJar,
    [string]$BaseMetalsJar,
    [string[]]$ExtraModJars = @(),
    [switch]$WithoutBaseMetals
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$install = (Resolve-Path -LiteralPath $NeoForgeInstall).Path
$pins = Get-Content -Raw -LiteralPath (Join-Path $project 'gradle.properties') | ConvertFrom-StringData
if ($pins.minecraft_version -ne '1.21.1' -or $pins.neo_version -ne '21.1.265') {
    throw 'Use the coordinated Minecraft 1.21.1 / NeoForge 21.1.265 candidate.'
}
if (!$WithoutBaseMetals -and !$BaseMetalsJar) { throw 'Supply BaseMetalsJar, or select WithoutBaseMetals.' }
if ($WithoutBaseMetals -and $BaseMetalsJar) { throw 'WithoutBaseMetals cannot also load BaseMetalsJar.' }
$java = (Resolve-Path -LiteralPath $JavaExe).Path
$javaVersion = (& $java -version 2>&1 | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $javaVersion -notmatch 'Temurin-21\.0\.7\+6') { throw "Unexpected JVM: $javaVersion" }
$steamName = "baseSteam-$($pins.mod_version).jar"
$checksums = Get-Content -LiteralPath (Join-Path $project 'build/release/SHA256SUMS')
$checksum = @($checksums | Where-Object { $_.EndsWith("  $steamName") })
if ($checksum.Count -ne 1 -or $checksum[0] -cnotmatch '^[A-F0-9]{64}  ') { throw 'Run writeReleaseChecksums first.' }
if (!$PowerJar) { $PowerJar = Join-Path $project "../../PowerAdvantage/build/libs/basePower-$($pins.poweradvantage_version).jar" }
$inputs = @((Join-Path $project "build/libs/$steamName"), $PowerJar, $MmdLibJar)
if (!$WithoutBaseMetals) { $inputs += $BaseMetalsJar }
$inputs += $ExtraModJars
$mods = [ordered]@{}
foreach ($inputJar in $inputs) {
    $resolved = (Resolve-Path -LiteralPath $inputJar).Path
    $name = [IO.Path]::GetFileName($resolved)
    if ($mods.Contains($name)) { throw "Duplicate mod filename: $name" }
    $mods[$name] = @{ Path = $resolved; Sha256 = (Get-FileHash -LiteralPath $resolved).Hash }
}
if ($mods[$steamName].Sha256 -cne $checksum[0].Substring(0, 64)) { throw 'Steam candidate checksum mismatch.' }
$argumentPath = "libraries/net/neoforged/neoforge/$($pins.neo_version)/win_args.txt"
$argumentsFile = (Resolve-Path -LiteralPath (Join-Path $install $argumentPath)).Path
$run = Join-Path $project "evidence/packaged-server-$RunId"
if (Test-Path -LiteralPath $run) { throw "Preserve the existing run and choose another RunId: $run" }
New-Item -ItemType Directory -Path (Join-Path $run 'mods') | Out-Null
$receipt = [ordered]@{
    status = 'FAILED'
    steamCommit = (& git -C $project rev-parse HEAD)
    steamStatus = @(& git -C $project status --porcelain)
    neoforge = $pins.neo_version
    java = $javaVersion
    argumentsFile = @{ Path = $argumentsFile; Sha256 = (Get-FileHash -LiteralPath $argumentsFile).Hash }
    mods = $mods
    withoutBaseMetals = [bool]$WithoutBaseMetals
    finishedUtc = $null
}
$process = $null
try {
    Copy-Item -LiteralPath (Join-Path $install 'libraries') -Destination $run -Recurse
    foreach ($name in $mods.Keys) {
        $destination = Join-Path $run "mods/$name"
        Copy-Item -LiteralPath $mods[$name].Path -Destination $destination
        if ((Get-FileHash -LiteralPath $destination).Hash -cne $mods[$name].Sha256) { throw "Jar changed during copy: $name" }
    }
    Set-Content -LiteralPath (Join-Path $run 'eula.txt') -Value 'eula=true' -Encoding ascii
    Set-Content -LiteralPath (Join-Path $run 'server.properties') -Encoding ascii -Value @(
        'server-ip=127.0.0.1', 'server-port=0', 'level-name=steam-smoke',
        'level-type=minecraft:flat', 'generate-structures=false', 'view-distance=2', 'simulation-distance=2',
        'generator-settings={"layers":[{"height":1,"block":"minecraft:bedrock"},{"height":2,"block":"minecraft:dirt"},{"height":1,"block":"minecraft:grass_block"}],"biome":"minecraft:plains"}'
    )
    $start = [Diagnostics.ProcessStartInfo]::new($java)
    $start.WorkingDirectory = $run
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardInput = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    foreach ($argument in @('-Xms512m', '-Xmx3g', "@$argumentPath", '--nogui')) { $start.ArgumentList.Add($argument) }
    $process = [Diagnostics.Process]::Start($start)
    $stdout = $process.StandardOutput.ReadToEndAsync()
    $stderr = $process.StandardError.ReadToEndAsync()
    $latest = Join-Path $run 'logs/latest.log'
    $deadline = [DateTime]::UtcNow.AddMinutes(3)
    do {
        if ($process.HasExited) { throw "NeoForge exited before ready: $($process.ExitCode)" }
        $log = if (Test-Path -LiteralPath $latest) { Get-Content -Raw -LiteralPath $latest }
        if ($log -match 'Done \([^)]+\)!') { break }
        Start-Sleep -Seconds 1
    } while ([DateTime]::UtcNow -lt $deadline)
    if ($log -notmatch 'Done \([^)]+\)!') { throw 'Timed out waiting for a fresh NeoForge world.' }
    $process.StandardInput.WriteLine('stop')
    if (!$process.WaitForExit(60000) -or $process.ExitCode -ne 0) { throw 'NeoForge did not stop cleanly.' }
    $log = Get-Content -Raw -LiteralPath $latest
    if ($log -notmatch "baseSteam $([regex]::Escape($pins.mod_version)) common setup complete" -or
            $log -match '\[[^\]\r\n]*/(?:ERROR|FATAL)\]|NoClassDefFoundError') { throw 'Missing Steam setup or server errors appeared.' }
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
    $receipt | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $run 'receipt.json')
}
