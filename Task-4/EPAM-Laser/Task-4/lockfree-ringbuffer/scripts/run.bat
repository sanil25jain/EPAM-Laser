@echo off
setlocal
set ROOT=%~dp0..
java -cp "%ROOT%\out" com.team.ringbuffer.RingBufferTest
endlocal
