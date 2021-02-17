package org.dbpedia.walloffame.crawling

import org.apache.jena.atlas.web.HttpException
import org.apache.jena.riot.{Lang, RDFDataMgr, RiotException, RiotNotFoundException}
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.logging.JsonLDLogger
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
    val jsonldLogger = new JsonLDLogger(config.log.file)

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

        if (result.conforms()) {
          val uniformedModel = WebIdUniformer.uniform(model)
          vos.insertModel(uniformedModel, accountURL)
        } else {
          result.violations.foreach(tuple=>{
            jsonldLogger.add(webid, "https://example.org/hasViolation" ,tuple._2)})
        }

      } catch {
        case httpException: HttpException => {
          logger.error(s"$webid: ${httpException.toString}")
          jsonldLogger.addException(webid, httpException)
        }
        case eofException: EOFException => {
          logger.error(s"$webid: ${eofException.toString}")
          jsonldLogger.addException(webid, eofException)
        }
        case socketException: SocketException => {
          logger.error(s"$webid: ${socketException.toString}")
          jsonldLogger.addException(webid, socketException)
        }
        case connectException: ConnectException => {
          logger.error(s"$webid: Connection timed out.")
          jsonldLogger.addException(webid, connectException)
        }
        case riotNotFoundException: RiotNotFoundException => {
          logger.error(s"$webid : url not found.")
          jsonldLogger.addException(webid, riotNotFoundException)
        }
        case riotException: RiotException => {
          logger.error(s"$webid : ${riotException.toString}")
          jsonldLogger.addException(webid, riotException)
        }
      }

    }

    jsonldLogger.writeOut()
  }



}
