package org.dbpedia.walloffame.virtuoso

import better.files.File
import org.apache.jena.query.{Query, QueryFactory}
import org.apache.jena.rdf.model.{Model, ModelFactory, ModelMaker}
import org.apache.jena.util.FileManager
import org.dbpedia.walloffame.VosConfig
import org.slf4j.LoggerFactory
import virtuoso.jdbc4.VirtuosoException
import virtuoso.jena.driver.{VirtGraph, VirtModel, VirtuosoQueryExecution, VirtuosoQueryExecutionFactory}

import java.io.{InputStream, InputStreamReader}

object VirtuosoHandler {

  def insertFile(file: File, vosConfig: VosConfig, subGraph: String) = {
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

  def insertModel(model: Model, vosConfig: VosConfig, subGraph:String) = {
    val virtmodel: VirtModel = VirtModel.openDatabaseModel(vosConfig.graph.concat(subGraph), vosConfig.url, vosConfig.usr, vosConfig.psw)
    virtmodel.add(model)
    virtmodel.close()
  }

  def clearGraph(vosConfig: VosConfig, graph:String) = {
    val set = new VirtGraph(graph, vosConfig.url, vosConfig.usr, vosConfig.psw)
    set.clear()
  }

  def getModel(vosConfig: VosConfig, graph:String): Model = {
    val model: Model = VirtModel.openDatabaseModel(graph, vosConfig.url, vosConfig.usr, vosConfig.psw)
    model
  }

  def getAllGraphs(vosConfig: VosConfig):Seq[String] ={

    val virt =
      try{
        val newVirt = new VirtGraph(vosConfig.url, vosConfig.usr, vosConfig.psw)
        Option(newVirt)
      } catch {
        case virtuosoException: VirtuosoException => {
        LoggerFactory.getLogger("Virtuoso").error("Connection refused")
        None
      }
    }

    if (virt == None) Seq.empty[String]
    else {
      val sparql: Query = QueryFactory.create(
        s"""
           |SELECT  DISTINCT ?g
           |WHERE  {
           |   GRAPH ?g {?s ?p ?o}
           |   FILTER regex(?g, "^${vosConfig.graph}")
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

  def getAllWebIds(vosConfig: VosConfig): Seq[(String, Model)] = {
    try {
      var models = Seq.empty[(String, Model)]
      VirtuosoHandler.getAllGraphs(vosConfig).foreach(graph =>
        models = models :+ (graph.split('/').last.capitalize, VirtuosoHandler.getModel(vosConfig, graph))
      )
      models
    } catch {
      case virtuosoException: VirtuosoException => {
        LoggerFactory.getLogger("Virtuoso").error("Connection to Virtuoso DB failed.")
        Seq.empty[(String, Model)]
      }
    }
  }

}
