@echo off
REM =====================================================================
REM CyberSaarthi — Launch Spring Boot Backend Application (CMD/Batch)
REM Port: 8080 | Host: 127.0.0.1
REM Requires: Java 21+ and Maven
REM =====================================================================

echo ==========================================================
echo   Starting CyberSaarthi Spring Boot Backend Service...   
echo ==========================================================

set "SCRIPT_DIR=%~dp0"
set "BACKEND_DIR=%SCRIPT_DIR%..\backend"

cd /d "%BACKEND_DIR%"

echo Running Maven Spring Boot application...
mvn spring-boot:run
