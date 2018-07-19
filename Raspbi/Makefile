CPP_FLAGS = -std=c++11 -lpthread -I./ndn-cxx-ndn-cxx-0.5.1/build -fPIC -Ilib
LINK_FLAGS = 
DEPS = *.hpp

SFML = -lsfml-graphics -lsfml-window -lsfml-system

all: producer consumer

producer: producer.cpp  ocr.cpp
	g++ -o ./build/producer producer.cpp ocr.cpp -lndn-cxx -lboost_system ./lib/Letter.cpp ./lib/image_util.cpp ./lib/processing.cpp $(CPP_FLAGS) $(LINK_FLAGS) $(SFML)
	
consumer: consumer.cpp
	g++ -o ./build/consumer consumer.cpp -lndn-cxx -lboost_system $(CPP_FLAGS)

clean: 
	rm ./build/producer ./build/consumer


