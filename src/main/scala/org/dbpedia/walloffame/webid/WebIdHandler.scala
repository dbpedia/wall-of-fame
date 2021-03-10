package org.dbpedia.walloffame.webid

import org.apache.commons.io.IOUtils
import org.apache.http.HttpException
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.{RDFDataMgr, RiotException, RiotNotFoundException}
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.SelectQueries
import org.dbpedia.walloffame.spring.model.Result
import org.dbpedia.walloffame.tools.JsonLDLogger
import org.slf4j.LoggerFactory

import java.io.EOFException
import java.net.{ConnectException, MalformedURLException, SocketException, URL}

/**
 *
 * @param shacl URL of Shacl file
 */
class WebIdHandler() {

  var logger = LoggerFactory.getLogger("WebIdHandler")

  def validateWebId(str:String, log:Boolean=true):(Model, Result) ={
    try { //if webid is send as url
      new URL(str)
      val model = RDFDataMgr.loadModel(str)
      if(checkIfURIisDBPedianPerson(str, model))
        validateAndUniformModel(model, log)
      else {
        val result = new Result
        result.addViolation(str, "is not a Person or no dbo:DBpedian", "https://example.org/hasViolation")
        JsonLDLogger.add(str, "https://example.org/hasViolation", "URI is not a Person and dbo:DBpedian")
        (ModelFactory.createDefaultModel(), result)
      }
    } catch {
      case malformedURLException: MalformedURLException => //if webId is send as plain text
        try{
          val model: Model = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(str, "UTF-8"), "", "TURTLE")
          validateAndUniformModel(model, log)
        } catch {
          case riotException: RiotException => handleException(str, riotException, log)
        }
      case httpException: HttpException => handleException(str, httpException, log)
      case eofException: EOFException => handleException(str, eofException, log)
      case socketException: SocketException => handleException(str, socketException, log)
      case connectException: ConnectException => handleException(str, connectException, log)
      case riotNotFoundException: RiotNotFoundException => handleException(str, riotNotFoundException, log)
      case riotException: RiotException => handleException(str, riotException, log)
    }
  }

  def handleException(node:String, exception: Exception, log:Boolean):(Model, Result)={
    logger.error(s"$node : url not found.")
    if(log) {
      JsonLDLogger.addException(node, exception)
    }
    val result = new Result
    result.addViolation("Exception", exception.toString)
    (ModelFactory.createDefaultModel(), result)
  }

  /**
   *
   * @param webid webid URL
   * @return
   */
  def validateAndUniformModel(model:Model, log:Boolean=true):(Model, Result)={

//    val result = WebIdValidator.validate(model, config.shacl.url)
    val result = WebIdValidator.validate(model, "./src/main/resources/shacl/shapes.ttl")

    if (result.conforms()) {
      val uniformedModel = WebIdUniformer.uniform(model)
      return (uniformedModel, result)
    } else {
      if(log){
        result.violations.foreach(tuple => {
          //UPDATE first param
          JsonLDLogger.add("person", "https://example.org/hasViolation", tuple._2)
        })
      }
      return (ModelFactory.createDefaultModel(), result)
    }

  }

  def checkIfURIisDBPedianPerson(uri:String, model: Model):Boolean={
    var isPerson =true
    val toBeChecked = Array("http://xmlns.com/foaf/0.1/Person", "http://dbpedia.org/ontology/DBpedian")
    var modelAttributes= Array.empty[String]
    val results = QueryHandler.executeQuery(SelectQueries.getTypeOf(uri), model)

    if(results.nonEmpty) {
      results.foreach(x => modelAttributes= modelAttributes :+ x.getResource("type").toString)
    }
    toBeChecked.foreach(entry=> if(!modelAttributes.contains(entry)) isPerson=false)

//    println(s"$uri is person: $isPerson")
    isPerson
  }

}
