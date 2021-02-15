package org.dbpedia.walloffame.crawling

import org.apache.jena.atlas.web.HttpException
import org.apache.jena.riot.{Lang, RDFDataMgr, RiotException, RiotNotFoundException}
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.logging.HtmlLogger
import org.dbpedia.walloffame.logging.HtmlLogger.logAccountException
import org.dbpedia.walloffame.uniform.WebIdUniformer
import org.dbpedia.walloffame.validation.WebIdValidator
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.slf4j.LoggerFactory

import java.io.EOFException
import java.net.{ConnectException, SocketException}


object WebIdFetcher {

  var logger = LoggerFactory.getLogger("WebIdFetcher")


  def fetchRegisteredWebIds(config: Config): Unit = {

    println(
      """
        |Download, validate, and uniform all registered WebIds on the DBpedia Databus.
        |Accounts:""".stripMargin)

    val url = "https://databus.dbpedia.org/system/api/accounts"
//    val url = "./src/main/resources/accounts.ttl"
    val model = RDFDataMgr.loadModel(url, Lang.NTRIPLES)

    val stmts = model.listStatements()
    while (stmts.hasNext) {

      val stmt = stmts.nextStatement()
      val account = stmt.getSubject.toString
      val accountName = stmt.getObject.toString.replaceFirst("https://databus.dbpedia.org/", "")
      println(account)

      try {
        val model = RDFDataMgr.loadModel(account, Lang.TURTLE)
        val result = WebIdValidator.validate(model, config.shacl.url)
        result.logResults()
        val uniformedModel = WebIdUniformer.uniform(model)
        VirtuosoHandler.insertModel(uniformedModel, config.virtuoso, accountName)
      } catch {
        case httpException: HttpException => {
          logAccountException(account, "httpException")
          logger.error("httpException")
        }
        case eofException: EOFException => {
          logAccountException(account, "httpException")
          logger.error("eofException")
        }
        case socketException: SocketException => {
          logAccountException(account, "httpException")
          logger.error(s"socketException")
        }
        case connectException: ConnectException => {
          HtmlLogger.append(s"$account : connection timed out\n")
          logger.error(s"Connection timed out for $account")
        }
        case riotNotFoundException: RiotNotFoundException => {
          HtmlLogger.append(s"$account : url not found\n")
          logger.error(s"$account : url not found.")
        }
        case riotException: RiotException => {
          HtmlLogger.append(s"$account : ${riotException.toString}\n")
          logger.error(s"riotException in $account .")
        }
      }

    }
  }

}
