.PHONY: build test run docker-build docker-test clean

build:
	mvn -q compile

test:
	mvn -q test

run:
	mvn -q compile exec:java

docker-build:
	docker build -t maze-escape .

docker-test: docker-build
	docker run --rm maze-escape

clean:
	mvn -q clean
