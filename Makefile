# Makefile for the ASL project.
# You can change the actions associated to each target but not the targets'
# semantics as we will use them for testing your project.

# Builds the project and produces a jar file.
.PHONY: build
build:
		ant -f build.xml
		mvn -f JobControlling/pom.xml verify

# Starts the control program
.PHONY: run
run:
		java -jar ASLJobControlling.jar

# Starts the middleware, directs logs to mw.log and writes pid to mw.pid.
.PHONY: start
start:
	./start.sh

# Stops the middleware by killing the process whose pid is in mw.pid.
.PHONY: stop
stop:
	./stop.sh

