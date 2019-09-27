#!/bin/bash

cd ~/automato/

IP=10.0.0.21
PORT=11212
SHARDED=false
MEMCACHEDSERVERS="10.0.0.31:12333"


STDOUTPUT=outputMiddleware
ERROROUTPUT=errorMiddleware

date > $STDOUTPUT
date > $ERROROUTPUT

ALLSRGSMEMTIER="$@"

echo "Startparameter: /usr/bin/java -jar "$ALLSRGSMEMTIER >> $STDOUTPUT

/usr/bin/java -jar "$@"  >> $STDOUTPUT  2>>$ERROROUTPUT &
#/usr/bin/java -jar middleware-fimeier.jar -l $IP -p $PORT -t 8 -s $SHARDED -m $MEMCACHEDSERVERS  >> $STDOUTPUT  2>>$ERROROUTPUT &

echo $!
