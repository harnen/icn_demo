#!/bin/bash

cd ndn-cxx-ndn-cxx-0.5.1/
./waf configure --boost-libs=/usr/lib/arm-linux-gnueabihf --prefix=../build/ --destdir=../build/ && ./waf
cd ..
mkdir build
make
