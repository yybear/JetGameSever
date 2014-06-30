#!/bin/sh
nohup java -server -cp ".:./lib/*:./log4j.properties" com.handwin.Main rhythm > err_rhythm.log 2>&1 &
tail -f err_rhythm.log
