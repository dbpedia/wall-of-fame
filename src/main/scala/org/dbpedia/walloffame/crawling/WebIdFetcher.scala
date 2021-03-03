package org.dbpedia.walloffame.crawling

import org.apache.jena.atlas.web.HttpException
import org.apache.jena.rdf.model.{Model, ModelFactory, ResourceFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr, RiotException, RiotNotFoundException}
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.logging.JsonLDLogger
import org.dbpedia.walloffame.uniform.queries.SelectQueries
import org.dbpedia.walloffame.uniform.{QueryHandler, WebIdUniformer}
import org.dbpedia.walloffame.validation.WebIdValidator
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.slf4j.LoggerFactory

import java.io.EOFException
import java.net.{ConnectException, SocketException}

object WebIdFetcher {

  var logger = LoggerFactory.getLogger("WebIdFetcher")

  def fetchRegisteredWebIds(config: Config): Unit = {

    val jsonldLogger = new JsonLDLogger(config.log.file)
    val vos = new VirtuosoHandler(config.virtuoso)

    var allCurrentGraphs = Seq.empty[String]
    var wait = true
    var time = 0
    while (wait) {
      try {
        allCurrentGraphs = vos.getAllGraphURIs()
        wait = false
      } catch {
        case e: Exception =>
          time +=1
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
      val accountURL = stmt.getObject.toString //.replaceFirst("https://databus.dbpedia.org/", "")
      println(webid)

//      var graphURL = webid.replace("http://","").replace("https://","")
//      if (graphURL.contains("#")) graphURL = graphURL.splitAt(graphURL.lastIndexOf("#"))._1
//      graphURL = config.virtuoso.graph.concat(graphURL)

      //clear former graph of this account
      vos.clearGraph(accountURL)
      allCurrentGraphs = allCurrentGraphs.filter(! _.contains(accountURL))

      try {
        val model = RDFDataMgr.loadModel(webid, Lang.TURTLE)

        if(isPerson(model,webid)) {

          val result = WebIdValidator.validate(model, config.shacl.url)

          if (result.conforms()) {
            val uniformedModel = WebIdUniformer.uniform(model, result.getInfos)
            uniformedModel.add(collectAdditionalData(webid, accountURL))
            vos.insertModel(uniformedModel, accountURL)

          } else {
            result.violations.foreach(tuple => {
              jsonldLogger.add(webid, "https://example.org/hasViolation", tuple._2)
            })
          }
        } else {
          logger.error(s"$webid: is not a foaf:Person")
          jsonldLogger.add(webid, "https://example.org/hasViolation", "Is not a foaf:Person")
        }

      } catch {
        case httpException: HttpException => {
          logger.error(s"$webid: ${httpException.toString}")
          jsonldLogger.addException(webid, httpException)
        }
        case eofException: EOFException => {
          logger.error(s"$webid: ${eofException.toString}")
          jsonldLogger.addException(webid, eofException)
        }
        case socketException: SocketException => {
          logger.error(s"$webid: ${socketException.toString}")
          jsonldLogger.addException(webid, socketException)
        }
        case connectException: ConnectException => {
          logger.error(s"$webid: Connection timed out.")
          jsonldLogger.addException(webid, connectException)
        }
        case riotNotFoundException: RiotNotFoundException => {
          logger.error(s"$webid : url not found.")
          jsonldLogger.addException(webid, riotNotFoundException)
        }
        case riotException: RiotException => {
          logger.error(s"$webid : ${riotException.toString}")
          jsonldLogger.addException(webid, riotException)
        }
      }

    }

    if (allCurrentGraphs.nonEmpty) {
      println("webids that were registered before, but are not accessible now so they are deleted now.")
      allCurrentGraphs.foreach(graph =>{
        println(graph)
        vos.clearGraph(graph)
      })
    }

    jsonldLogger.writeOut()
  }


  def collectAdditionalData(webidURL:String, account:String):Model={
    val model = ModelFactory.createDefaultModel()
    val result = QueryHandler.executeQuery(SelectQueries.getDatabusUserData(webidURL)).head

    val ontology = "http://dbpedia.org/ontology/"
    val numUploads = "numUploads"
    val uploadSize = "uploadSize"

    model.add(
      ResourceFactory.createStatement(
        ResourceFactory.createResource(webidURL),
        ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/account"),
        ResourceFactory.createResource(account)
      )
    )

    def addDecimalLiteralToModel(literal:String) ={
      val value = {
        if (result.getLiteral(literal) != null) result.getLiteral(literal)
        else ResourceFactory.createTypedLiteral(0)
      }

      model.add(
        ResourceFactory.createStatement(
          ResourceFactory.createResource(account),
          ResourceFactory.createProperty(ontology+literal),
          value
        )
      )
    }

    addDecimalLiteralToModel(numUploads)
    addDecimalLiteralToModel(uploadSize)

    model
  }

  def isPerson(model: Model, webidURI:String):Boolean={
    try{
      QueryHandler.executeQuery(SelectQueries.checkIfIsPerson(webidURI),model).head
      true
    } catch {
      case noSuchElementException: NoSuchElementException => false
    }
  }
}
