@echo off
echo Run the SQL scripts in folder
setlocal

REM ------------------
REM    Check/ set variable
REM ------------------
if not defined ANT_HOME goto ERR_1
echo ANT_HOME=%ANT_HOME%
set ANT_BAT_FILE=%ANT_HOME%/bin/ant.bat
REM set ANT_OPTS=-Xms512m -Xmx512m

REM if not exist %ANT_BAT_FILE% goto ERR_2W

REM ------------------
REM    Call the ant
REM ------------------
call "%ANT_BAT_FILE%" dropDB

goto SUCC_END

REM ------------------
REM    Error handling
REM ------------------
:ERR_1
echo Error: Please define ANT_HOME first.
goto HELP_MSG

:ERR_2
echo Error: File not found %ANT_BAT_FILE%
goto HELP_MSG

:ERR_3
echo Error: Cannot find the SQL folder: %1
goto HELP_MSG

:ERR_4
echo Error: Cannot find the DB profile: dbProfiles/%2.properties
goto HELP_MSG

REM ------------------
REM    Help message
REM ------------------
:HELP_MSG
echo.
echo Usage: runSqlInFolder.cmd [SQL_FOLDER] [DB_PROFILE]
echo Run the SQL scripts in [SQL_FOLDER] using the [DB_PROFILE] in dbProfiles.
echo.
echo Mandatory arguments:
echo     SQL_FOLDER: SQL file folder (e.g. release)
echo     DB_PROFILE: DB Profile name (e.g. dev, prod, test, etc.)
echo.
echo This program depends on Apache Ant.
echo.
goto END_FILE

REM ------------------
REM    Success ending
REM ------------------
:SUCC_END
goto END_FILE

:END_FILE
REM ------------------
REM    Program end
REM ------------------
endlocal
