package org.dbpedia.walloffame.uniform

import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.dbpedia.walloffame.uniform.queries.{ConstructOptionalQueries, ConstructQueries}
import org.dbpedia.walloffame.validation.WebIdValidator
import org.slf4j.{Logger, LoggerFactory}

object WebIdUniformer {

  val logger: Logger = LoggerFactory.getLogger("validator")

  def uniform(model: Model): Model = {
    val result = WebIdValidator.validate(model)
    result.logResults()

    //    val model = RDFDataMgr.loadModel(webidFile.pathAsString)
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


    if (!construct(ConstructOptionalQueries.constructFirstName())) {
      //      logger.info(s"firstname not found for ${webidFile.name}.")
      return constructModel
    }
    if (!construct(ConstructOptionalQueries.constructGeekCode())) {
      //      logger.info(s"geekcode not found for ${webidFile.name}.")
      return constructModel
    }
    if (!construct(ConstructOptionalQueries.constructGender())) {
      //      logger.info(s"gender not found for ${webidFile.name}.")
      return constructModel
    }
    if (!construct(ConstructOptionalQueries.constructImg())) {
      //      logger.info(s"image not found for ${webidFile.name}.")
      return constructModel
    }
    if (!construct(ConstructOptionalQueries.constructName())) {
      //      logger.info(s"name not found for ${webidFile.name}.")
      return constructModel
    }
//    ConstructOptionalQueries.getClass.getMethods.foreach(method =>
//      if (!construct(method)) {
//        logger.warn(s"optional item(s) not found for ${webidFile.name}.")
//      }
//    )


    constructModel
  }

}
