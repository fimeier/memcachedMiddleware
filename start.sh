#!/bin/bash

# I do NOT use this file!! Just here for you


middleware_flags=

java -jar dist/middleware-YOURETHZID.jar middleware_flags &> mw.log &
echo $! > mw.pid
