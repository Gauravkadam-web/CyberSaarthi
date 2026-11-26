# CyberSaarthi — Seed Demonstration Dataset Script
# Target: PostgreSQL local dev database (cybersaarthi_dev)

param (
    [string]$DbHost = "localhost",
    [string]$DbPort = "5432",
    [string]$DbName = "cybersaarthi_dev",
    [string]$DbUser = "postgres"
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "CyberSaarthi: Seeding Demonstration Dataset" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Target Database : $DbName on $DbHost:$DbPort (User: $DbUser)" -ForegroundColor Yellow

$scriptPath = Join-Path $PSScriptRoot "..\backend\src\main\resources\db\demo\demo_seed_data.sql"

if (-not (Test-Path $scriptPath)) {
    Write-Error "Seed SQL file not found at: $scriptPath"
    exit 1
}

$env:PGPASSWORD = $env:DB_PASSWORD
if (-not $env:PGPASSWORD) {
    $env:PGPASSWORD = "postgres"
}

try {
    & psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -f $scriptPath
    Write-Host "`nDemo data seeded successfully!" -ForegroundColor Green
    Write-Host "Demo Accounts:" -ForegroundColor White
    Write-Host "  - Administrator: admin@cybersaarthi.org / CyberSaarthi@123" -ForegroundColor Green
    Write-Host "  - Citizen User : citizen@cybersaarthi.org / CyberSaarthi@123" -ForegroundColor Green
    Write-Host "  - Student User : student@cybersaarthi.org / CyberSaarthi@123" -ForegroundColor Green
} catch {
    Write-Error "Failed to seed demo data. Please verify PostgreSQL is running."
}
