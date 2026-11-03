@echo off
setlocal
cd /d "%~dp0"
title Smart Poultry Farm Backend
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run.ps1"
pause
