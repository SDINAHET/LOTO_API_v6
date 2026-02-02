@echo off
echo ============================================
echo  Liberation du port 5432 (PostgreSQL)
echo ============================================

echo.
echo Processus utilisant le port 5432 :
netstat -ano | findstr :5432

echo.
echo Si un PID est affiche ci-dessus :
echo   - Notez le dernier nombre (PID)
echo   - Utilisez : taskkill /F /PID [PID]

echo.
echo Tentative d'arret des services PostgreSQL...

net stop postgresql >nul 2>&1
net stop "postgresql-x64-16" >nul 2>&1
net stop "postgresql-x64-15" >nul 2>&1
net stop "postgresql-x64-14" >nul 2>&1

echo.
echo Verifiez a nouveau :
netstat -ano | findstr :5432

echo.
echo Si le port est libre, vous pouvez continuer
echo (installation PostgreSQL ou Docker Desktop)

pause
