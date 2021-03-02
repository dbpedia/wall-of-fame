package org.dbpedia.walloffame.uniform

import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.dbpedia.walloffame.uniform.queries.{ConstructOptionalQueries, ConstructQueries}
import org.slf4j.{Logger, LoggerFactory}

object WebIdUniformer {

  val logger: Logger = LoggerFactory.getLogger("validator")

  def uniform(model: Model, nonExistingOptionals: Array[(String, String, String)] = Array.empty[(String, String, String)]): Model = {

    val constructModel = ModelFactory.createDefaultModel()

    def construct(constructQuery: String): Boolean = {
      QueryHandler.executeConstructQuery(
        constructQuery,
        model,
        constructModel
      )
    }

    if (!construct(ConstructQueries.constructWebId())) {
      //      logger.error(s"mandatory item(s) not found for ${webidFile.name}.")
      return constructModel
    }

//    nonExistingOptionals.foreach(println(_))
//    nonExistingOptionals.filter(x => x._3.split("#|/").last == "geekcode").foreach(println(_))
//    nonExistingOptionals.filter(x => x._3.split("#|/").last == "img").foreach(println(_))

    if(!nonExistingOptionals.exists(x => x._3.split("#|/").last == "img")) {
//      println("jetzt")
//      nonExistingOptionals.foreach(println(_))
      construct(ConstructOptionalQueries.constructImg())
    }
    if(!nonExistingOptionals.exists(x => x._3.split("#|/").last == "geekcode")) {
//      println("jetzt2")
//      nonExistingOptionals.foreach(println(_))
      construct(ConstructOptionalQueries.constructGeekCode())
    }

//    val stmts = constructModel.listStatements()
//    while(stmts.hasNext) println(stmts.nextStatement())

    constructModel
  }

}
