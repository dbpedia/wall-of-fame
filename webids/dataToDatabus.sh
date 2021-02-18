#!/bin/bash

cd ..
mvn spring-boot:run -Dstart-class=org.dbpedia.walloffame.DatabusApplication -Dspring-boot.run.arguments=webids/uniformedWebIds/$(date +%y-%m-%d)
cd webids/
mvn versions:set -DnewVersion=$(date +%y-%m-%d)
mvn prepare-package
mvn databus:package-export
mvn databus:deploy
cd ..
