# =====================================================================
# CyberSaarthi — Launch Spring Boot Backend Application (PowerShell)
# Port: 8080 | Host: 127.0.0.1
# Requires: Java 21+ and Maven
# =====================================================================

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Starting CyberSaarthi Spring Boot Backend Service...   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$BackendDir = Join-Path $ProjectRoot "backend"

Set-Location $BackendDir

Write-Host "Running Maven Spring Boot application..." -ForegroundColor Green
mvn spring-boot:run
