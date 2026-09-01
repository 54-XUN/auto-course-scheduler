@echo off
chcp 65001 >nul
title 排课系统一键启动
echo 正在启动排课系统，请稍候...
echo.
powershell -ExecutionPolicy Bypass -File "%~dp0start-all.ps1"
echo.
pause
