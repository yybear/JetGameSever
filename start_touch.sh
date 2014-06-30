#!/bin/sh
nohup java -server -cp ".:./lib/*:./log4j.properties" com.handwin.Main touch > err_touch.log 2>&1 &
tail -f err_touch.log
