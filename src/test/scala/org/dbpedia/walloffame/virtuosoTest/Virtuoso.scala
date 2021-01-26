package org.dbpedia.walloffame.virtuosoTest

import better.files.File
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ContextConfiguration, TestPropertySource}

@SpringBootTest
class Virtuoso {


  val dir = File("./src/test/resources/webids")

  @Autowired
  private var config: Config = _


  @Test
  def test(){

//    VirtuosoHandler.clearGraph(config.virtuoso)
//    println(config)
//
//    dir.children.toList.foreach(println(_))
//    dir.listRecursively.foreach(file => VirtuosoHandler.insertFile(file, config.virtuoso))
//
//    val stmts = VirtuosoHandler.getModel(config.virtuoso).listStatements()
//
//    while(stmts.hasNext) println(stmts.nextStatement())
  }
}
