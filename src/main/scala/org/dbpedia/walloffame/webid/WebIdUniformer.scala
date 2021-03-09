package org.dbpedia.walloffame.webid

import org.apache.jena.rdf.model.{Literal, Model, ModelFactory, ResourceFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.{ConstructQueries, SelectQueries}
import org.slf4j.{Logger, LoggerFactory}

object WebIdUniformer {

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName.dropRight(1))

  def uniform(model: Model): Model = {
    val constructedModel = ModelFactory.createDefaultModel()

    QueryHandler.executeConstructQuery(
      ConstructQueries.constructWebIdWithOptionals(),
      model,
      constructedModel
    )

    //check for Type of Optionals
    //TODO add Log
    val optionals = QueryHandler.executeQuery(SelectQueries.getOptionals, constructedModel).head
    if (!(optionals.get("img") == null || optionals.get("img").isResource)) {
      val stmt = constructedModel.getProperty(optionals.getResource("maker"), ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/img"))
      constructedModel.remove(stmt)
    }
    if (!(optionals.get("geekcode") == null)) {
      val stmt = constructedModel.getProperty(optionals.getResource("maker"), ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/geekcode"))
      if(!stmt.getObject.isLiteral) constructedModel.remove(stmt)
      else if(stmt.getObject.asLiteral().getDatatypeURI != "http://www.w3.org/2001/XMLSchema#string") constructedModel.remove(stmt)
    }

    enrichModelWithDatabusData(constructedModel)
  }

  def enrichModelWithDatabusData(model:Model):Model={

    val maker = QueryHandler.executeQuery(SelectQueries.getMakerURL(), model).head.getResource("maker").toString

    checkForRelatedDatabusAccount(maker) match {
      case None => logger.warn(s"No Dbpedia-Databus account found for $maker")
      case Some(account) =>
        val result = QueryHandler.executeQuery(SelectQueries.getDatabusUserData(maker)).head

        model.add(
          ResourceFactory.createStatement(
            ResourceFactory.createResource(maker),
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

//  def emptyResult():(Model, Result)={
//    (ModelFactory.createDefaultModel(), new Result)
//  }
//
//  def isPerson(model: Model, webidURI:String):Boolean={
//    try{
//      QueryHandler.executeQuery(SelectQueries.checkIfIsPerson(webidURI),model).head
//      true
//    } catch {
//      case noSuchElementException: NoSuchElementException => false
//    }
//  }

}
