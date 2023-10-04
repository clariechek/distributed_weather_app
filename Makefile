FLAGS = -g -cp .:java-json.jar:DataUtil.java:LogUtil.java:RequestInformation.java:LamportClock.java:Response.java:Listener.java:Handler.java:WeatherEntry.java

COMPILE = javac $(FLAGS)

JAVA_FILES = $(wildcard ./*.java)

CLASS_FILES = $(JAVA_FILES:.java=.class)

all: clean $(addprefix ./, $(notdir $(CLASS_FILES)))

%.class: %.java
	$(COMPILE) $<

clean:
	rm -rf %.class
