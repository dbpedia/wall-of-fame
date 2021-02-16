package org.dbpedia.walloffame

import better.files.File
import org.dbpedia.walloffame.crawling.WebIdFetcher
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class InitRunner extends CommandLineRunner {

  @Autowired
  private var config: Config = _

  override def run(args: String*): Unit = {

    File("./tmp/").delete(true)
    File("./tmp/").createDirectory()

//    prepareWallOfFame()
  }

  def prepareWallOfFame() = {

    //delete all webId graphs from Session before!
    var wait = true
    var time = 0
    while (wait) {
      try {
        val vos = new VirtuosoHandler(config.virtuoso)
        vos.getAllGraphURIs().foreach(graph => vos.clearGraph(graph))
        wait = false
      } catch {
        case e: Exception =>
          time +=1
          println(s"Waiting since $time seconds for Virtuoso to start up.")
          Thread.sleep(1000)
      }
    }

    //fetch databus-registered webIds to virtuoso of wall of fame
    WebIdFetcher.fetchRegisteredWebIds(config)
  }
}
