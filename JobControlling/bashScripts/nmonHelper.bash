#!/bin/bash

# $1 start|stop
# $2 filename
# $3 captureduration

cd ~/automato/

if [ $1 == 'start' ]
then
    if test -f nmonPIDlastRun
    then
	PID=`cat nmonPIDlastRun`
    	echo "killing nmon pid...probably still running ="+$PID
        kill -USR2 $PID
    fi
    nmon -F ${2}.nmon -s1 -c$3 -p > nmonPIDlastRun
fi

if [ $1 == 'stop' ]
then
    PID=`cat nmonPIDlastRun`
    echo "killing pid="+$PID
    kill -USR2 $PID
    rm nmonPIDlastRun 
fi

