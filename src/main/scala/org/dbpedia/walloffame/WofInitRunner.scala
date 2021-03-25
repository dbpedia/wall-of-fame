package org.dbpedia.walloffame

import better.files.File
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.dbpedia.walloffame.webid.DatabusWebIdsFetcher
import org.dbpedia.walloffame.webid.enrich.github.GithubTokenHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class WofInitRunner extends CommandLineRunner {

  @Autowired
  private var config: Config = _

  override def run(args: String*): Unit = {

    File("./tmp/").delete(swallowIOExceptions = true)
    File("./tmp/").createDirectory()
    File(config.log.file).delete(swallowIOExceptions = true)

    val vos = new VirtuosoHandler(config.virtuoso)
    vos.deleteAllGraphs()

    //warum wird diese funktion ohne if abfrage erneut ausgefuehrt?
    if(config.github.githubToken==null) config.github.setGithubToken(GithubTokenHandler.setToken(config.github.client_id))

    prepareWallOfFame()
  }

  //warum geht scheduled hier, aber nicht direkt im WebIdFetcher?
//  @Scheduled(cron = "0 * * * * ?", zone = "GMT+1:00")
  @Scheduled(cron = "0 0 8 * * ?", zone = "GMT+1:00")
  def prepareWallOfFame():Unit = {
    //fetch databus-registered webIds to virtuoso of wall of fame
    DatabusWebIdsFetcher.fetchRegisteredWebIds(config)
  }
}
