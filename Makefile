.PHONY: all clean test

all: Main.class

Main.class: $(wildcard *.java)
	javac *.java

clean:
	rm -f *~ *.class

test: all
	time java -ea -Xmx3G Main

