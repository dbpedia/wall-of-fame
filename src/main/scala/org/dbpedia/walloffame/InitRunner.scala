package org.dbpedia.walloffame

import better.files.File
import org.dbpedia.walloffame.crawling.WebIdFetcher
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

    prepareWallOfFame()
  }

  def prepareWallOfFame() = {
    //fetch databus-registered webIds to virtuoso of wall of fame
    WebIdFetcher.fetchRegisteredWebIds(config)
  }
}
