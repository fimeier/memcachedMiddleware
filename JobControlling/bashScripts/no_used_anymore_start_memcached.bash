#!/bin/bash

sudo service memcached stop


cd ~/automato/

PORT=$1

memcached -t 1 -p $PORT > outputMemcached 2>errorMemcached 
