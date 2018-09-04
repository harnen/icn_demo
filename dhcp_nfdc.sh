#! /bin/bash
# just for testing
#dir=/home/pi/icn_demo
#Log=$dir/dnsmasqscript.log
#date >> $Log
#echo "Param1 = $1" >> $Log
#echo "Param2 = $2" >> $Log
#echo "Param3 = $3" >> $Log
#echo "Param4 = $4" >> $Log

#echo "Param1 = $1"
#echo "Param2 = $2"
#echo "Param3 = $3"
#echo "Param4 = $4"

#nfdc face list | grep remote=tcp4://$3 > face.list
#for i in $(awk '{print $1}' face.list | sed 's/^faceid=//'); do
#   echo "3 is:" $3 "and i is:" $i
#   nfdc route remove /$3 $i
#   echo "nfdc route remove /$3 $i" >> $Log
#   echo "nfdc route remove /$3 $i"
#   nfdc face destroy $i
#   echo "nfdc face destroy $i" >> $Log
#   echo "nfdc face destroy $i"
#done
#rm face.list
#echo "3 is:" $3
#nfdc face create tcp://$3
#echo "nfdc face create tcp://$3" >> $Log
#echo "nfdc face create tcp://$3"
#nfdc route add prefix /pic/$3 nexthop tcp://$3
#echo "nfdc route add prefix /pic/$3 nexthop tcp://$3" >> $Log
#echo "nfdc route add prefix /pic/$3 nexthop tcp://$3"

#new code

# just for testing
dir=/home/pi/icn_demo
Log=$dir/dnsmasqscript.log
date >> $Log
echo "Param1 = $1" >> $Log
echo "Param2 = $2" >> $Log
echo "Param3 = $3" >> $Log
echo "Param4 = $4" >> $Log


if [ "$1"=="add" ]; then
   nfdc face list | grep remote=tcp4://$3 > face.list
   echo "faces are going to be deleted." >> $Log
   for i in $(awk '{print $1}' face.list | sed 's/^faceid=//'); do
      nfdc route remove /$3 $i
      echo "nfdc route remove /pic/$3 $i" >> $Log
      nfdc face destroy $i
      echo "nfdc face destroy $i" >> $Log
   done
   rm face.list

   nfdc face create tcp://$3
   echo "nfdc face create tcp://$3" >> $Log
   nfdc route add prefix /pic/$3 nexthop tcp://$3
   echo "nfdc route add prefix /pic/$3 nexthop tcp://$3" >> $Log

else
   nfdc face list | grep remote=tcp4://$3 > face.list
   echo "faces are going to be deleted." >> $Log
   for i in $(awk '{print $1}' face.list | sed 's/^faceid=//'); do
      nfdc route remove /$3 $i
      echo "nfdc route remove /pic/$3 $i" >> $Log
      nfdc face destroy $i
      echo "nfdc face destroy $i" >> $Log
   done
   rm face.list

fi


#

