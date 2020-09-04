VERSION=HEAD-SNAPSHOT

.PHONY: build test release release-local

build:
	sbt +clean +compile

test:
	sbt 'set scalaVersion := "2.12.12"' test
	sbt 'set scalaVersion := "2.13.3"' test

release: test
	sbt 'set version := "$(VERSION)"' +publishSigned

# Do a +publishLocal if you need it in your local .ivy2 repo
release-local:
	sbt 'set version := "$(VERSION)"' +publishM2
