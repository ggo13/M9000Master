#!/bin/sh

java -cp /usr/local/apache-activemq-5.4.2-fuse-01-00/activemq-all-5.4.2-fuse-01-00.jar:.:../lib/xbean.jar:../lib/M9kStation.jar:../lib/M9000XMLConfig.jar:../lib/commons-configuration-1.6.jar:../lib/commons-lang-2.4.jar:../lib/commons-collections-3.2.1.jar:../lib/jsch-0.1.55.jar:../lib/log4j-1.2.16.jar:../lib/mysql-connector-java-5.1.13-bin.jar com.usi.m9000.station.consumers.M9kMasterRequestHandler
