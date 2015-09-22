all: 
	javac -d . -classpath ./lib/*:. *.java
clean:
	rm -f *.class
