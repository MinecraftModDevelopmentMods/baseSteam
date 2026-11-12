[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidatePattern('^[a-zA-Z0-9_-]+$')][string]$RunId,
    [string]$JavaExe = "$env:USERPROFILE/.jdks/jdk-25.0.3+9/bin/java.exe",
    [string]$CompilerJar = "$env:USERPROFILE/.p2/pool/plugins/org.eclipse.jdt.core.compiler.batch_3.46.0.v20260528-0407.jar"
)

$ErrorActionPreference = 'Stop'
$jvmEnvironment = [ordered]@{}
foreach ($name in @('JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', '_JAVA_OPTIONS')) {
    $jvmEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
    if ($null -ne $jvmEnvironment[$name]) { throw "ECJ qualification must not inherit $name" }
}
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$java = (Resolve-Path -LiteralPath $JavaExe).Path
$compiler = (Resolve-Path -LiteralPath $CompilerJar).Path
$compilerHash = (Get-FileHash -LiteralPath $compiler -Algorithm SHA256).Hash
if ($compilerHash -cne 'D0D43F8E2D7003E5EFED612E2CBB5F01870043397D8F1BBE536FD9128F4FCBF7') {
    throw 'The installed Eclipse ECJ 3.46.0 compiler does not match the qualified tool.'
}
$version = (& $java -version 2>&1 | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $version -notmatch 'Temurin-25\.0\.3\+9') { throw "Unexpected JVM: $version" }
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
    jvmOptionEnvironment = $jvmEnvironment
    compilerJvmArguments = @('-XX:ActiveProcessorCount=4', '-Xmx1G')
    unitJvmArguments = @('-XX:ActiveProcessorCount=4', '-Xmx1536m')
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
        $arguments = @('-25', '-proc:none', '-encoding', 'UTF-8', '-classpath', ('"' + ($libraries -join ';') + '"'),
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
    $results = Join-Path $project 'build/test-results/test'
    $xml = @(Get-ChildItem -LiteralPath $results -Filter TEST-*.xml | Sort-Object Name)
    if ($xml.Count -lt 2) { throw 'A complete Gradle XML inventory is required before independent ECJ qualification' }
    $receipt.gradleResults = @($xml | ForEach-Object {
        @{ path = $_.FullName; sha256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash }
    })
    $inventory = Join-Path $run 'complete-inventory.json'
    $inventoryMain = 'zone.moddev.mc.basesteam.testing.EclipseTestInventory'
    & $java -XX:ActiveProcessorCount=4 -cp ($runtime -join ';') $inventoryMain (Join-Path $run 'test') $results $inventory `
            *> (Join-Path $run 'complete-inventory.log')
    if ($LASTEXITCODE -ne 0) { throw "Compiled JUnit inventory differs from complete Gradle results; see $run/complete-inventory.log" }
    $receipt.inventory = Get-Content -Raw -LiteralPath $inventory | ConvertFrom-Json
    $tests = @($receipt.inventory.suites.PSObject.Properties.Name | Sort-Object)
    $receipt.inventoryNegativeControls = @()
    foreach ($negative in @('omitted-suite', 'omitted-method', 'stale-method')) {
        $negativeResults = Join-Path $run $negative
        New-Item -ItemType Directory -Path $negativeResults | Out-Null
        $xml | Copy-Item -Destination $negativeResults
        $first = Join-Path $negativeResults $xml[0].Name
        if ($negative -eq 'omitted-suite') {
            Remove-Item -LiteralPath $first
        } else {
            [xml]$document = Get-Content -Raw -LiteralPath $first
            $method = @($document.testsuite.testcase)[0]
            if ($negative -eq 'omitted-method') {
                $null = $document.testsuite.RemoveChild($method)
                $document.testsuite.SetAttribute('tests', ([int]$document.testsuite.tests - 1).ToString())
            } else {
                $method.SetAttribute('name', 'staleInventoryNegativeControl')
            }
            $document.Save($first)
        }
        & $java -XX:ActiveProcessorCount=4 -cp ($runtime -join ';') $inventoryMain (Join-Path $run 'test') $negativeResults `
                (Join-Path $run "$negative.json") *> (Join-Path $run "$negative.log")
        if ($LASTEXITCODE -eq 0 -or (Get-Content -Raw -LiteralPath (Join-Path $run "$negative.log")) -notmatch
                'Incomplete or stale Gradle inventory|Duplicate or inconsistent Gradle suite') {
            throw "ECJ inventory accepted or failed for the wrong reason: $negative"
        }
        $receipt.inventoryNegativeControls += $negative
    }
    $unitReceipt = Join-Path $run 'junit.json'
    $powerJar = @($receipt.scopes.test.libraries | Where-Object { [IO.Path]::GetFileName($_) -like 'basePower-*.jar' })
    if ($powerJar.Count -ne 1) { throw 'ECJ unit runtime must retain exactly one audited native Power jar' }
    & $java -XX:ActiveProcessorCount=4 -Xmx1536m "-javaagent:$bootstrap" "-Dbasesteam.nativePowerJar=$($powerJar[0])" --add-opens java.base/java.lang.invoke=ALL-UNNAMED `
            -cp ($runtime -join ';') zone.moddev.mc.basesteam.testing.EclipseUnitLauncher $unitReceipt @tests `
            *> (Join-Path $run 'junit.log')
    $receipt.unitExitCode = $LASTEXITCODE
    if ($LASTEXITCODE -ne 0) { throw "ECJ-compiled JUnit failed; see $run/junit.log" }
    $receipt.unit = Get-Content -Raw -LiteralPath $unitReceipt | ConvertFrom-Json
    if ($receipt.unit.tests -ne $receipt.inventory.tests -or $receipt.unit.ignored -ne 0 -or
            $receipt.unit.assumptions -ne 0 -or $receipt.unit.failures -ne 0 -or
            (@($receipt.unit.classes | Sort-Object) -join ',') -cne ($tests -join ',')) {
        throw 'ECJ JUnit execution did not match the complete compiled test inventory'
    }
    $receipt.status = 'ECJ_ALL_SCOPES_AND_UNIT_PASS'
    Write-Output "$($receipt.status): $run"
} finally {
    $receipt.finishedUtc = [DateTime]::UtcNow.ToString('o')
    $receipt | ConvertTo-Json -Depth 9 | Set-Content -LiteralPath (Join-Path $run 'receipt.json') -Encoding utf8
    Pop-Location
}
