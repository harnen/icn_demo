#!/bin/bash

cd ndn-cxx-ndn-cxx-0.5.1/
./waf configure --prefix=../build/ --destdir=../build/ && ./waf
cd ..
mkdir build
make
