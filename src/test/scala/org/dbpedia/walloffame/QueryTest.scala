package org.dbpedia.walloffame

import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.junit.jupiter.api.Test

class QueryTest {

//  @Test
//  def optionalQueries()={
//    val model = ModelFactory.createDefaultModel()
//    model.read("./src/test/resources/eisenbahnplatte.ttl")
//
//    val optional = QueryHandler.executeQuery(SelectQueries.getOptionalValues(), model).head
//    println(optional.getLiteral("?geekcode").getLexicalForm)
//  }


//  @Test
//  def createWebIdObject={
//    println(this.getClass.getSimpleName)
//    val model = RDFDataMgr.loadModel("./src/main/resources/yumyab.ttl", Lang.TURTLE)
//    val uniModel = WebIdUniformer.uniform(model)
//    val stmts = uniModel.listStatements()
//    while(stmts.hasNext) println(stmts.nextStatement())
//    val result = QueryHandler.executeQuery(SelectQueries.getWebIdData(), model).head
//    if (!(result.get("img") == null || result.get("img").isResource)) println("kacka")
//    else println("kleine kacka")
//
//    val webId = new WebId(uniModel)
//
//    println(new Gson().toJson(webId))
//  }

  @Test
  def loadModel()={
    val model = RDFDataMgr.loadModel("https://eisenbahnplatte.github.io/webid.ttl#this", Lang.TURTLE)

    val stmts = model.listStatements()
    while(stmts.hasNext) println(stmts.nextStatement())
  }

}
