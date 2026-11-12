[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidatePattern('^[a-zA-Z0-9_-]+$')][string]$RunId,
    [string]$JavaExe = "$env:USERPROFILE/.jdks/temurin-21.0.7+6/bin/java.exe",
    [string]$CompilerJar = "$env:USERPROFILE/.p2/pool/plugins/org.eclipse.jdt.core.compiler.batch_3.46.0.v20260528-0407.jar"
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$java = (Resolve-Path -LiteralPath $JavaExe).Path
$compiler = (Resolve-Path -LiteralPath $CompilerJar).Path
$compilerHash = (Get-FileHash -LiteralPath $compiler -Algorithm SHA256).Hash
if ($compilerHash -cne 'D0D43F8E2D7003E5EFED612E2CBB5F01870043397D8F1BBE536FD9128F4FCBF7') {
    throw 'The installed Eclipse ECJ 3.46.0 compiler does not match the qualified tool.'
}
$version = (& $java -version 2>&1 | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $version -notmatch 'Temurin-21\.0\.7\+6') { throw "Unexpected JVM: $version" }
$pins = Get-Content -Raw -LiteralPath (Join-Path $project 'gradle.properties') | ConvertFrom-StringData
[xml]$classpath = Get-Content -Raw -LiteralPath (Join-Path $project '.classpath')
$entries = @($classpath.classpath.classpathentry)
$run = Join-Path $project "build/ecj-$RunId"
if (Test-Path -LiteralPath $run) { throw "Evidence already exists: $run" }
New-Item -ItemType Directory -Path $run | Out-Null
$receipt = [ordered]@{
    status = 'FAILED'
    commit = (& git -C $project rev-parse HEAD)
    java = $version
    compiler = @{ path = $compiler; sha256 = $compilerHash }
    classpathSha256 = (Get-FileHash -LiteralPath (Join-Path $project '.classpath') -Algorithm SHA256).Hash
    powerSource = $pins.poweradvantage_source_ref
    scopes = [ordered]@{}
    finishedUtc = $null
}
function Attribute($entry, [string]$name) {
    @($entry.attributes.attribute | Where-Object { $_.name -eq $name } | ForEach-Object value) -join ','
}
function ProjectPath([string]$path) {
    (Resolve-Path -LiteralPath $(if ([IO.Path]::IsPathRooted($path)) { $path } else { Join-Path $project $path })).Path
}
Push-Location -LiteralPath $project
try {
    foreach ($scope in @('main', 'test', 'gameTestValidation')) {
        $roots = @($entries | Where-Object { $_.kind -eq 'src' -and (Attribute $_ 'gradle_scope') -eq $scope })
        $sources = @($roots | ForEach-Object {
            if ($_.GetAttribute('including') -or $_.GetAttribute('excluding')) {
                throw "Unsupported Eclipse source filter on $($_.path)"
            }
            Get-ChildItem -LiteralPath (ProjectPath $_.path) -Filter *.java -Recurse | ForEach-Object FullName
        } | Sort-Object -Unique)
        if (!$sources.Count) { throw "Empty Eclipse $scope source scope" }
        $libraries = @($entries | Where-Object { $_.kind -eq 'lib' -and
                ((Attribute $_ 'gradle_used_by_scope').Split(',') -contains $scope) } | ForEach-Object { ProjectPath $_.path })
        foreach ($dependency in @(
                @{ pattern = 'basePower-*.jar'; hash = $pins.poweradvantage_deobf_sha256 },
                @{ pattern = 'BaseMetals-*.jar'; hash = $pins.basemetals_deobf_sha256 },
                @{ pattern = 'OreSpawn-*.jar'; hash = $pins.orespawn_deobf_sha256 })) {
            $matches = @($libraries | Where-Object { [IO.Path]::GetFileName($_) -like $dependency.pattern })
            if ($matches.Count -ne 1 -or (Get-FileHash -LiteralPath $matches[0] -Algorithm SHA256).Hash -cne $dependency.hash) {
                throw "Eclipse $scope is missing the exact dependency $($dependency.pattern)"
            }
        }
        if ($scope -ne 'test' -and @($libraries | Where-Object { [IO.Path]::GetFileName($_) -like 'junit-*' }).Count) {
            throw "JUnit leaked into the Eclipse $scope compiler scope"
        }
        $output = Join-Path $run $scope
        New-Item -ItemType Directory -Path $output | Out-Null
        if ($scope -ne 'main') { $libraries = @((Join-Path $run 'main')) + $libraries }
        $arguments = @('-21', '-proc:none', '-encoding', 'UTF-8', '-classpath', ('"' + ($libraries -join ';') + '"'),
                '-d', ('"' + $output + '"')) + @($sources | ForEach-Object { '"' + $_ + '"' })
        $argumentFile = Join-Path $run "$scope.args"
        $arguments | Set-Content -LiteralPath $argumentFile -Encoding utf8
        $receipt.scopes[$scope] = [ordered]@{
            sourceCount = $sources.Count
            sources = @($sources | ForEach-Object { @{ path = $_; sha256 = (Get-FileHash -LiteralPath $_ -Algorithm SHA256).Hash } })
            libraries = $libraries
            exitCode = $null
        }
        & $java -XX:ActiveProcessorCount=4 -Xmx1G -jar $compiler "@$argumentFile" *> (Join-Path $run "$scope.log")
        $receipt.scopes[$scope].exitCode = $LASTEXITCODE
        if ($LASTEXITCODE -ne 0) { throw "ECJ $scope failed; see $run/$scope.log" }
    }
    $bootstrap = ProjectPath '../../PowerAdvantage/build/unit-bootstrap/forge-native-unit-bootstrap.jar'
    if ((Get-FileHash -LiteralPath $bootstrap -Algorithm SHA256).Hash -cne $pins.poweradvantage_test_bootstrap_sha256) {
        throw 'Pinned native unit bootstrap changed'
    }
    $runtime = @((Join-Path $run 'test'), (Join-Path $run 'main'), (Join-Path $project 'build/resources/main'))
    $runtime += @($receipt.scopes.test.libraries | Where-Object { $_ -ne (Join-Path $run 'main') })
    $tests = @($receipt.scopes.test.sources | Where-Object { $_.path -like '*Test.java' } | ForEach-Object {
        $source = Get-Content -Raw -LiteralPath $_.path
        if ($source -notmatch '(?m)^\s*package\s+([\w.]+)\s*;') { throw "Missing package in $($_.path)" }
        $Matches[1] + '.' + [IO.Path]::GetFileNameWithoutExtension($_.path)
    })
    $unitReceipt = Join-Path $run 'junit.json'
    & $java -XX:ActiveProcessorCount=4 -Xmx1536m "-javaagent:$bootstrap" --add-opens java.base/java.lang.invoke=ALL-UNNAMED `
            -cp ($runtime -join ';') zone.moddev.mc.basesteam.testing.EclipseUnitLauncher $unitReceipt @tests `
            *> (Join-Path $run 'junit.log')
    $receipt.unitExitCode = $LASTEXITCODE
    if ($LASTEXITCODE -ne 0) { throw "ECJ-compiled JUnit failed; see $run/junit.log" }
    $receipt.unit = Get-Content -Raw -LiteralPath $unitReceipt | ConvertFrom-Json
    $receipt.status = 'ECJ_ALL_SCOPES_AND_UNIT_PASS'
    Write-Output "$($receipt.status): $run"
} finally {
    $receipt.finishedUtc = [DateTime]::UtcNow.ToString('o')
    $receipt | ConvertTo-Json -Depth 9 | Set-Content -LiteralPath (Join-Path $run 'receipt.json') -Encoding utf8
    Pop-Location
}
