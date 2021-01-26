#!/bin/bash

sudo env "PATH=$PATH" mvn spring-boot:build-image
cd docker && sudo docker-compose up
