@echo off
setlocal

if "%OS%"=="Windows_NT" goto nt
echo This script only works with NT-based versions of Windows.
goto :eof

:nt
rem
rem Find the application home.
rem
set _REALPATH=%cd%\

rem Decide on the wrapper binary.
set _WRAPPER_BASE=wrapper
set _WRAPPER_EXE=%_REALPATH%%_WRAPPER_BASE%-windows-x86-32.exe
if exist "%_WRAPPER_EXE%" goto conf
set _WRAPPER_EXE=%_REALPATH%%_WRAPPER_BASE%-windows-x86-64.exe
if exist "%_WRAPPER_EXE%" goto conf
set _WRAPPER_EXE=%_REALPATH%%_WRAPPER_BASE%.exe
if exist "%_WRAPPER_EXE%" goto conf
echo Unable to locate a Wrapper executable using any of the following names:
echo %_REALPATH%%_WRAPPER_BASE%-windows-x86-32.exe
echo %_REALPATH%%_WRAPPER_BASE%-windows-x86-64.exe
echo %_REALPATH%%_WRAPPER_BASE%.exe
pause
goto :eof

rem
rem Find the wrapper.conf
rem
:conf
set _WRAPPER_CONF="%_REALPATH%\conf\wrapper.conf"

rem
rem 
rem
:startup
IF "%1" == "" GOTO usage
IF "%1" == "-i" GOTO install
IF "%1" == "-r" GOTO uninstall
IF "%1" == "-t" GOTO start
IF "%1" == "-p" GOTO stop

:usage
echo storagemachine
echo   -i 	install
echo   -r 	uninstall
echo   -t 	start
echo   -p 	stop
goto :eof

:install
"%_WRAPPER_EXE%" -i %_WRAPPER_CONF%
if not errorlevel 1 goto :eof
pause

:uninstall
"%_WRAPPER_EXE%" -r %_WRAPPER_CONF%
if not errorlevel 1 goto :eof
pause

:start
"%_WRAPPER_EXE%" -t %_WRAPPER_CONF%
if not errorlevel 1 goto :eof
pause

:stop
"%_WRAPPER_EXE%" -p %_WRAPPER_CONF%
if not errorlevel 1 goto :eof
pause

