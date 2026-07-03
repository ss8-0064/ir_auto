@echo off
echo === IR Auto 一键推送 ===

REM Set proxy (change port if needed)
set http_proxy=http://127.0.0.1:7890
set https_proxy=http://127.0.0.1:7890

cd /d "%~dp0"

echo [1/3] Adding changes...
git add -A

echo [2/3] Committing...
set /p MSG="Commit message (press Enter for default): "
if "%MSG%"=="" set MSG=update
git commit -m "%MSG%"

echo [3/3] Pushing to GitHub...
git push

echo === Done ===
pause
