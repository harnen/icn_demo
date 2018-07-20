sudo apt-get update
sudo apt-get install git
sudo apt-get install bzip2
sudo apt-get install build-essential
sudo apt-get install wget
sudo apt-get install cmake
sudo apt-get install elementary-cmake-modules
sudo apt-get install extra-cmake-modules
sudo apt-get install gccmakedep
sudo apt-get install gcc-obj-c++
sudo apt-get install gcc5-c++
sudo apt-get install gcc6-c++
sudo apt-get install gcc7-c++
sudo apt-get install libQt5Sql5-sqlite
sudo apt-get install libcppdb_sqlite3-0
sudo apt-get install libsqlite3-0
sudo apt-get install perl-DBD-SQLite
sudo apt-get install php5-sqlite
sudo apt-get install sqlite3
sudo apt-get install sqlite3-dev
sudo apt-get install sqliteman
sudo apt-get install libcryptopp-dev

sudo swapon --show
free -h
df -h
sudo fallocate -l 1.5G /swapfile
ls -lh /swapfile
sudo chmod 600 /swapfile
ls -lh /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
sudo swapon --show
free -h
sudo cp /etc/fstab /etc/fstab.bak
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
sudo sysctl vm.vfs_cache_pressure=60


wget https://github.com/named-data/ndn-cxx/archive/ndn-cxx-0.5.1.tar.gz
wget https://github.com/named-data/NFD/archive/NFD-0.5.1.tar.gz
#wget https://sourceforge.net/projects/boost/files/boost/1.61.0/boost_1_61_0.tar.bz2
tar xvzf ndn-cxx-0.5.1.tar.gz
tar xvzf NFD-0.5.1.tar.gz
#tar xvjf boost_1_61_0.tar.bz2
#cd boost_1_61_0
#./bootstrap.sh
#./b2

cd ../ndn-cxx-ndn-cxx-0.5.1
sudo apt-get install libopenssl
sudo apt-get install libpcap-devel
sudo apt-get install libpcap1

./waf configure
./waf
sudo ./waf install

cd ../NFD-NFD-0.5.1

git clone git://github.com/zaphoyd/websocketpp.git
cd websocketpp
cmake .
sudo make install
cd ..




