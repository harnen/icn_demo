# icn_demo
The repository contains an edge computing demo for ICN'18

# OCR
We implement and evaluate a simple Optical Character Recognition (OCR) algorithm to illustrate the execution of pure programs. Our simple algorithm is initialized in the enclave with a model of each letter of the alphabet; it can then be fed with input images to detect the list of embedded letters. We implemented this example to show how our system can be used to outsource execution of pure
programs.

## Requirements
The system requires libsfml. On Ubuntu type:
`` sudo apt-get install libsfml-dev``

## Compilation

```
  cd ocr
  make
  ./app ./data/input_5_OK.png
```  
## Dnsmasq config
Just add this line to the file /etc/dnsmasq.conf with the name of the script that automatically creates a face towards the Android client in the AP.

```
dhcp-script=/home/pi/icn_demo/dhcp_log.sh

```  

## Android app
The NDN OCR Android app for the demo can be found in the following repository:

https://github.com/srene/icn_demo_Android

but also in Google Play:

https://play.google.com/store/apps/details?id=uk.ac.ucl.ndnocr
