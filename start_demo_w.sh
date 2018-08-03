#!/bin/bash
nfd-start
nfdc face create tcp://192.168.49.1
nfdc route add prefix /pic nexthop tcp://192.168.49.1
/home/pi/build/producer_sergi
