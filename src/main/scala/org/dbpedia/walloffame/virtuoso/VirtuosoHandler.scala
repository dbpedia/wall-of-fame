package org.dbpedia.walloffame.virtuoso

import better.files.File
import org.apache.jena.query.{Query, QueryFactory}
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.util.FileManager
import org.dbpedia.walloffame.VosConfig
import org.dbpedia.walloffame.spring.model.WebId
import org.slf4j.LoggerFactory
import virtuoso.jdbc4.VirtuosoException
import virtuoso.jena.driver.{VirtGraph, VirtModel, VirtuosoQueryExecution, VirtuosoQueryExecutionFactory}

import java.io.{InputStream, InputStreamReader}

class VirtuosoHandler(vosConfig: VosConfig) {

  val logger = LoggerFactory.getLogger(classOf[VirtuosoHandler])
  val mainGraph = "https://databus.dbpedia.org/"

  def deleteAllGraphs(): Unit ={
    getAllGraphURIs().foreach(clearGraph)
  }

  def insertFile(file: File, subGraph: String): Unit = {
    try {
      val model: Model = VirtModel.openDatabaseModel(vosConfig.graph.concat(subGraph), vosConfig.url, vosConfig.usr, vosConfig.psw)
      val in: InputStream = FileManager.get().open(file.pathAsString)
      if (in == null) {
        throw new IllegalArgumentException("File: " + file + " not found")
      }
      model.read(new InputStreamReader(in), null, "TURTLE")
      model.close()
    } catch {
      case e: Exception => System.out.println("Ex=" + e)
    }
  }

  def insertModel(model: Model, targetGraph:String): Unit = {
    val virtmodel: VirtModel = VirtModel.openDatabaseModel(targetGraph, vosConfig.url, vosConfig.usr, vosConfig.psw)
    virtmodel.add(model)
    virtmodel.close()
  }

  def clearGraph(graph:String): Unit = {
    val set = new VirtGraph(graph, vosConfig.url, vosConfig.usr, vosConfig.psw)
    set.clear()
  }

  def getGraph(graph:String): VirtGraph = {
    val virtGraph = new VirtGraph(graph, vosConfig.url, vosConfig.usr, vosConfig.psw)
    virtGraph
  }

  def getAllGraphURIs():Seq[String] ={

    val virt =
      try{
        val newVirt = new VirtGraph(mainGraph, vosConfig.url, vosConfig.usr, vosConfig.psw)
        Option(newVirt)
      } catch {
        case virtuosoException: VirtuosoException => {
        LoggerFactory.getLogger("Virtuoso").error("Connection refused")
        None
      }
    }

    if (virt.isEmpty) Seq.empty[String]
    else {
      val sparql: Query = QueryFactory.create(
        s"""
           |SELECT  DISTINCT ?g
           |WHERE  {
           |   GRAPH ?g {?s ?p ?o}
           |   FILTER regex(?g, "^${mainGraph}")
           |}
           |ORDER BY  ?g
      """.stripMargin)

      val vqe: VirtuosoQueryExecution = VirtuosoQueryExecutionFactory.create(sparql, virt.get)

      val results = vqe.execSelect

      var graphs = Seq.empty[String]

            while (results.hasNext) {
              val rs = results.nextSolution
              val s = rs.get("g")
              graphs=graphs:+s.toString
            }
      graphs
    }

  }

  def getAllWebIdGraphs(): Seq[VirtGraph] = {
    try {
      var graphs = Seq.empty[VirtGraph]

      this.getAllGraphURIs().foreach(graph =>
        graphs = graphs :+ this.getGraph(graph)
      )
      graphs
    } catch {
      case virtuosoException: VirtuosoException => {
        logger.error(s"Connection to Virtuoso DB failed. ${virtuosoException.toString}")
        Seq.empty[VirtGraph]
      }
    }
  }

  def getAllWebIdsAsJson(): String = {
    val graphs = this.getAllWebIdGraphs()

    if (graphs.isEmpty) {
      ""
    } else {
      var json = "{ \"webIds\": [\n"

      import com.google.gson.Gson
      val gson = new Gson()

      graphs.foreach(graph => {
        val webid = new WebId(ModelFactory.createModelForGraph(graph))
//        webid.setAccount(graph.getGraphName.splitAt(graph.getGraphName.lastIndexOf("/")+1)._2)
        json += s"${gson.toJson(webid)},\n"
      })

      json.dropRight(2).concat("\n]}")
    }
  }

  def getAccountOfWebId(url:String): Option[String] ={
    val virt =
      try{
        val newVirt = new VirtGraph(mainGraph, vosConfig.url, vosConfig.usr, vosConfig.psw)
        Option(newVirt)
      } catch {
        case virtuosoException: VirtuosoException => {
          LoggerFactory.getLogger("Virtuoso").error("Connection refused")
          None
        }
      }

    if (virt.isDefined) {
      val sparql: Query = QueryFactory.create(
        s"""
          |SELECT DISTINCT ?g
          |WHERE {
          |  GRAPH ?g { <$url> a <http://xmlns.com/foaf/0.1/PersonalProfileDocument> . }
          |}
          |""".stripMargin)

      val vqe: VirtuosoQueryExecution = VirtuosoQueryExecutionFactory.create(sparql, virt.get)
      val results = vqe.execSelect

      if (results.hasNext) {
        val accountURL = results.nextSolution.get("g").toString
        return Option(accountURL.splitAt(accountURL.lastIndexOf("/")+1)._2)
      }
    }

    None
  }
}
