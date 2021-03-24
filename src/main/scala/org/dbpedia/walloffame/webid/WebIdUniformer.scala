package org.dbpedia.walloffame.webid

import org.apache.jena.rdf.model.{Model, ModelFactory, ResourceFactory}
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.ConstructQueries
import org.slf4j.{Logger, LoggerFactory}

object WebIdUniformer {

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName.dropRight(1))

  def uniform(model: Model, personURL:String, excludeOptionals:Seq[String]=Seq.empty[String]): Model = {
    val constructedModel = ModelFactory.createDefaultModel()

    QueryHandler.executeConstructQuery(
      ConstructQueries.constructWebIdWithOptionals(personURL),
      model,
      constructedModel
    )

    excludeOptionals.foreach(optional => {
      val stmt = constructedModel.getProperty(ResourceFactory.createResource(personURL), ResourceFactory.createProperty(optional))
      if(stmt!=null) constructedModel.remove(stmt)
    })

//    //check for Type of Optionals
//    val optionals = QueryHandler.executeQuery(SelectQueries.getOptionals, constructedModel).head
//
//    if (!(optionals.get("img") == null || optionals.get("img").isResource)) {
//      val stmt = constructedModel.getProperty(optionals.getResource("maker"), ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/img"))
//      constructedModel.remove(stmt)
//    }
//
//    if (optionals.get("geekcode") != null) {
//      val stmt = constructedModel.getProperty(optionals.getResource("maker"), ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/geekcode"))
//      if(!stmt.getObject.isLiteral) {
//        constructedModel.remove(stmt)
//      }
//      else {
//        val dataType = stmt.getObject.asLiteral().getDatatypeURI
//        if(dataType != "http://www.w3.org/2001/XMLSchema#string" && dataType != null) {
//          constructedModel.remove(stmt)
//        }
//      }
//    }

    constructedModel
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
