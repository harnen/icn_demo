#! /bin/bash
# just for testing
dir=/home/pi/icn_demo
Log=$dir/dnsmasqscript.log
date >> $Log
echo "Param1 = $1" >> $Log
echo "Param2 = $2" >> $Log
echo "Param3 = $3" >> $Log
echo "Param4 = $4" >> $Log

nfdc face list | grep remote=tcp4://$3 > face.list
for i in $(awk '{print $1}' face.list | sed 's/^faceid=//'); do
   nfdc route remove /$3 $i
   echo "nfdc route remove /$3 $i" >> $Log
   nfdc face destroy $i
   echo "nfdc face destroy $i" >> $Log
done
rm face.list
nfdc face create tcp://$3
echo "nfdc face create tcp://$3" >> $Log
nfdc route add prefix /pic/$3 nexthop tcp://$3
echo "nfdc route add prefix /pic/$3 nexthop tcp://$3" >> $Log
#
