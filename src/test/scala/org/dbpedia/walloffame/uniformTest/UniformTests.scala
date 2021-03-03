package org.dbpedia.walloffame.uniformTest

import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.dbpedia.walloffame.uniform.queries.SelectQueries
import org.dbpedia.walloffame.uniform.{QueryHandler, WebIdUniformer}
import org.junit.jupiter.api.Test

class UniformTests {


  @Test
  def shouldUniformCorrect:Unit ={

    val model = RDFDataMgr.loadModel("./src/test/resources/denis.ttl")

    val uniModel = WebIdUniformer.uniform(model)

    RDFDataMgr.write(System.out, uniModel, Lang.TTL)
  }


  @Test
  def stringTest:Unit ={
    val model = RDFDataMgr.loadModel("https://yum-yab.github.io/webid.ttl")
    try{
      QueryHandler.executeQuery(SelectQueries.checkIfIsPerson("https://yum-yab.github.io/webid.ttl#this"),model).head
      println("ja")
    } catch {
      case noSuchElementException: NoSuchElementException => println("no")
    }
  }
}
