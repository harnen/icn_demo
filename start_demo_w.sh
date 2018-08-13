#!/bin/bash
nfd-start
sleep 5
nfdc face create tcp://192.168.49.1
sleep 2
nfdc route add prefix /pic nexthop tcp://192.168.49.1
/home/pi/icn_demo/build/producer_sergi
