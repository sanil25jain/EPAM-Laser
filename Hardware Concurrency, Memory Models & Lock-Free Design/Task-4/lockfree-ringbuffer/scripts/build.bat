@echo off
setlocal

set ROOT=%~dp0..
set OUT=%ROOT%\out

echo Building into %OUT%
if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"

dir /s /b "%ROOT%\src\main\java\*.java" > "%OUT%\sources.txt"
javac -d "%OUT%" "@%OUT%\sources.txt"
del "%OUT%\sources.txt"

echo Build OK.
endlocal
