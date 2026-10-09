@echo off
setlocal

set ROOT=%~dp0..
cd /d "%ROOT%"

echo Building (Maven downloads the Disruptor jar on first run)...
call mvn -q clean package
if errorlevel 1 exit /b 1
call mvn -q dependency:copy-dependencies -DoutputDirectory=target/lib
if errorlevel 1 exit /b 1

echo Build OK.
endlocal
