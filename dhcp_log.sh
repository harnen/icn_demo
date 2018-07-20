#! /bin/bash
# just for testing
Log=/home/pi/icn_demo/dnsmasqscript.log
date >> $Log
echo "Param1 = $1" >> $Log
echo "Param2 = $2" >> $Log
echo "Param3 = $3" >> $Log
echo "Param4 = $4" >> $Log
nfdc face create tcp://$3
nfdc route add prefix /$3 nexthop tcp://$3
#
