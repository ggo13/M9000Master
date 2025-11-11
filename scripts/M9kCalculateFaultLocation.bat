@echo off
setlocal EnableDelayedExpansion

rem Get command line arguments
set "args=%*"
set n=0
for %%a in ("%args: =" "%") do (
   set /A n+=1
   set "arg[!n!]=%%~a"
)
java -jar ..\lib\M9kFaultLocation.jar %1 %arg[2]%