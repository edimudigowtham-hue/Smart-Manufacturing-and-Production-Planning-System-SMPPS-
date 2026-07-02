@echo off
setlocal

set "ROOT=%~dp0smpps-microservices"
set "MVNW=%ROOT%\mvnw.cmd"
set "POM=%ROOT%\pom.xml"

if not exist "%POM%" (
    echo Could not find microservices project at: %ROOT%
    exit /b 1
)

echo Compiling SMPPS microservices...
pushd "%ROOT%"
call "%MVNW%" compile -DskipTests
set "STATUS=%ERRORLEVEL%"
popd

if "%STATUS%"=="0" (
    echo.
    echo BUILD SUCCESS. Open this folder in IntelliJ:
    echo %ROOT%
) else (
    echo.
    echo BUILD FAILED with exit code %STATUS%.
)

exit /b %STATUS%


