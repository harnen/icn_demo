zypper install git
zypper install bzip2
zypper install -t pattern devel_basis
zypper install wget
zypper install cmake
zypper install elementary-cmake-modules
zypper install extra-cmake-modules
zypper install gccmakedep
zypper install gcc-obj-c++
zypper install gcc5-c++
zypper install gcc6-c++
zypper install gcc7-c++
zypper install libQt5Sql5-sqlite
zypper install libcppdb_sqlite3-0
zypper install libsqlite3-0
zypper install perl-DBD-SQLite
zypper install php5-sqlite
zypper install sqlite3
zypper install sqlite3-devel
zypper install sqliteman
zypper install libcryptopp-devel

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
wget https://sourceforge.net/projects/boost/files/boost/1.61.0/boost_1_61_0.tar.bz2
tar xvzf ndn-cxx-0.5.1.tar.gz
tar xvzf NFD-0.5.1.tar.gz
tar xvjf boost_1_61_0.tar.bz2

cd ../ndn-cxx-ndn-cxx-0.5.1
zypper install libboost_1_58_0-devel
zypper install libopenssl
zypper install libpcap-devel
zypper install libpcap1

./waf configure
./waf
sudo ./waf install

cd ../NFD-NFD-0.5.1

git clone git://github.com/zaphoyd/websocketpp.git
cd websocketpp
cmake .
sudo make install
cd ..




