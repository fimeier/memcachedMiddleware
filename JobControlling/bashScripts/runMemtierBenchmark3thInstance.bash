#!/bin/bash

cd ~/automato/

#CLIENT=Client1

STDOUTPUT=outputClient3thInst
ERROROUTPUT=errorClient3thInst


date > $STDOUTPUT
date > $ERROROUTPUT

ALLSRGSMEMTIER="$@"

echo "Startparameter: memtier_benchmark "$ALLSRGSMEMTIER >> $STDOUTPUT

#memtier_benchmark --server=10.0.0.21 --port=11212 --protocol=memcache_text --json-out-file=json.txt -d4096 -x1 --expiry-range=9999-10000 --key-maximum=100 -x1 -n100 >> $STDOUTPUT 2>>$ERROROUTPUT 

memtier_benchmark "$@" >> $STDOUTPUT 2>>$ERROROUTPUT 
