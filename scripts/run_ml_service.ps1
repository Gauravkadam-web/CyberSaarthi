# =====================================================================
# CyberSaarthi — Launch FastAPI ML Inference Service (PowerShell)
# Port: 8000 | Host: 127.0.0.1
# =====================================================================

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location "$ScriptDir\.."

Write-Host "[CyberSaarthi] Starting ML Inference Service on http://127.0.0.1:8000..." -ForegroundColor Cyan
& "ml\.venv\Scripts\python.exe" -m uvicorn ml.inference.main:app --host 127.0.0.1 --port 8000 --reload
