# =====================================================================
# CyberSaarthi — Launch React Web Client (PowerShell)
# Port: 3000 | Host: 127.0.0.1
# Requires: Node.js 18+ and npm
# =====================================================================

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "    Starting CyberSaarthi React Web Client (Vite)...     " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$FrontendDir = Join-Path $ProjectRoot "frontend"

Set-Location $FrontendDir

Write-Host "Starting Vite development server on port 3000..." -ForegroundColor Green
npm run dev
