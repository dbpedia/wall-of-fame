package org.dbpedia.walloffame.shaclTest

import better.files.File
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.jena.shacl.{ShaclValidator, Shapes}
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.SelectQueries
import org.dbpedia.walloffame.spring.model.Result
import org.junit.jupiter.api.Test

import java.io.ByteArrayOutputStream
import scala.util.matching.Regex

class ShaclTest {

  val shapeFile = File("./src/main/resources/shacl/shapes.ttl")
  val testResourceDir = File("./src/test/resources/")

  @Test
  def shaclTest: Unit = {
    val webIdFile = testResourceDir / "wrongWebId.ttl"

    val result = validateNew(webIdFile, shapeFile)
    println(result.violations.isEmpty)
    println(result.conforms)
  }

  def validateNew(webIdFile: File, shapesFile: File): Result = {
    val shapesGraph = RDFDataMgr.loadGraph(shapesFile.pathAsString)
    val dataGraph = RDFDataMgr.loadGraph(webIdFile.pathAsString)
    val shapes = Shapes.parse(shapesGraph)

    val report = ShaclValidator.get.validate(shapes, dataGraph)
    val result = new Result

    //full result
    val out = new ByteArrayOutputStream()
    RDFDataMgr.write(out, report.getModel, Lang.TTL)
    result.setResult(out.toString)
    out.close()

    //set infos and violations
    QueryHandler.executeQuery(SelectQueries.resultSeverity, report.getModel).foreach(
      solution => {
        if (solution.getResource("severity").getURI == "http://www.w3.org/ns/shacl#Violation") {
          result.addViolation(solution.getResource("focusNode").getURI, solution.getLiteral("message").getLexicalForm)
        }
        else if (solution.getResource("severity").getURI == "http://www.w3.org/ns/shacl#Info") {
          result.addInfo(solution.getResource("focusNode").getURI, solution.getLiteral("message").getLexicalForm)
        }
      }
    )

    result
  }

  @Test
  def shaclTestShouldSuccess: Unit = {
    val webIdFile = testResourceDir / "jan.ttl"
    val modelJan = RDFDataMgr.loadModel(webIdFile.pathAsString)

    val result = validate(modelJan, RDFDataMgr.loadModel(shapeFile.pathAsString))

    result.violations.foreach(println(_))
    result.infos.foreach(println(_))
    println(result.conforms)
  }


  def validate(webId: Model, shapesModel: Model): Result = {
    val shapesGraph = shapesModel.getGraph
    val dataGraph = webId.getGraph
    val shapes = Shapes.parse(shapesGraph)

    val report = ShaclValidator.get.validate(shapes, dataGraph)
    val result = new Result

    //full result
    val out = new ByteArrayOutputStream()
    RDFDataMgr.write(out, report.getModel, Lang.TTL)

    println(out.toString)
    result.setResult(out.toString)
    out.close()

    //set infos and violations
    QueryHandler.executeQuery(SelectQueries.resultSeverity, report.getModel).foreach(
      solution => {
        if (solution.getResource("severity").getURI == "http://www.w3.org/ns/shacl#Violation") {
          result.addViolation(solution.getResource("focusNode").getURI, solution.getLiteral("message").getLexicalForm)
        }
        else if (solution.getResource("severity").getURI == "http://www.w3.org/ns/shacl#Info") {
          result.addInfo(solution.getResource("focusNode").getURI, solution.getLiteral("message").getLexicalForm)
        }
      }
    )

    result
  }

  @Test
  def returnString()={
    val id = "http://www.w3.org/2001/XMLSchema#asdasdasd"

    val pattern = new Regex("#|/")

    println(pattern.split("http://www.w3.org/2001/XMLSchema#asdasdasd").last)
    println(id.split("#|/").last)
    val i = id.matches("[#|/]")
  println(i)
  }
}

