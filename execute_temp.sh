#!/bin/bash

sudo env "PATH=$PATH" mvn package
java -jar target/walloffame-0.1-SNAPSHOT.jar

