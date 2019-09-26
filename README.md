# asl-fall19-project (fimeier@student.ethz.ch)
# Content
- [asl-fall19-project (fimeier@student.ethz.ch)](#asl-fall19-project-fimeierstudentethzch)
- [Content](#content)
- [Runcommands](#runcommands)
- [Commands (azure?)](#commands-azure)
- [Commands for Job Controlling](#commands-for-job-controlling)
- [memcache configuration](#memcache-configuration)
- [configAzureHostIP and Setup](#configazurehostip-and-setup)




nopwlogin local
sudo visudo
fimeier ALL=(ALL) NOPASSWD:ALL

# Runcommands 
memtier_benchmark --port=11212 --protocol=memcache_text --json-out-file=json.txt -d4096 -x1


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




