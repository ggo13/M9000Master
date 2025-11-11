#!/bin/bash
source m9k-utils
echo "WARNING: It will remove the existing database and all data will be lost"
ConfirmOrExit
echo "\nRemoving existing database..."

mysql -u root -pusi -e 'drop database IF EXISTS m9000' 2>&1 | grep -v "\[Warning\] Using a password"

if [ $? -eq 0 ]
then
	echo "Database remove SUCCESSFUL"
	exit 0;
else
	echo "Database remove FAILED"
	exit 1
fi


