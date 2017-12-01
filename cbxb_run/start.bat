@echo off

set START_FOLDER=C:\Users\ace.li\CBX\WorkSpaces\CBX_Test\cbxb_run

:project
cd %START_FOLDER%
cls
echo.
echo -----------------------------propertis------------------------------------
echo BuildBizJar: %START_FOLDER%
echo ==========================================================================
echo.
echo 1. start pgsql run
echo 2. build common jar
echo 3. build core jar
echo 4. build ui jar
echo 5. build general jar
echo 6. Exit
echo --------------------------------------------------------------------------
echo E. Exit
echo.
echo.
choice /C 123456e /M "Choose an option"
if errorlevel 6 goto exit
if errorlevel 5 goto buildGeneralJar
if errorlevel 4 goto buildUIJar
if errorlevel 3 goto buildCoreJar
if errorlevel 2 goto buildCommonJar
if errorlevel 1 goto pgsql


:pgsql
call run-pgsql.bat
goto project

:buildCommonJar
call Maven_Common.bat
goto project

:buildCoreJar
call Maven_Core.bat
goto project

:buildUIJar
call Maven_UI.bat
goto project

:buildGeneralJar
call Maven_General.bat
goto project


:fileNotFound
echo.
echo.
echo.
echo ---------------------------------------------error----------------------------------------------------------------------------
echo.
echo init.bat not found, please copy init.bat.sample to init.bat and correct the parames.
echo.
echo ------------------------------------------------------------------------------------------------------------------------------
echo.
echo.
echo.

:exit
cmd
goto end

:end