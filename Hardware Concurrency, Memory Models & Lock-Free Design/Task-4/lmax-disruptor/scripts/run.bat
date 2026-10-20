@echo off
setlocal
set ROOT=%~dp0..
java -cp "%ROOT%\target\classes;%ROOT%\target\lib\*" com.team.disruptorlib.DisruptorLibraryTest %*
endlocal
