package org.dbpedia.walloffame

import better.files.File
import org.apache.jena.shared.JenaException
import org.apache.juli.logging.{Log, LogFactory}
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.dbpedia.walloffame.webid.DatabusWebIdsFetcher
import org.dbpedia.walloffame.webid.enrich.github.GithubTokenHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class WofInitRunner extends CommandLineRunner {

  val logger: Log = LogFactory.getLog(getClass)

  @Autowired
  private var config: Config = _

  override def run(args: String*): Unit = {
    logger.info("Execute WofInitRunner")

    //why debug isnt printed out?
    logger.debug("DASDASDASDFGFDSA")

    deleteStuffFromOldSession()
    prepareWallOfFame()
  }

  def deleteStuffFromOldSession():Unit={
    File("./tmp/").delete(swallowIOExceptions = true)
    File("./tmp/").createDirectory()
    File(config.log.file).delete(swallowIOExceptions = true)

    try{
      val vos = new VirtuosoHandler(config.virtuoso)
      vos.deleteAllGraphs()
    } catch {
      case jenaException: JenaException => logger.error(jenaException)
    }
  }

  def prepareWallOfFame():Unit = {
    config.github.setGithubToken(GithubTokenHandler.getToken(config.github.client_id))

    //fetch databus-registered webIds to virtuoso of wall of fame
    val fetcher = new DatabusWebIdsFetcher(config)
    fetcher.fetchRegisteredWebIds()
  }

}
