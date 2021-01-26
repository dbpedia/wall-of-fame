package org.dbpedia.walloffame.crawling

import org.apache.jena.atlas.web.HttpException

import java.io.{ByteArrayOutputStream, EOFException, FileOutputStream, IOException}
import java.net.{ConnectException, SocketException}
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.{Lang, RDFDataMgr, RiotException, RiotNotFoundException}
import org.dbpedia.walloffame.VosConfig
import org.dbpedia.walloffame.uniform.WebIdUniformer
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.slf4j.LoggerFactory


object WebIdFetcher {

  var logger = LoggerFactory.getLogger("WebIdFetcher")

  def fetchRegisteredWebIds(virtuosoConfig: VosConfig): Unit = {

    println(
      """
        |Download, validate, and uniform all registered WebIds on the DBpedia Databus.
        |Accounts:""".stripMargin)

    val url = "https://databus.dbpedia.org/system/api/accounts"
    val model = RDFDataMgr.loadModel(url, Lang.NTRIPLES)

    val stmts = model.listStatements()
    while (stmts.hasNext) {

      val stmt = stmts.nextStatement()
      val account = stmt.getSubject.toString
      val accountName = stmt.getObject.toString.replaceFirst("https://databus.dbpedia.org/", "")
      println(account)

      try {
        val uniformedModel = WebIdUniformer.uniform(RDFDataMgr.loadModel(account, Lang.TURTLE))
        VirtuosoHandler.insertModel(uniformedModel, virtuosoConfig, accountName)
      } catch {
        case httpException: HttpException => logger.error("httpException")
        case eofException: EOFException => logger.error("eofException")
        case socketException: SocketException => logger.error(s"SOCKETEXCEPTIon")
        case connectException: ConnectException => logger.error(s"Connection timed out for $account")
        case riotNotFoundException: RiotNotFoundException => logger.error(s"url $account not found.")
        case riotException: RiotException => logger.error(s"riotException in $account .")
      }

    }
    //
    //    accounts.foreach(account => {
    ////      println(s"ACCOUNT: $account")
    //      try{
    //        val accountModel = ModelFactory.createDefaultModel()
    //        accountModel.read(account.head, "TURTLE")
    //
    //        val uniformedModel = WebIdUniformer.uniform(accountModel)
    //        VirtuosoHandler.insertModel(uniformedModel, virtuoso, account.last)
    ////        val outFile= crawlDir/ s"${account(1)}.nt"
    ////        RDFDataMgr.write(new FileOutputStream(outFile.toJava), accountModel, Lang.NTRIPLES)
    //      } catch {
    //        case riotNotFoundException: RiotNotFoundException => LoggerFactory.getLogger("Crawler").error(s"url ${account.head} not found.")
    //        case riotException: RiotException => LoggerFactory.getLogger("Crawler").error(s"riotException in ${account.head}.")
    //      }
    //
    //    })
  }


//  def crawl(): File = {
//    val crawlStream = getClass.getClassLoader.getResourceAsStream("crawl.sh")
//    val in = scala.io.Source.fromInputStream(crawlStream)
//
//    val crawlFile = File("./tmp/crawl.sh")
//    crawlFile.parent.createDirectoryIfNotExists()
//
//    val out = new java.io.PrintWriter(crawlFile.toJava)
//    try {
//      in.getLines().foreach(out.println(_))
//    }
//    finally {
//      out.close
//    }
//
//    var result =""
//
//    try{
//      import sys.process._
//      Seq("chmod", "+x", crawlFile.pathAsString).!!
//      result = Seq(crawlFile.pathAsString).!!
//
////      crawlFile.delete()
//    } catch {
//      case io:IOException => println(s"${crawlFile.pathAsString} not found")
//    }
//
//    File(result.trim)
//  }
}
