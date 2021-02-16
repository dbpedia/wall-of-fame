package org.dbpedia.walloffame.crawling

import org.apache.jena.atlas.web.HttpException
import org.apache.jena.riot.{Lang, RDFDataMgr, RiotException, RiotNotFoundException}
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.logging.JsonLDLogger
import org.dbpedia.walloffame.logging.JsonLDLogger.logAccountException
import org.dbpedia.walloffame.uniform.WebIdUniformer
import org.dbpedia.walloffame.validation.WebIdValidator
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.slf4j.LoggerFactory

import java.io.EOFException
import java.net.{ConnectException, SocketException}


object WebIdFetcher {

  var logger = LoggerFactory.getLogger("WebIdFetcher")


  def fetchRegisteredWebIds(config: Config): Unit = {

    val vos = new VirtuosoHandler(config.virtuoso)

    println(
      """
        |Download, validate, and uniform all registered WebIds on the DBpedia Databus.
        |Accounts:""".stripMargin)

    //get all registered webIds
    val url = "https://databus.dbpedia.org/system/api/accounts"
//    val url = "./src/main/resources/accounts.ttl"
    val model = RDFDataMgr.loadModel(url, Lang.NTRIPLES)

    val stmts = model.listStatements()
    while (stmts.hasNext) {
      val stmt = stmts.nextStatement()
      val webid = stmt.getSubject.toString
      val accountURL = stmt.getObject.toString //.replaceFirst("https://databus.dbpedia.org/", "")
      println(webid)

      try {
        val model = RDFDataMgr.loadModel(webid, Lang.TURTLE)
        val result = WebIdValidator.validate(model, config.shacl.url)

        result.logResults()
        if (result.conforms()) {
          val uniformedModel = WebIdUniformer.uniform(model)
          vos.insertModel(uniformedModel, accountURL)
        } else {

        }

      } catch {
        case httpException: HttpException => {
          logAccountException(webid, httpException)
          logger.error(s"$webid: ${httpException.toString}")
        }
        case eofException: EOFException => {
          logAccountException(webid, eofException)
          logger.error(s"$webid: ${eofException.toString}")
        }
        case socketException: SocketException => {
          logAccountException(webid, socketException)
          logger.error(s"$webid: ${socketException.toString}")
        }
        case connectException: ConnectException => {
          JsonLDLogger.append(s"$webid : connection timed out\n")
          logger.error(s"$webid: Connection timed out.")
        }
        case riotNotFoundException: RiotNotFoundException => {
          JsonLDLogger.append(s"$webid : url not found\n")
          logger.error(s"$webid : url not found.")
        }
        case riotException: RiotException => {
          JsonLDLogger.append(s"$webid : ${riotException.toString}\n")
          logger.error(s"$webid : ${riotException.toString}")
        }
      }

    }
  }

}
