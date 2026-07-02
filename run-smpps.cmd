@echo off
setlocal

set "ROOT=%~dp0smpps-microservices"
set "MVNW=%ROOT%\mvnw.cmd"
set "POM=%ROOT%\pom.xml"

if not exist "%POM%" (
	echo Could not find microservices project at: %ROOT%
	exit /b 1
)

echo Starting SMPPS microservices from: %ROOT%
echo Each service will open in a separate command window.

start "SMPPS Eureka Server" cmd /k "cd /d %ROOT% && call mvnw.cmd -pl eureka-server spring-boot:run"
timeout /t 15 /nobreak >nul

start "SMPPS Config Server" cmd /k "cd /d %ROOT% && call mvnw.cmd -pl config-server spring-boot:run"
timeout /t 10 /nobreak >nul

start "SMPPS Product Service" cmd /k "cd /d %ROOT% && call mvnw.cmd -pl product-service spring-boot:run"
start "SMPPS Production Order Service" cmd /k "cd /d %ROOT% && call mvnw.cmd -pl production-order-service spring-boot:run"
start "SMPPS Machine Service" cmd /k "cd /d %ROOT% && call mvnw.cmd -pl machine-service spring-boot:run"
start "SMPPS Quality Service" cmd /k "cd /d %ROOT% && call mvnw.cmd -pl quality-service spring-boot:run"
start "SMPPS Maintenance Service" cmd /k "cd /d %ROOT% && call mvnw.cmd -pl maintenance-service spring-boot:run"

timeout /t 10 /nobreak >nul
start "SMPPS API Gateway" cmd /k "cd /d %ROOT% && call mvnw.cmd -pl api-gateway spring-boot:run"

echo Startup commands have been launched.
endlocal

