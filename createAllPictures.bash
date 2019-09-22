#!/bin/bash

cd JobControlling/experiments/baseline21
./createPlots.bash

cd ../baseline22
./createPlots.bash

cd ../baseline31
./createPlots.bash
./createPlotsMiddleware.bash
chmod 755 createPlotsMiddleware.bashavgQueueLength
./createPlotsMiddleware.bashavgQueueLength
chmod 755 createPlotsMiddleware.bashavgMemcachedResponseTimeSet
./createPlotsMiddleware.bashavgMemcachedResponseTimeSet

cd ../baseline32
./createPlots.bash
./createPlotsMiddleware.bash
chmod 755 createPlotsMiddleware.bashavgQueueLength
./createPlotsMiddleware.bashavgQueueLength
chmod 755 createPlotsMiddleware.bashavgMemcachedResponseTimeSet
./createPlotsMiddleware.bashavgMemcachedResponseTimeSet

cd ../fullSystem41
chmod 755 createPlots.bash
./createPlots.bash
chmod 755 createPlotsMiddleware.bash
./createPlotsMiddleware.bash
chmod 755 createPlotsMiddleware.bashavgQueueLength
./createPlotsMiddleware.bashavgQueueLength
chmod 755 createPlotsMiddleware.bashavgMemcachedResponseTimeSet
./createPlotsMiddleware.bashavgMemcachedResponseTimeSet


cd ../shardedCase51
chmod 755 createPlots.bash
./createPlots.bash
chmod 755 createPlotsMiddleware.bash
./createPlotsMiddleware.bash
chmod 755 createPlotsMiddleware.bashavgQueueLength
./createPlotsMiddleware.bashavgQueueLength
chmod 755 createPlotsMiddleware.bashavgMemcachedResponseTimeSet
./createPlotsMiddleware.bashavgMemcachedResponseTimeSet
chmod 755 createPlotsMiddleware.bashthroughputSet
./createPlotsMiddleware.bashthroughputSet

cd ../nonshardedCase52
chmod 755 createPlots.bash
./createPlots.bash
chmod 755 createPlotsMiddleware.bash
./createPlotsMiddleware.bash
chmod 755 createPlotsMiddleware.bashavgQueueLength
./createPlotsMiddleware.bashavgQueueLength
chmod 755 createPlotsMiddleware.bashavgMemcachedResponseTimeSet
./createPlotsMiddleware.bashavgMemcachedResponseTimeSet
chmod 755 createPlotsMiddleware.bashthroughputSet
./createPlotsMiddleware.bashthroughputSet



