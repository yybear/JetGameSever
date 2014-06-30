#!/bin/sh
nohup java -server -cp ".:./lib/*:./log4j.properties" com.handwin.Main > err.log 2>&1 &
tail -f err.log
