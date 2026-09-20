param(
    [string]$Tags = "@automation",

    [ValidateSet("properties", "true", "false")]
    [string]$Logs = "properties",

    [switch]$Headless
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Error "Maven no está instalado o no se encuentra en PATH."
    exit 1
}

$effectiveLogs = "false"
$propertyLine = Select-String -Path "$PSScriptRoot\serenity.properties" `
    -Pattern '^\s*execution\.logs\s*=\s*(true|false)\s*$' | Select-Object -First 1

if ($propertyLine) {
    $effectiveLogs = ($propertyLine.Matches[0].Groups[1].Value).ToLowerInvariant()
}
if ($env:EXECUTION_LOGS -match '^(?i:true|false)$') {
    $effectiveLogs = $env:EXECUTION_LOGS.ToLowerInvariant()
}
if ($Logs -ne "properties") {
    $effectiveLogs = $Logs
}

$mavenArguments = @()
if ($effectiveLogs -eq "false") {
    $mavenArguments += "-q"
}

$mavenArguments += @(
    "clean",
    "verify",
    "-Dcucumber.filter.tags=$Tags"
)

if ($Logs -ne "properties") {
    $mavenArguments += "-Dexecution.logs=$Logs"
}

if ($Headless) {
    $mavenArguments += "-Dheadless.mode=true"
}

& mvn @mavenArguments
exit $LASTEXITCODE
