# asl-fall19-project (fimeier@student.ethz.ch)
# Content
- [asl-fall19-project (fimeier@student.ethz.ch)](#asl-fall19-project-fimeierstudentethzch)
- [Content](#content)
- [TODO](#todo)
- [Scratchpad](#scratchpad)
- [loadIt procedure](#loadit-procedure)
- [Runcommands](#runcommands)
- [Commands (azure?)](#commands-azure)
- [Commands for Job Controlling](#commands-for-job-controlling)
- [memcache configuration](#memcache-configuration)
- [configAzureHostIP and Setup](#configazurehostip-and-setup)
- [memtier_benchmark --help](#memtierbenchmark---help)

# TODO

* ~~prüfe loadIt... sollte es eigentlich intelligent laden... prüfe ob alle keys geladen (statistik wert?)~~
    * ~~passe Paremeter für load an~~
* Benchmarks
    * prüfe ob logfiles augeschalten sind auf azure für memcached bzw vv option
    * histogram file kopieren deaktivieren
    * ~~implemetiere tests insbesonder wahl data-size parameter~~ erledigt
    * ~~Prüfe ob es second Instanz gibt bei Tests/Benchmarks~~ JA!!! es gibt 1 bis 3
    * ~~prüfe ob meine Memtierparameter noch stimmen (#instanzen, #threads.... die automatische Rechnung!!!!)~~ erledigt
      * ~~wird für 2.1 / 2.2 nicht mehr stimmen, da dort 1-Instanz<->3-CT und 3-Instanzen<->1-CT verglichen werden~~
      * ~~zuvor hatte ich dass CT kummuliert über alle Instanzen pro Client immer 2 ist (stimmt für restliche Experimente noch immer!!!)~~
    * implementiere Performance Messung (HW) für Client/Server/MW



* DataProcessing
    * wichtig: allenfalls gibt es probleme, da ich die classe nicht richtig resete
    * prüfe ob bei den statistiken die "alte variante" überhaupt noch benötigt wird
      * ich vermute ich habe alles ersetzt durch die Konfig files welche noch im alten git sind



# Scratchpad
no pw login local
sudo visudo
fimeier ALL=(ALL) NOPASSWD:ALL

test memcached auf vm-yoga port 12666

# loadIt procedure
* add value size in procedure BZW data-size=4096 jeweils anpassen
--test-time=3 willkürlich

* localtest write
memtier_benchmark --protocol=memcache_text --key-maximum=10000 --server=localhost --test-time=3 --clients=50 --json-out-file=json.txt --run-count=1 --expiry-range=2000000-2000001 --threads=4 --data-size=4096 --port=12666 --key-pattern=P:P --ratio=1:0 

* localtest read
memtier_benchmark --protocol=memcache_text --json-out-file=json.txt --run-count=1 --expiry-range=2000000-2000001 --threads=4 --data-size=4096 --port=12666 --key-maximum=10000 --server=localhost --test-time=6 --ratio=0:1 --clients=50 

* write
--protocol=memcache_text --key-maximum=10000 --server=10.0.0.32 --test-time=3 --clients=50 --json-out-file=json.txt --run-count=1 --expiry-range=2000000-2000001 --threads=4 --data-size=4096 --port=12444 --key-pattern=P:P --ratio=1:0 

--protocol=memcache_text --key-maximum=10000 --server=10.0.0.31 --test-time=3 --clients=50 --json-out-file=json.txt --run-count=1 --expiry-range=2000000-2000001 --threads=4 --data-size=4096 --port=12333 --key-pattern=P:P --ratio=1:0 

--protocol=memcache_text --key-maximum=10000 --server=10.0.0.33 --test-time=3 --clients=50 --json-out-file=json.txt --run-count=1 --expiry-range=2000000-2000001 --threads=4 --data-size=4096 --port=12555 --key-pattern=P:P --ratio=1:0

* read
--protocol=memcache_text --json-out-file=json.txt --run-count=1 --expiry-range=2000000-2000001 --threads=4 --data-size=4096 --port=12333 --key-maximum=10000 --server=10.0.0.31 --test-time=6 --ratio=0:1 --clients=50 


# Runcommands 
memtier_benchmark --port=11212 --protocol=memcache_text --json-out-file=json.txt -d4096 -x1

memtier_benchmark --server=server1 --port=12333 --protocol=memcache_text

ssh server1 "sudo service memcached restart"


java -jar middleware-fimeier.jar -l 127.0.0.1 -p 11212 -t 8 -s false -m 127.0.0.1:12333

memcached -p 12333


java -jar middleware-fimeier.jar -l 127.0.0.1 -p 11212 -t 8 -s false -m 10.0.0.31:12333

10.0.0.21 11212

public static String[] middlewareIPs = new String[] {"10.0.0.21","10.0.0.22"};
public static String[] middlewarePorts = new String[] {"11212","12212"};


public static String[] serverIPs = new String[] {"10.0.0.31","10.0.0.32","10.0.0.33"};
public static String[] serverPorts = new String[] {"12333","12444","12555"};

# Commands (azure?)
--protocol=memcache_text

memtier_benchmark --port=12333 --protocol=memcache_text --ratio=0:1 --expiry-range=9999-10000 --key-maximum=1000

memcached -p 12333 -vv





# Commands for Job Controlling
screen -S jobcontrolling
java -jar ~/asl-fall18-project/dist/ASLJobControlling.jar > outputASLJOB 2>errorASLJOB 

screen -dR jobcontrolling


ssh 10.0.0.31
screen -S experiment
cd automato; ./start_memcached.bash 12333

ssh 10.0.0.32
screen -S experiment
cd automato; ./start_memcached.bash 12444


ssh 10.0.0.33
screen -S experiment
cd automato; ./start_memcached.bash 12555

# memcache configuration

10.0.0.31:12333
10.0.0.32:12444
10.0.0.33:12555

Config-file: **/etc/memcached.conf**

-d

logfile ~/automato/memcached.log

-m 64

-t 1

-p 12xxx

-u memcache


Step 2 – launch service
#!/bin/bash
ssh azureuser@<dns name> "sudo service memcached restart"



# configAzureHostIP and Setup

admin-user: fimeier

wsl@p52.local
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC1QWSwxdTv69v/sbdUNLnIT/KkqtOt1wI/ERIPY155vhukkr71rNwuAJmWLiIcLQEBy+QpBKHpxjIRJo1c1K5Zu1BGCRJ6MJIZmqDp1Ezozs591A5GUkYY71w2Lv8v6ZX6nDqiqIlaiiTpgDhqu46JGu0Ui5zkbpEhW6btGcrLMcL+nwI+XJLRw901w98dFLpj1gy+PxaxYXCC289GPHRQIjcb6hryzV/29EhHLgS28sFWeaPFe6RCDUWIQ5jeUvBVPt01QgYAVLaos44afEyKgbhxY3wiUB0CCQe/xsOdXaWGxTKsIIv1WFxoaoyhna4zw60bmeQrCQb2zXX7OTHBdjZl59ia/rodr9+r4I+P6hoWZ/ID6yry7GUnEakO9e3TXW2BePt5OHqvuMAwwFyBMkHgPASZhJj2gEjWTG5h58XFYmb1pS2H2tzDX1EzMedMcyn4UiRbf6l45Qk4lPF0J53RfyKRGWMyBPuj/FnssC6StxxG5Hgdb+MNWFMFKDArRnkDV3KefpaUkkgoAaamIpT0BREgHCgmANj40M3QMnFQ2LTlUb3BACbQCFnLt7bhoPZdItADrQPH5FzCbHaofHvrQ1xRgdIoK5lQ+0MXGYBTcvc6hCe7VTrYnn1s5Rltgsm/e3xMSklN8eogfUoBcJAt+XS+nwwTGfUNMvIw4Q== wsl@p52.local

Middleware1@Hyper-V
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQCr0fw2kQ9DezBgKMvZKMbCipwyckT+Tas5byOB71umRyX2gaqQv1Po23q1ZL6+L7231mmGXHJdI2RZ74UAAQ4Tb2hcfvbqg1eyjIckIo2FT1mWzRv6NUiiG79Ceuw7rDnzFAbNZYODMf4CQACpPQEr4mJyg4tD4UY6/IE5ol2Io69r2/hHY4Iq+QhGPZXlBZjjTXQnNgcxwUFE5apmx5mAley1hXfgywNZIlrY2SBRCZVadvM989FURr19tWxBAvEl8kfPNWNTCBgDENJocYlAoAfOg6UI3hjVaO1x+4Xz1Wh0QXpXQrp1r4L4GBD+drXHa87Uvorg80TBnRBa9gt5lBypiUNhUJozl9glV2NnhsKnaQ1V6oneZnT7wBAzfD+PI5DXvqoeveRFYxF+Q4E8YgImI0BHWUUMnZ6aG+3LF9nKX8RKdmfqGKcS7Lx3mZ6anKGn7awkHIpUKH6t9da6f3jF3x1ZrUUuK5JO5pbORevFE9JUuYPrIRqb3NqCc529ooPJPB3xMmLk3sj0XGQjiZjjAeF8dvOfvhAVQLkIsGfpXv3ERgY119EJ++ngsWEocJC5D9qXNvOtpa2X4WITrLiN2gWjHzN+bU4o058GRELjwa0hmtQoGdId/FtYcwho2dB4BkSxvNp+GjAzYJkiLMon/2Bco7xRGXtL1FwxiQ== middleware1@asl.loca

Middleware1@azure
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDIxConFFDefmFmNMdEQkitzyQDeMIgxWuJUM8Mw9VbuaIwnv40I6YVeGcgIGRpinNLEafkEdgnWNZTbMFhtEGaXLGXMZWNA6hHpDiDq4cMJKqtwtS9VpHdH1NguMIdarhsBrvlMaUKXtkqqS3H7w7x6J3657kL6vT6x70I9nLWSdfy6iDPtCEpEyeNKS1wnAqkkDu1WtMjHC/GesYKcKMxIrZStzd0jNN4MESKuFXuyWSCvjrVzrKPtUMXjlAR8TDb89IFN37NGtrS/MZRm8v76C8wEc2ls/LiMdTdWPA8f1CwqtqLQQLClcVSN5eJOcYx93iNho7REFyw6CGi9pFyiSvFWFQfL2cNga338pK8w4mvMHd+4CtA/nh5RhwTQWjJKNpYnzYgXbYqamANnn71KEOFjc8o+uqvdPIRujFeRKY6CrbzGmZRAOpnP8x1LI3ngceVTmp+7DP02RHmzovdcyHssjxLyaaw/4PU4peiFhVF6AYfLB1QNL58RsnQimFr5Eufu0OFCoMEm1j1Af9QUiljTLXEeUEgTFJiO4Go8jl78rk8620992XaovslE6ZrZmWn38KyaHpBM5ZTADAOQiqgH4mFOBo5LQ4nIrgul0HE1RXYGAWnedx9bdxaXjRpYXfTMcaff7N8gUR5r0JZvlsZijk6Ui8QL/K2bI5TVQ== Middleware1@azure

Client1 sshNIC1 10.0.0.11 storetcpprteunt27ksshpublicip1.westeurope.cloudapp.azure.com

Client2 sshNIC2 10.0.0.12 storetcpprteunt27ksshpublicip2.westeurope.cloudapp.azure.com

Client3 sshNIC3 10.0.0.13 storetcpprteunt27ksshpublicip3.westeurope.cloudapp.azure.com


Middleware1 sshNIC4 10.0.0.21 storetcpprteunt27ksshpublicip4.westeurope.cloudapp.azure.com

Middleware2 sshNIC5 10.0.0.22 storetcpprteunt27ksshpublicip5.westeurope.cloudapp.azure.com

Server1 sshNIC6 10.0.0.31 storetcpprteunt27ksshpublicip6.westeurope.cloudapp.azure.com

Server2 sshNIC7 10.0.0.32 storetcpprteunt27ksshpublicip7.westeurope.cloudapp.azure.com

Server3 sshNIC8 10.0.0.33 storetcpprteunt27ksshpublicip8.westeurope.cloudapp.azure.com





**************************ALT*****************
azure installierte keys
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC1QWSwxdTv69v/sbdUNLnIT/KkqtOt1wI/ERIPY155vhukkr71rNwuAJmWLiIcLQEBy+QpBKHpxjIRJo1c1K5Zu1BGCRJ6MJIZmqDp1Ezozs591A5GUkYY71w2Lv8v6ZX6nDqiqIlaiiTpgDhqu46JGu0Ui5zkbpEhW6btGcrLMcL+nwI+XJLRw901w98dFLpj1gy+PxaxYXCC289GPHRQIjcb6hryzV/29EhHLgS28sFWeaPFe6RCDUWIQ5jeUvBVPt01QgYAVLaos44afEyKgbhxY3wiUB0CCQe/xsOdXaWGxTKsIIv1WFxoaoyhna4zw60bmeQrCQb2zXX7OTHBdjZl59ia/rodr9+r4I+P6hoWZ/ID6yry7GUnEakO9e3TXW2BePt5OHqvuMAwwFyBMkHgPASZhJj2gEjWTG5h58XFYmb1pS2H2tzDX1EzMedMcyn4UiRbf6l45Qk4lPF0J53RfyKRGWMyBPuj/FnssC6StxxG5Hgdb+MNWFMFKDArRnkDV3KefpaUkkgoAaamIpT0BREgHCgmANj40M3QMnFQ2LTlUb3BACbQCFnLt7bhoPZdItADrQPH5FzCbHaofHvrQ1xRgdIoK5lQ+0MXGYBTcvc6hCe7VTrYnn1s5Rltgsm/e3xMSklN8eogfUoBcJAt+XS+nwwTGfUNMvIw4Q== wsl@p52.local



ssh-keygen -o -t rsa -C "wsl@p52.local" -b 4096


------>Middleware1 erstelle pk und benutze diesen für git und adde ihn pro vm im azureportal (reset pw...)
dann direkt ssh login mglich.... ssh-copy-id war auf hyperv nötig
ssh-keygen -o -t rsa -C "middleware1@asl.local" -b 4096
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDEUZz9xXrppLKtmCwUUIkRLPKiv5QE1MFdj+eMjl5UOhuLlA5xag0BTWMLhybqIEgZj6ft3XUA6W6RR+8znhUu62t5CKqh0dwwk66erpRE/JAfX4TwTchJSJwak8c1SKukEyxHVfEcKytvolYf6zzJnv9nsOzC0LeRRkpNzmPXnec4j6VRnhUM5LyTrkzlYL8RQpW/tZhZO4FuQnkqFcST7fQHJA15I/w0D0aFLBasd4sDO5UFDbU99rJxYPRRwVmh50MJ1hzytoxM1STyLfFg47Hzn74VvlIUpHqTEeqc7JQ2HJcyea3natN2zDtzuHbphjk+bTG3UiXmEuvL2bK+bQMGysD8Dajv6DOvwKpF8eBFpKUZpCB1k9Zn8ctN3fN9hmmCkg7MVMNjCdHb4axKYLm5v0/VG2+RYHWAAb68/jhtVHfu6KEHjXzQI5zFZyTpErZiJEUozsLaitP59n2X7DwsQGTZQDgwhDKpHQPUp1M25NQ9WqCWNe03JegVZ2Ry3bodyPHixBId99425RodUYLZ3OlHFeVBbCwLxbMjjMoh2jtsDg62KrN8AC4Hz9GK8/PnswfUA6SZmfASNWyy9RwfkI/hTj57N+hdc5N4e1Ph4Kf+0+l3fdPDb1U/ZuC96aEx7Xg9ZlMb25wbgeZygpeAqs50ry/eeyzKKd9Ciw== middleware1@asl.local
xclip -sel clip < ~/.ssh/id_rsa.pub

//damit man sich auf einem anderen host ohne pw einloggen kann
ssh-copy-id -i id_rsa.pub 10.0.0.11




//Commands
java -jar EclipseMiddleware.jar -l 10.0.0.21 -p 11212 -t 8 -s false -m 10.0.0.31:12333 10.0.0.32:12444 10.0.0.33:12555
memtier_benchmark --server=10.0.0.31 --port=12333 --protocol=memcache_text --json-out-file=json.txt -d4096 -x1 --expiry-range=9999-10000 --key-maximum=100 -x1


!!!!!!!!!!!!!!!!!!Memcached angepasst... wird dienst gestoppt?
Server 1,2,3 (habe auf server 3 memtier_benchmarkt auch installiert
sudo apt-get update
sudo apt-get install memcached git unzip ant openjdk-8-jdk build-essential autoconf automake libpcre3-dev libevent-dev pkg-config zlib1g-dev
sudo service memcached stop


sudo apt-get install git unzip build-essential autoconf automake libpcre3-dev libevent-dev pkg-config zlib1g-dev

!!!Prüfe ob jeweils überall die Selbe Version Installiert wird
wget https://memcached.org/files/memcached-1.5.18.tar.gz
tar -zxf memcached-1.5.18.tar.gz
cd memcached-1.5.18
./configure --prefix=/usr/local/memcached
make && sudo make install
sudo service memcached stop
/usr/local/memcached/bin/memcached


Middleware 1,2
sudo apt-get update
sudo apt-get install git unzip ant openjdk-8-jdk build-essential autoconf automake libpcre3-dev libevent-dev pkg-config zlib1g-dev

Client 1,2,3
memtier_benchmark version 1.2.17

sudo apt-get install git unzip build-essential autoconf automake libpcre3-dev libevent-dev pkg-config zlib1g-dev libssl-dev
wget https://github.com/RedisLabs/memtier_benchmark/archive/1.2.17.zip
unzip 1.2.17.zip; cd memtier_benchmark-1.2.17; autoreconf -ivf
./configure
make; sudo make install



# memtier_benchmark --help
Usage: memtier_benchmark [options]
A memcache/redis NoSQL traffic generator and performance benchmarking tool.

Connection and General Options:
  -s, --server=ADDR              Server address (default: localhost)
  -p, --port=PORT                Server port (default: 6379)
  -S, --unix-socket=SOCKET       UNIX Domain socket name (default: none)
  -P, --protocol=PROTOCOL        Protocol to use (default: redis).  Other
                                 supported protocols are memcache_text,
                                 memcache_binary.
  -a, --authenticate=CREDENTIALS Authenticate to redis using CREDENTIALS, which depending
                                 on the protocol can be PASSWORD or USER:PASSWORD.
  -x, --run-count=NUMBER         Number of full-test iterations to perform
  -D, --debug                    Print debug output
      --client-stats=FILE        Produce per-client stats file
      --out-file=FILE            Name of output file (default: stdout)
      --json-out-file=FILE       Name of JSON output file, if not set, will not print to json
      --show-config              Print detailed configuration before running
      --hide-histogram           Don't print detailed latency histogram
      --cluster-mode             Run client in cluster mode
      --help                     Display this help
      --version                  Display version information

Test Options:
  -n, --requests=NUMBER          Number of total requests per client (default: 10000)
                                 use 'allkeys' to run on the entire key-range
  -c, --clients=NUMBER           Number of clients per thread (default: 50)
  -t, --threads=NUMBER           Number of threads (default: 4)
      --test-time=SECS           Number of seconds to run the test
      --ratio=RATIO              Set:Get ratio (default: 1:10)
      --pipeline=NUMBER          Number of concurrent pipelined requests (default: 1)
      --reconnect-interval=NUM   Number of requests after which re-connection is performed
      --multi-key-get=NUM        Enable multi-key get commands, up to NUM keys (default: 0)
      --select-db=DB             DB number to select, when testing a redis server
      --distinct-client-seed     Use a different random seed for each client
      --randomize                random seed based on timestamp (default is constant value)

Arbitrary command:
      --command=COMMAND          Specify a command to send in quotes.
                                 Each command that you specify is run with its ratio and key-pattern options.
                                 For example: --command="set __key__ 5" --command-ratio=2 --command-key-pattern=G
                                 To use a generated key or object, enter:
                                   __key__: Use key generated from Key Options.
                                   __data__: Use data generated from Object Options.
      --command-ratio            The number of times the command is sent in sequence.(default: 1)
      --command-key-pattern      Key pattern for the command (default: R):
                                 G for Gaussian distribution.
                                 R for uniform Random.
                                 S for Sequential.
                                 P for Parallel (Sequential were each client has a subset of the key-range).

Object Options:
  -d  --data-size=SIZE           Object data size (default: 32)
      --data-offset=OFFSET       Actual size of value will be data-size + data-offset
                                 Will use SETRANGE / GETRANGE (default: 0)
  -R  --random-data              Indicate that data should be randomized
      --data-size-range=RANGE    Use random-sized items in the specified range (min-max)
      --data-size-list=LIST      Use sizes from weight list (size1:weight1,..sizeN:weightN)
      --data-size-pattern=R|S    Use together with data-size-range
                                 when set to R, a random size from the defined data sizes will be used,
                                 when set to S, the defined data sizes will be evenly distributed across
                                 the key range, see --key-maximum (default R)
      --expiry-range=RANGE       Use random expiry values from the specified range

Imported Data Options:
      --data-import=FILE         Read object data from file
      --data-verify              Enable data verification when test is complete
      --verify-only              Only perform --data-verify, without any other test
      --generate-keys            Generate keys for imported objects
      --no-expiry                Ignore expiry information in imported data

Key Options:
      --key-prefix=PREFIX        Prefix for keys (default: "memtier-")
      --key-minimum=NUMBER       Key ID minimum value (default: 0)
      --key-maximum=NUMBER       Key ID maximum value (default: 10000000)
      --key-pattern=PATTERN      Set:Get pattern (default: R:R)
                                 G for Gaussian distribution.
                                 R for uniform Random.
                                 S for Sequential.
                                 P for Parallel (Sequential were each client has a subset of the key-range).
      --key-stddev               The standard deviation used in the Gaussian distribution
                                 (default is key range / 6)
      --key-median               The median point used in the Gaussian distribution
                                 (default is the center of the key range)

WAIT Options:
      --wait-ratio=RATIO         Set:Wait ratio (default is no WAIT commands - 1:0)
      --num-slaves=RANGE         WAIT for a random number of slaves in the specified range
      --wait-timeout=RANGE       WAIT for a random number of milliseconds in the specified range (normal 
                                 distribution with the center in the middle of the range)

