@echo off
REM =====================================================================
REM CyberSaarthi — Launch React Web Client (CMD/Batch)
REM Port: 3000 | Host: 127.0.0.1
REM Requires: Node.js 18+ and npm
REM =====================================================================

echo ==========================================================
echo     Starting CyberSaarthi React Web Client (Vite)...     
echo ==========================================================

set "SCRIPT_DIR=%~dp0"
set "FRONTEND_DIR=%SCRIPT_DIR%..\frontend"

cd /d "%FRONTEND_DIR%"

echo Starting Vite development server on port 3000...
npm run dev
