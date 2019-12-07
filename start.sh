#!/bin/bash

# I do NOT use this file!! Just here for you

#remark: I used additional flags. They represent the default behavior of the middleware
#finalStats files are created for window 15-74
#you should start the middleware and give it 1.5 seconds time befor you start memtier.
#Run memtier for 80 seconds

#middleware_flags=-m 10.0.0.31:12333 -tTimeForMeasurements 81500 -p 11212 -tWaitBeforeMeasurements 1500 -s false -t 32 -l 10.0.0.21
middleware_flags="-m 10.0.0.31:12333 -p 11212 -s false -t 32 -l 10.0.0.21"

java -jar dist/middleware-fimeier.jar $middleware_flags &> mw.log &
echo $! > mw.pid
