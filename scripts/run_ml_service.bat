@echo off
REM =====================================================================
REM CyberSaarthi — Launch FastAPI ML Inference Service
REM Port: 8000 | Host: 127.0.0.1
REM =====================================================================

cd /d "%~dp0\.."
echo [CyberSaarthi] Starting ML Inference Service on http://127.0.0.1:8000...
ml\.venv\Scripts\python.exe -m uvicorn ml.inference.main:app --host 127.0.0.1 --port 8000 --reload
