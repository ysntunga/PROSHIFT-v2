@echo off
echo ====================================
echo PROSHIFT GitHub Auto-Sync Script
echo ====================================
git pull --rebase origin main
git add .
git commit -m "Auto-sync from local workspace [%date% %time%]"
git push origin main
echo ====================================
echo Sync Completed Successfully!
echo ====================================
pause
