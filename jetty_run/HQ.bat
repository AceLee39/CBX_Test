echo off

setlocal

SET HQ_HOME=..\..\hornetq-2.4.0.Final\bin

echo %HQ_HOME%
cd %HQ_HOME%
run.bat
pause
endlocal