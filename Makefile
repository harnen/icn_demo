CPP_FLAGS = -std=c++14 -lpthread -I./ndn-cxx-ndn-cxx-0.5.1/build -fPIC -Ilib
LINK_FLAGS = 
DEPS = *.hpp

SFML = -lsfml-graphics -lsfml-window -lsfml-system

all: producer producer_s consumer

producer: producer.cpp  ocr.cpp tess.cpp 
	g++ -o ./build/producer producer.cpp ocr.cpp tess.cpp -llept -ltesseract -lndn-cxx -lboost_system ./lib/Letter.cpp ./lib/image_util.cpp ./lib/processing.cpp $(CPP_FLAGS) $(LINK_FLAGS) $(SFML)
	
producer_s: producer_sergi.cpp ocr.cpp tess.cpp
	 g++ -o ./build/producer_sergi producer_sergi.cpp ocr.cpp tess.cpp -llept -ltesseract -lndn-cxx -lboost_system ./lib/Letter.cpp ./lib/image_util.cpp ./lib/processing.cpp $(CPP_FLAGS) $(LINK_FLAGS) $(SFML)

consumer: consumer.cpp
	g++ -o ./build/consumer consumer.cpp -lndn-cxx -lboost_system $(CPP_FLAGS)

clean: 
	rm ./build/producer ./build/producer_sergi ./build/consumer


