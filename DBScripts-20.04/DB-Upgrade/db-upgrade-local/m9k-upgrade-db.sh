#!/bin/bash

mysql -uroot -pusi < m9k-update-grants-procedures-events.sql 2>&1 | grep -v "\[Warning\] Using a password"
if [ $? -eq 0 ]
then
echo "Successfully removed all old procedures and events owned by root."
else
echo "Error in removing old procedures and events owned by root"
exit 1;
fi
mysql -u dfr -pusi < MySql-Station-upgrade-local.sql 2>&1 | grep -v "\[Warning\] Using a password"
if [ $? -eq 0 ]
then
	# Altering ser table
	EXISTS=$(mysql --skip-column-names -udfr -pusi m9000 -e "SELECT count(*) FROM information_schema.columns WHERE table_schema = 'm9000' AND table_name = 'ser' and COLUMN_NAME = 'ser_date'" 2>&1 | grep -v "\[Warning\] Using a password");
	if [ $EXISTS -eq 0 ]
	then
		SER_COUNT=$(mysql --skip-column-names -udfr -pusi m9000 -e "select TABLE_ROWS from information_schema.tables where table_schema='m9000' and table_name = 'ser'" 2>&1 | grep -v "\[Warning\] Using a password");
		if [ $SER_COUNT -ge 10000000 ]
		then
			echo "We have approximately $SER_COUNT rows in SER table right now. Modifying SER table might take up to an hour or more based on the size. Please wait..."
		fi 
		time mysql -u root -pusi < m9k-alter-ser.sql 2>&1 | grep -v "\[Warning\] Using a password"
		if [ $? -eq 0 ]
		then
			echo "SER table update successful"
		else
			echo "SER table update failed"
		fi
	else
		echo "SER table is already updated. Skipping."
	fi
	
	# Altering dfrlog table
	time mysql -u root -pusi < m9k-alter-dfrlog.sql 2>&1 | grep -v "\[Warning\] Using a password"
	if [ $? -eq 0 ]
	then
		echo "dfrlog table update successful"
	else
		echo "dfrlog table update failed"
	fi
	
	# Modify all MyISAM tables to INNODB
	time mysql -u dfr -pusi m9000 -e  "show table status where Engine='MyISAM';" 2>&1 | grep -v "\[Warning\] Using a password" | awk  'NR>1 {print "ALTER TABLE "$1" ENGINE = InnoDB;"}'  |   mysql -u dfr -pusi m9000 2>&1 | grep -v "\[Warning\] Using a password"
	if [ $? -eq 0 ]
	then
		echo "All tables with old MyISAM updated successful to new INNODB"
	else
		echo "Update to INNODB engine failed"
	fi
# MySQL connector for servd has to be updated before changing authentication plugin	
#	mysql -uroot -pusi < update_users_Auth_plugin.sql 2>&1 | grep -v "\[Warning\] Using a password"
#	if [ $? -eq 0 ]
#	then
#	echo "Successfully removed all old procedures and events owned by root."
#	else
#	echo "Error in removing old procedures and events owned by root"
#	exit 1;
#	fi
	
	
else
	echo "Alter table FAILED"
	exit 1
fi
