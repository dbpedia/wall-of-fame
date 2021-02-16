package org.dbpedia.walloffame.virtuosoTest

import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.jena.util.FileManager
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import virtuoso.jdbc4.VirtuosoException
import virtuoso.jena.driver._

import java.io.{ByteArrayOutputStream, InputStream, InputStreamReader}


class VirtuosoHandler {


  val graph = "http://test"

  @Test
  def insertTriples(): Unit ={
    clearGraph(graph)

    val set = new VirtGraph ("jdbc:virtuoso://localhost", "dba", "dba")

    val str = s"INSERT INTO GRAPH <$graph> { <aa> <bb> 'cc' . <aa1> <bb1> 123. }"
    val vur = VirtuosoUpdateFactory.create(str, set)

    vur.exec()


    readFromGraph(graph, set)
  }

  @Test
  def insertFile(): Unit ={
    clearGraph(graph)

    try {
      val file = "./src/test/resources/correctWebId.ttl"

      val model: Model = VirtModel.openDatabaseModel(graph, "jdbc:virtuoso://localhost:1111", "dba", "dba")
      val in: InputStream = FileManager.get().open(file)
      if (in == null) {
        throw new IllegalArgumentException("File: " + file + " not found")
      }
      model.read(new InputStreamReader(in), null, "TURTLE")
      model.close()
    } catch {
      case e:Exception => System.out.println("Ex="+e)
    }

    readFromGraph(graph, new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba"))

  }

  @Test
  def insertDataToJenaModel(): Unit ={
    clearGraph(graph)

    try {
      val file = "./src/test/resources/correctWebId.ttl"

      val model: Model = VirtModel.openDatabaseModel(graph, "jdbc:virtuoso://localhost:1111", "dba", "dba")
      val in: InputStream = FileManager.get().open(file)
      if (in == null) {
        throw new IllegalArgumentException("File: " + file + " not found")
      }
      model.read(new InputStreamReader(in), null, "TURTLE")
      model.close()
    } catch {
      case e:Exception => System.out.println("Ex="+e)
    }

    val model2: Model = VirtModel.openDatabaseModel(graph, "jdbc:virtuoso://localhost:1111", "dba", "dba")
    val out = new ByteArrayOutputStream()
    RDFDataMgr.write(out, model2, Lang.TTL)
    println(out)

  }

  @Test
  def shouldDisplayAllEntries(): Unit ={
    readFromGraph(graph, new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba"))

  }

  @Test
  def shouldDeleteAllEntries(): Unit ={
    clearGraph(graph)
  }

  @Test
  def shouldDisplayAllGraphs(): Unit ={
    readFromAllGraphs()
  }




  def clearGraph(graphName: String)={
    val set = new VirtGraph(graphName,"jdbc:virtuoso://localhost:1111", "dba", "dba")
    set.clear()
  }

  def readFromGraph(graphName:String, graph:VirtGraph) ={
    import org.apache.jena.query.{Query, QueryFactory}

    val sparql: Query = QueryFactory.create(s"SELECT * FROM <$graphName> WHERE { ?s ?p ?o }")

    val vqe: VirtuosoQueryExecution = VirtuosoQueryExecutionFactory.create(sparql, graph)

    val results = vqe.execSelect
    while (results.hasNext) {
      val rs = results.nextSolution
      val s = rs.get("s")
      val p = rs.get("p")
      val o = rs.get("o")
      System.out.println(" { " + s + " " + p + " " + o + " . }")
    }
  }

  def readFromAllGraphs() ={
    import org.apache.jena.query.{Query, QueryFactory}

    val virt = new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba")

    val sparql: Query = QueryFactory.create(
      """
        |SELECT  DISTINCT ?g
        |WHERE  {
        |   GRAPH ?g {?s ?p ?o}
        |   FILTER regex(?g, "^http://webids")
        |}
        |ORDER BY  ?g
      """.stripMargin)

    val vqe: VirtuosoQueryExecution = VirtuosoQueryExecutionFactory.create(sparql, virt)

    val results = vqe.execSelect
    while (results.hasNext) {
      val rs = results.nextSolution
      val s = rs.get("g")
    }
  }

  @Test
  def shouldCatchException()={
    try{

      val virt = new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba")
    } catch {
      case virtuosoException: VirtuosoException => "hallo"
    }

  }

  @Test
  def shouldCatch()={
    getAllGraphs()
  }


  def getAllGraphs():Seq[String] ={


    import org.apache.jena.query.{Query, QueryFactory}

    val virt =
      try{
        val newVirt = new VirtGraph("jdbc:virtuoso://localhost:1111", "dba", "dba")
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
           |   FILTER regex(?g, "^asd")
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

}
