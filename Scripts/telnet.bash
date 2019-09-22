#!/bin/bash

PORT=$1

telnet localhost $PORT <<EOF
get foo

set blubs 0 900 4 
asdf

EOF
