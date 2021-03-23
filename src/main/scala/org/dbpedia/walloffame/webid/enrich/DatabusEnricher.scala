package org.dbpedia.walloffame.webid.enrich

import org.apache.jena.rdf.model.{Literal, Model, ResourceFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.SelectQueries
import org.dbpedia.walloffame.webid.WebIdUniformer.logger

object DatabusEnricher {

  def enrichModelWithDatabusData(model:Model, personURL:String):Model={

    //    println(s"MAKER: $maker")
    checkForRelatedDatabusAccount(personURL) match {
      case None => logger.warn(s"No Dbpedia-Databus account found for $personURL")
      case Some(account) =>
        val result = QueryHandler.executeQuery(SelectQueries.getDatabusUserData(personURL)).head

        model.add(
          ResourceFactory.createStatement(
            ResourceFactory.createResource(personURL),
            ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/account"),
            ResourceFactory.createResource(account)
          )
        )

        def addDecimalLiteralToModel(prop:String, value:Literal) ={
          val ontology = "http://dbpedia.org/ontology/"
          model.add(
            ResourceFactory.createStatement(
              ResourceFactory.createResource(account),
              ResourceFactory.createProperty(ontology+prop),
              value
            )
          )
        }

        val numVersions = "numVersions"
        var value = {
          if (result.getLiteral(numVersions) != null) result.getLiteral(numVersions)
          else ResourceFactory.createTypedLiteral(0)
        }
        addDecimalLiteralToModel(numVersions, value)

        val numArtifacts = "numArtifacts"
        value = {
          if (result.getLiteral(numArtifacts) != null) result.getLiteral(numArtifacts)
          else ResourceFactory.createTypedLiteral(0)
        }
        addDecimalLiteralToModel(numArtifacts, value)

        val uploadSize = "uploadSize"
        value = {
          if(result.getLiteral(uploadSize) != null) {
            val uploadSizeAsMB = result.getLiteral(uploadSize).getLong  / 1024 / 1024
            ResourceFactory.createTypedLiteral(uploadSizeAsMB)
          } else {
            ResourceFactory.createTypedLiteral(0)
          }
        }
        addDecimalLiteralToModel(uploadSize, value)
    }

    model
  }

  def checkForRelatedDatabusAccount(webid:String):Option[String]={
    val model = RDFDataMgr.loadModel("https://databus.dbpedia.org/system/api/accounts", Lang.NTRIPLES)
    val solutions = QueryHandler.executeQuery(SelectQueries.checkForRelatedDatabusAccount(webid),model)

    if(solutions.nonEmpty) Option(solutions.head.getResource("acc").toString)
    else None
  }


}