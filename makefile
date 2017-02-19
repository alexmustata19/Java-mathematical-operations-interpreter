.PHONY: build clean run

run: build
	java -Xmx512m Interpretor.MainClass ${ARGS}
build: 
	javac -d . src/Interpretor/*.java 
clean:
	rm -rf Interpretor/*.class
