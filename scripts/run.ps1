#Requires -Version 5.1
# Installs dependencies and starts the hotel-booking Spring Boot app.
# Usage: .\scripts\run.ps1 [-Port 8080]
param(
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
Set-Location $ProjectRoot

function Find-Jdk17Plus {
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
        try {
            $verLine = & "$env:JAVA_HOME\bin\java.exe" -version 2>&1 | Select-String 'version "(\d+)'
            if ($verLine -and [int]$verLine.Matches[0].Groups[1].Value -ge 17) {
                return $env:JAVA_HOME
            }
        } catch { }
    }
    $roots = @("C:\Program Files\Java", "C:\Program Files (x86)\Java")
    foreach ($root in $roots) {
        $candidates = Get-ChildItem $root -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match 'jdk-?(1[7-9]|[2-9]\d)' } |
            Sort-Object Name -Descending
        if ($candidates) { return $candidates[0].FullName }
    }
    return $null
}

$jdk = Find-Jdk17Plus
if ($jdk) {
    $env:JAVA_HOME = $jdk
    Write-Host "Using JAVA_HOME=$jdk"
} else {
    Write-Warning "No JDK 17+ found automatically; using whatever 'java'/JAVA_HOME is already set. Build will fail if it's older than 17."
}

Write-Host "Installing dependencies..."
& "$ProjectRoot\mvnw.cmd" -q -DskipTests install
if ($LASTEXITCODE -ne 0) { throw "Maven install failed" }

Write-Host "Starting hotel-booking on port $Port ..."
& "$ProjectRoot\mvnw.cmd" spring-boot:run "-Dspring-boot.run.arguments=--server.port=$Port"
