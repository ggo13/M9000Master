#!/bin/bash
mysql -u root -pusi < MySql-RemoteMaster-upgrade-db.sql 2>&1 | grep -v "\[Warning\] Using a password"
