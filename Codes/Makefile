JAVA=java
JAVAC=javac
LIBS=dist/lib
SRC=classes/

classpath:= \
	$(LIBS)/log4j.jar \
	$(LIBS)/RXTXcomm.jar \
	$(LIBS)/mysql.jar \
	$(SRC)
empty:=
space:= $(empty) $(empty)
classpath:=     $(subst $(space),:,$(classpath))

ifneq ($(CLASSPATH),)
   classpath:= $(CLASSPATH):$(classpath)
endif
export CLASSPATH:=$(classpath)

all:
	echo "Try make server for compiling the server\n make run for running the server\n make patient for compiling and programming the patient_bot \n make bot for compiling and burning bot\n"

server:
	if [ -e classes ]; then rm -rf classes; fi
	mkdir classes
	find src/SERVER_CODE/ -name *.java | xargs javac -cp $(classpath) -d classes/

run:
	java -cp $(classpath) pss.server.RequestHandler	

patient:
	make -f src/BOT_CODE/Makefile_IR
	make program -f src/BOT_CODE/Makefile_IR
	
bot:
	make -f src/BOT_CODE/Makefile_BOT
	make program -f src/BOT_CODE/Makefile_BOT

bot2:
	make -f src/BOT_CODE/Makefile_BOT2
	make program -f src/BOT_CODE/Makefile_BOT2

erase_bot:
	make erase -f src/BOT_CODE/Makefile_BOT

doc:
	xdg-open documentation/html/index.html

clean:
	make clean -f src/BOT_CODE/Makefile_BOT
	make clean -f src/BOT_CODE/Makefile_BOT2
	make clean -f src/BOT_CODE/Makefile_IR
	if [ -e classes ]; then rm -rf classes; fi
