@ECHO off

IF "%1"=="" (GOTO ALL) else (GOTO SPECIFIC)
GOTO END

:ALL
echo "!!!Executing this script without station id will remove all the configurations available. "
	setlocal
	:PROMPT
	SET /P AREYOUSURE=Are you sure (Y/[N])?
	IF /I "%AREYOUSURE%" NEQ "Y" GOTO END

	:PROMPT
	SET /P AREYOUSURE=Are you really sure (Y/[N])?
	IF /I "%AREYOUSURE%" NEQ "Y" GOTO END
	
	mysql -u root -p -e "truncate from m9000.dat"
	endlocal 
	GOTO END

:SPECIFIC
echo You are about to remove the station configuration with station id %1
	setlocal
	:PROMPT
	SET /P AREYOUSURE=Are you sure (Y/[N])?
	IF /I "%AREYOUSURE%" NEQ "Y" GOTO END
	
	mysql -u root -p -e "delete from m9000.station_details where stationId = %1" 
	endlocal

:END
if %ERRORLEVEL%==0 (echo Successfully exiting.) else (echo Task failed to complete due to error)

EXIT /B %ERRORLEVEL%