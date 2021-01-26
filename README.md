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

Run webapp using :

    ./execute.sh

Per default the webapp is accessible at port 20088. You can start browsing at:

    localhost:20088/         ,or
    localhost:20088/validate

Get all WebIds of Wall of Fame:

    mvn spring-boot:run -Dstart-class=org.dbpedia.walloffame.DatabusApplication

Push WebIds of WallOfFame to DBpedia Databus:

    mvn spring-boot:run -Dstart-class=org.dbpedia.walloffame.DatabusApplication -Dspring-boot.run.arguments=databus/uniformedWebIds/$(date +%y-%m-%d) && cd databus/uniformedWebIds/ && mvn versions:set -DnewVersion=$(date +%y-%m-%d) && mvn prepare-package && mvn databus:package-export && mvn databus:deploy
