#!/bin/sh
nohup java -server -cp ".:./lib/*:./log4j.properties" com.handwin.Main shaz > err_shaz.log 2>&1 &
tail -f err_shaz.log
