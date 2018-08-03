#!/bin/sh -e
#
# rc.local
#
# This script is executed at the end of each multiuser runlevel.
# Make sure that the script will "exit 0" on success or any other
# value on error.
#
# In order to enable or disable this script just change the execution
# bits.
#
# By default this script does nothing.

# Print the IP address
#_IP=$(hostname -I) || true
#if [ "$_IP" ]; then
#  printf "My IP address is %s\n" "$_IP"
#fi

#iptables-restore < /etc/iptables.ipv4.nat  

#nfd-stop
#sleep 5
#nfd-start
#sleep 5
nfdc face create tcp://raspberryW1
sleep 2
nfdc face create tcp://raspberryW2
sleep 2
nfdc route add prefix /exec/OCR nexthop tcp://raspberryW1
sleep 2
nfdc route add prefix /exec/OCR nexthop tcp://raspberryW2

exit 0
