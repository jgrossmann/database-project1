all: 
	javac -d class -classpath ./lib/*:.:class/* src/*.java
clean:
	rm -f class/*.class
