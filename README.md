# DBpedia Wall of Fame
A SHACLOntology and several tools to attribute contributions to the DBpedia movement to individual DBpedians, i.e. give credit for their merit in a machine readable format (RDF).  

## License
Creative Commons BY 4.0 International
All contributions to this repo will be considered as freely given to DBpedia and published under CC-BY-SA

# How to join
* Databus Account with link to FOAF profile
* FOAF Profile needs to validate correctly


# Technical components in this repo

## FOAF/WebId Crawler 

## DBpedia WoF Ontology


# Prerequisites
1. Install Maven 3.3.9
2. Install Docker and Docker-Compose
    
# Build and Run

Run webapp (set port in application.yml, default=21288):

1. package jar and copy in main dir
   
   ```mvn package && cp ./target/*.jar wof.jar```

1. Start Virtuoso: 

   ```docker-compose -f docker/virtuoso/docker-compose.yml up```

2. Start Application (in new Terminal window): 

   ```java -jar wof.jar```

--------------------------------

Push WebIds of Wall of Fame to DBpedia Databus:

    java -cp wof.jar -Dloader.main=org.dbpedia.walloffame.DatabusApp org.springframework.boot.loader.PropertiesLauncher webids/uniformedWebIds/$(date +%y-%m-%d) && cd webids/ && mvn versions:set -DnewVersion=$(date +%y-%m-%d) && mvn prepare-package && mvn databus:package-export && mvn databus:deploy

--------------------------------

Virtuoso and App in one command (not working yet):

    mvn spring-boot:build-image && docker-compose -f docker/docker-compose.yml up
