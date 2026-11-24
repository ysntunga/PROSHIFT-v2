# PROSHIFT GitHub Auto-Sync Script for PowerShell
Write-Host "====================================" -ForegroundColor Cyan
Write-Host "PROSHIFT GitHub Auto-Sync Script" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Cyan

git pull --rebase origin main
git add .
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
git commit -m "Auto-sync from local workspace [$timestamp]"
git push origin main

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "Sync Completed Successfully!" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Cyan
