#!/bin/bash

cd "$(dirname "$0")/.."
mvn spring-boot:run -Dstart-class=org.dbpedia.walloffame.DatabusApp -Dspring-boot.run.arguments=webids/uniformedWebIds/$(date +%y-%m-%d)
cd webids/
mvn versions:set -DnewVersion=$(date +%y-%m-%d)
mvn prepare-package
mvn databus:package-export
mvn databus:deploy
