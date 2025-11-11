@ECHO off
echo updating database...

mysql -u root -pusi < MySql-Windows-Master-Upgrade.sql

if %ERRORLEVEL% == 0 (
	echo DB Set up SUCCESSFUL
	goto end
) else (
	echo Db set up FAILED
	goto end
)

:end


