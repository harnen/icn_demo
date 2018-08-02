#! /bin/bash
# just for testing
dir=/home/pi/icn_demo
Log=$dir/dnsmasqscript.log
date >> $Log
echo "Param1 = $1" >> $Log
echo "Param2 = $2" >> $Log
echo "Param3 = $3" >> $Log
echo "Param4 = $4" >> $Log

nfdc face list > face.list
for i in $(awk '{print $1}' face.list | sed 's/^faceid=//'); do
   nfdc route remove /$3 $i
   nfdc face destroy $i
done
rm face.list
nfdc face create tcp://$3
nfdc route add prefix /$3 nexthop tcp://$3
#
