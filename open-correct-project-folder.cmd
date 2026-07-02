@echo off
setlocal

set "ROOT=%~dp0smpps-microservices"

if not exist "%ROOT%\pom.xml" (
    echo Could not find microservices project at: %ROOT%
    pause
    exit /b 1
)

echo Opening the correct microservices folder:
echo %ROOT%
explorer "%ROOT%"

echo.
echo In IntelliJ, use File ^> Open and select this same folder:
echo %ROOT%
pause

