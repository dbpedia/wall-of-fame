#!/bin/bash

cd "$(dirname "$0")/.."
java -cp wof.jar -Dloader.main=org.dbpedia.walloffame.DatabusApp org.springframework.boot.loader.PropertiesLauncher webids/uniformedWebIds/$(date +%y-%m-%d)
cd webids/
mvn versions:set -DnewVersion=$(date +%y-%m-%d)
mvn prepare-package
mvn databus:package-export
mvn databus:deploy
