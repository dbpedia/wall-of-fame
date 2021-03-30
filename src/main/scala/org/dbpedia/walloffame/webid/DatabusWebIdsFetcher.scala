package org.dbpedia.walloffame.webid

import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.juli.logging.{Log, LogFactory}
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.tools.JsonLDLogger
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.dbpedia.walloffame.webid.enrich.DatabusEnricher
import org.dbpedia.walloffame.webid.enrich.github.GitHubEnricher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DatabusWebIdsFetcher(config: Config){

  val logger: Log = LogFactory.getLog(getClass)

  @Scheduled(cron = "0 0 8 * * ?", zone = "GMT+1:00")
  def fetchRegisteredWebIds(): Unit = {

    println("DBpedia Databus Webid Fetcher:")

    val vos = new VirtuosoHandler(config.virtuoso)
    val webIdHandler = new WebIdHandler(config.shacl.url)

    //get all webIds already stored in Virtuoso
    var allCurrentGraphs = Seq.empty[String]
    var wait = true
    var time = 10
    while (wait) {
      try {
        allCurrentGraphs = vos.getAllGraphURIs()
        wait = false
      } catch {
        case e: Exception =>
          if(time <= 0) {
            println("")
            logger.error(e)
            return
          }

          time -= 1

          print('\r')
          print(s"Wait $time more seconds for Virtuoso to start up.")
          Thread.sleep(1000)
      }
    }

    val gitHubMap = GitHubEnricher.countAllGithubCommitsPerUser(config.github.githubToken)
    //    val gitHubMap = collection.mutable.Map[String, Int]().withDefaultValue(0)

    //get all registered webIds
    println(
      """
        |Download, validate, and uniform all registered WebIds on the DBpedia Databus.
        |Accounts:""".stripMargin)

    val url = "https://databus.dbpedia.org/system/api/accounts"
    val model = RDFDataMgr.loadModel(url, Lang.NTRIPLES)

    val stmts = model.listStatements()
    while (stmts.hasNext) {
      val stmt = stmts.nextStatement()
      val webid = stmt.getSubject.toString
      val accountURL = stmt.getObject.toString
      println(accountURL)
      //clear former graph of this account
      vos.clearGraph(accountURL)
      allCurrentGraphs = allCurrentGraphs.filter(!_.contains(accountURL))

      try {
        val uniformedModel = webIdHandler.validateWebId(webid)._1
        if(!uniformedModel.isEmpty) {
          DatabusEnricher.enrichModelWithDatabusData(uniformedModel, webid)
          GitHubEnricher.enrichModelWithGithubData(uniformedModel, gitHubMap, webid)
          vos.insertModel(uniformedModel, accountURL)
        }
      } catch {
        case noSuchElementException: NoSuchElementException => logger.error(s"$webid not valid.")
      }
    }

    if (allCurrentGraphs.nonEmpty) {
      println("Webids that were registered before, but are not accessible anymore, so they will be deleted now:")
      allCurrentGraphs.foreach(graph => {
        println(graph)
        vos.clearGraph(graph)
      })
    }

    JsonLDLogger.writeOut(config.log.file)
  }
}