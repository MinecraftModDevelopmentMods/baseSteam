param([string]$DownloadDirectory, [switch]$ValidateOnly)
$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
Import-Module (Join-Path $PSScriptRoot 'NativeDependencyPins.psm1') -Force
$properties = Read-NativeProperties (Join-Path $project 'gradle.properties')
$lock = Read-NativeDependencyPins (Join-Path $PSScriptRoot 'native-dependencies.json') $properties
if ($ValidateOnly) {
    Write-Output 'Hosted pin structure is valid; remote source and jar checks NOT RUN.'
    return
}
if (!$DownloadDirectory) { throw 'Supply a dedicated -DownloadDirectory for the four pinned public jars' }
$directory = [IO.Path]::GetFullPath($DownloadDirectory)
if (Test-Path -LiteralPath $directory) { throw 'Use a new native dependency download directory' }
New-Item -ItemType Directory -Path $directory | Out-Null
$records = @()
foreach ($pin in $lock.dependencies) {
    Assert-NativeSourceAncestry $pin
    Assert-NativeSourceTag $pin
    $segments = @($pin.group.Split('.')) + @($pin.artifact, $pin.mavenVersion, "$($pin.artifact)-$($pin.mavenVersion).jar")
    $url = 'https://maven.moddev.zone/releases/' + (($segments | ForEach-Object { [Uri]::EscapeDataString($_) }) -join '/')
    $path = Join-Path $directory "$($pin.artifact)-$($pin.mavenVersion).jar"
    Invoke-WebRequest -Uri $url -OutFile $path -MaximumRetryCount 3
    Assert-NativeDependencyArchive $path $pin
    $records += [ordered]@{ id=$pin.id; version=$pin.modVersion; path=$path; sha256=$pin.sha256; url=$url
        source=$(if ($pin.ContainsKey('source')) { $pin.source } else { $null }) }
    if ($env:GITHUB_ENV) {
        $name = @{ basepower='POWER_JAR'; basemetals='BASE_METALS_JAR'; mmdlib='MMDLIB_JAR'; orespawn='ORESPAWN_JAR' }[$pin.id]
        "$name=$path" | Add-Content -LiteralPath $env:GITHUB_ENV -Encoding utf8
    }
}
$records | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $directory 'verified-inputs.json') -Encoding utf8
if ($env:GITHUB_ENV) { "CHILD_VERSION=$($properties.mod_version)" | Add-Content -LiteralPath $env:GITHUB_ENV -Encoding utf8 }
Write-Output 'Verified all four native dependency jars and required source ancestry. This is not Steam gameplay or release approval.'
