@echo off
rem Starts DotProxy and restarts it automatically when it asks to be restarted.
rem Place this script next to DotProxy.jar. Requires Java 21+ on the PATH (or set JAVA).
rem Exit code 69 = intentional shutdown (no restart).
cd /d "%~dp0"
if "%JAVA%"=="" set JAVA=java
if "%JVM_ARGS%"=="" set JVM_ARGS=-Xmx300m -Xms32m -XX:+UseG1GC -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -XX:G1PeriodicGCInterval=30000
:loop
"%JAVA%" %JVM_ARGS% --enable-native-access=ALL-UNNAMED -jar DotProxy.jar %*
if %ERRORLEVEL%==69 (
  echo DotProxy shut down.
  exit /b 0
)
echo DotProxy exited with code %ERRORLEVEL%, restarting in 3 seconds... (Ctrl+C to stop)
timeout /t 3 /nobreak >nul
goto loop
