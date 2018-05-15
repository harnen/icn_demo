CPP_FLAGS = -lpthread -I. -std=c++11 -fPIC -Ilib
LINK_FLAGS = 

SFML = -lsfml-graphics -lsfml-window -lsfml-system

all: app

app: app.cpp ocr.cpp
	g++ -o app -g app.cpp ocr.cpp \
    ./lib/Letter.cpp ./lib/image_util.cpp ./lib/processing.cpp \
	$(CPP_FLAGS) $(LINK_FLAGS) $(SFML)

clean: 
	rm app

run: server
	./app

