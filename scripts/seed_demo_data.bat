@echo off
REM =====================================================================
REM CyberSaarthi — Seed Demonstration Dataset Batch Script
REM =====================================================================

set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=cybersaarthi_dev
set DB_USER=postgres
if "%PGPASSWORD%"=="" set PGPASSWORD=postgres

echo ==========================================================
echo CyberSaarthi: Seeding Demonstration Dataset
echo ==========================================================
echo Database: %DB_NAME% on %DB_HOST%:%DB_PORT% (User: %DB_USER%)

psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "%~dp0..\backend\src\main\resources\db\demo\demo_seed_data.sql"

if %ERRORLEVEL% equ 0 (
    echo.
    echo Demo data seeded successfully!
    echo Demo Accounts:
    echo   - Administrator: admin@cybersaarthi.org / CyberSaarthi@123
    echo   - Citizen User : citizen@cybersaarthi.org / CyberSaarthi@123
    echo   - Student User : student@cybersaarthi.org / CyberSaarthi@123
) else (
    echo Error executing seed script. Please verify PostgreSQL is running.
)
pause
