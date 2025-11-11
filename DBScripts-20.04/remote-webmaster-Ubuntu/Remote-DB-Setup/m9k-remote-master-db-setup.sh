#!/bin/bash

echo "\nInstalling database..."

mysql -u root -pusi < MySql-Remote-Master-Setup.sql 2>&1 | grep -v "\[Warning\] Using a password"

if [ $? -eq 0 ]
then
	echo "DB Set up SUCCESSFUL"
	exit 0;
else
	echo "Db set up FAILED"
	exit 1
fi


