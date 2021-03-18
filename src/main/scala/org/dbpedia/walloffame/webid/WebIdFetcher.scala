package org.dbpedia.walloffame.webid

import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.tools.JsonLDLogger
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.slf4j.LoggerFactory

object WebIdFetcher {

  var logger = LoggerFactory.getLogger("WebIdFetcher")

  def fetchRegisteredWebIds(config: Config): Unit = {

    val vos = new VirtuosoHandler(config.virtuoso)
    val webIdHandler = new WebIdHandler()

    val gitHubMap = Enricher.countAllGithubCommitsPerUser()

    //get all webIds already stored in Virtuoso
    var allCurrentGraphs = Seq.empty[String]
    var wait = true
    var time = 0
    while (wait) {
      try {
        allCurrentGraphs = vos.getAllGraphURIs()
        wait = false
      } catch {
        case e: Exception =>
          time += 1
          println(s"Waiting since $time seconds for Virtuoso to start up.")
          Thread.sleep(1000)
      }
    }

    println(
      """
        |Download, validate, and uniform all registered WebIds on the DBpedia Databus.
        |Accounts:""".stripMargin)

    //get all registered webIds
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

          Enricher.enrichModelWithDatabusData(uniformedModel, webid)
          Enricher.enrichModelWithGithubData(uniformedModel, gitHubMap, webid)
          vos.insertModel(uniformedModel, accountURL)
        }
      } catch {
        case noSuchElementException: NoSuchElementException => ""
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

  //
  //  def collectAdditionalData(webidURL:String, account:String):Model={
  //    val model = ModelFactory.createDefaultModel()
  //    val result = QueryHandler.executeQuery(SelectQueries.getDatabusUserData(webidURL)).head
  //
  //    val ontology = "http://dbpedia.org/ontology/"
  //    val numUploads = "numUploads"
  //    val uploadSize = "uploadSize"
  //
  //    model.add(
  //      ResourceFactory.createStatement(
  //        ResourceFactory.createResource(webidURL),
  //        ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/account"),
  //        ResourceFactory.createResource(account)
  //      )
  //    )
  //
  //    def addDecimalLiteralToModel(prop:String, value:Literal) ={
  //      model.add(
  //        ResourceFactory.createStatement(
  //          ResourceFactory.createResource(account),
  //          ResourceFactory.createProperty(ontology+prop),
  //          value
  //        )
  //      )
  //    }
  //
  //    val value = {
  //      if (result.getLiteral(numUploads) != null) result.getLiteral(numUploads)
  //      else ResourceFactory.createTypedLiteral(0)
  //    }
  //    addDecimalLiteralToModel(numUploads, value)
  //
  //    val value2 = {
  //      if(result.getLiteral(uploadSize) != null) {
  //        val uploadSizeAsMB = result.getLiteral(uploadSize).getLong  / 1024 / 1024
  //        println("UPLOADSIZE")
  //        println(webidURL)
  //        println(uploadSizeAsMB)
  //        ResourceFactory.createTypedLiteral(uploadSizeAsMB)
  //      } else {
  //        ResourceFactory.createTypedLiteral(0)
  //      }
  //    }
  //    addDecimalLiteralToModel(uploadSize, value2)
  //
  //    val stmts = model.listStatements()
  //    while (stmts.hasNext) println(stmts.nextStatement())
  //    model
  //  }
  //
  //  def isPerson(model: Model, webidURI:String):Boolean={
  //    try{
  //      QueryHandler.executeQuery(SelectQueries.checkIfIsPerson(webidURI),model).head
  //      true
  //    } catch {
  //      case noSuchElementException: NoSuchElementException => false
  //    }
  //  }
}
