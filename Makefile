#Directory for compiled classes
BIN = ./bin/

# Directory for source files
SRC = ./src/*

FLAGS = -g -d $(BIN) -cp .:./lib/java-json.jar:DataUtil.java:LogUtil.java:RequestInformation.java:LamportClock.java:Response.java:Listener.java:Handler.java:WeatherEntry.java $(SRC)

COMPILE = javac $(FLAGS)

JAVA_FILES = $(wildcard $(SRC)*.java)

CLASS_FILES = $(JAVA_FILES:.java=.class)

all: clean $(addprefix $(BIN), $(notdir $(CLASS_FILES)))

$(BIN)%.class: $(SRC)%.java
	@mkdir -p $(BIN)
	$(COMPILE) $<

clean:
	rm -rf $(BIN)*
