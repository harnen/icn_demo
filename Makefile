CPP_FLAGS = -lpthread -I. -std=c++11 -fPIC -Ilib
LINK_FLAGS = 

SFML = -lsfml-graphics -lsfml-window -lsfml-system

all: producer consumer

producer: producer.cpp  ocr.cpp
	g++ -o producer producer.cpp ocr.cpp -lndn-cxx -lboost_system \
	./lib/Letter.cpp ./lib/image_util.cpp ./lib/processing.cpp \
	$(CPP_FLAGS) $(LINK_FLAGS) $(SFML)
	
consumer: consumer.cpp
	g++ -o consumer consumer.cpp -lndn-cxx -lboost_system

clean: 
	rm producer consumer


