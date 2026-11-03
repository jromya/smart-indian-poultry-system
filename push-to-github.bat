@echo off
cd /d "%~dp0"
echo ==========================================================
echo   Pushing Smart Indian Poultry to GitHub (jromya)...
echo ==========================================================
git push -u origin main --force
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Push failed. Check internet connection and try again.
    pause
    exit /b 1
)
echo.
echo ==========================================================
echo   SUCCESS! Opening Render 1-Click Cloud Deploy...
echo   https://render.com/deploy?repo=https://github.com/jromya/smart-indian-poultry-system
echo ==========================================================
start https://render.com/deploy?repo=https://github.com/jromya/smart-indian-poultry-system
pause
