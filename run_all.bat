@echo off
setlocal
cd /d "%~dp0"

where mvn >nul 2>nul
if errorlevel 1 (
  echo ERROR: Maven no esta instalado o no esta en PATH.
  echo Instale Maven 3.9 o superior y vuelva a ejecutar este archivo.
  exit /b 1
)

if "%REQRES_API_KEY%"=="" (
  echo ERROR: Defina REQRES_API_KEY para ejecutar los escenarios API de Reqres.
  exit /b 1
)

echo Ejecutando todos los escenarios Web y API...
set "EFFECTIVE_LOGS=%EXECUTION_LOGS%"
if "%EFFECTIVE_LOGS%"=="" (
  for /f "tokens=2 delims==" %%L in ('findstr /R /C:"^[ ]*execution.logs[ ]*=" serenity.properties') do set "EFFECTIVE_LOGS=%%L"
)
if "%EFFECTIVE_LOGS%"=="" set "EFFECTIVE_LOGS=false"
set "EFFECTIVE_LOGS=%EFFECTIVE_LOGS: =%"

if /I "%EFFECTIVE_LOGS%"=="true" (
  call mvn clean verify "-Dcucumber.filter.tags=@automation"
) else (
  call mvn -q clean verify "-Dcucumber.filter.tags=@automation"
)
set EXIT_CODE=%ERRORLEVEL%

if not "%EXIT_CODE%"=="0" (
  echo La ejecucion termino con errores. Revise reports y target/site/serenity.
  exit /b %EXIT_CODE%
)

echo Ejecucion finalizada correctamente.
endlocal
