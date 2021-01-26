package org.dbpedia.walloffame.shaclTest

import better.files.File
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.jena.shacl.lib.ShLib
import org.apache.jena.shacl.{ShaclValidator, Shapes}
import org.dbpedia.walloffame.spring.model.Result
import org.dbpedia.walloffame.uniform.QueryHandler
import org.dbpedia.walloffame.uniform.queries.SelectQueries
import org.dbpedia.walloffame.validation.WebIdValidator
import org.junit.jupiter.api.Test

import java.io.ByteArrayOutputStream

class ShaclTest {

  val shapeFile = File("./src/main/resources/shacl/shapes.ttl")
  val testResourceDir = File("./src/test/resources/")

  //  @Test
  //  def shouldPrintOutCorrectOutput {
  //    val webIdFile = testResourceDir / "wrongWebId.ttl"
  //    println(WebIdValidator.validate(webIdFile, shapeFile))
  //  }
  //
  //  @Test
  //  def shouldSuccess {
  //    val webIdFile = testResourceDir / "correctWebId.ttl"
  //    println(WebIdValidator.validate(webIdFile))
  //  }

  @Test
  def shaclShouldSuccess: Unit = {
    val webIdFile = testResourceDir / "correctWebId.ttl"
    assert(validate(webIdFile, shapeFile))
  }

  def validate(webIdFile: File, shapesFile: File): Boolean = {

    val shapesGraph = RDFDataMgr.loadGraph(shapesFile.pathAsString)
    val dataGraph = RDFDataMgr.loadGraph(webIdFile.pathAsString)
    val shapes = Shapes.parse(shapesGraph)

    val report = ShaclValidator.get.validate(shapes, dataGraph)

    println("REPORT")
    ShLib.printReport(report)
    System.out.println()
    val out = new ByteArrayOutputStream()
    RDFDataMgr.write(System.out, report.getModel, Lang.TTL)
    println(out)

    val reportModel = report.getModel

    val query =
      """
        |PREFIX sh: <http://www.w3.org/ns/shacl#>
        |
        |SELECT (count(?error) as ?countErrors)
        |WHERE {
        |  ?report  a  sh:ValidationReport ;
        |           sh:result ?result .
        |  ?result  sh:resultSeverity sh:Violation ;
        |           sh:resultSeverity ?error.
        |}
        |""".stripMargin
    val result = QueryHandler.executeQuery(query, reportModel).head

    println(result)
    result.getLiteral("?countErrors").getInt.equals(0)
  }

  @Test
  def shaclShouldFail: Unit = {
    val webIdFile = testResourceDir / "wrongWebId.ttl"
    assert(!validate(webIdFile, shapeFile))
  }

  //
  //  @Test
  //  def correctFileShouldPass: Unit = {
  //
  //    val webIdFile = testResourceDir / "correctWebId.ttl"
  //    println(WebIdValidator.validate(webIdFile, shapeFile))
  //  }
  //
  //  @Test
  //  def wrongFileShouldNotPass: Unit = {
  //    val webIdFile = testResourceDir / "wrongWebId.ttl"
  //    println(WebIdValidator.validate(webIdFile, shapeFile))
  //  }

  @Test
  def stringShould: Unit = {
    val str =
      """
        |@prefix sh:    <http://www.w3.org/ns/shacl#> .
        |@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        |@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
        |@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
        |@prefix foaf:  <http://xmlns.com/foaf/0.1/> .
        |@prefix shsh:  <http://www.w3.org/ns/shacl-shacl#> .
                |
                |[ a            sh:ValidationReport ;
                |  sh:conforms  false ;
                |  sh:result    [ a                             sh:ValidationResult ;
                |                 sh:focusNode                  <https://akirsche.github.io/webid.ttl> ;
                |                 sh:resultMessage              "minCount[1]: Invalid cardinality: expected min 1: Got count = 0" ;
                |                 sh:resultPath                 foaf:maker ;
                |                 sh:resultSeverity             sh:Violation ;
                |                 sh:sourceConstraintComponent  sh:MinCountConstraintComponent ;
                |                 sh:sourceShape                []
                |               ]
                |] .
                |""".stripMargin
  }


  @Test
  def shapeShouldNotBeEmpty: Unit = {
    val shapes = Shapes.parse("./src/test/resources/New Folder/shape.ttl")

    val result = new Result
    result.getResult
    assert(!shapes.isEmpty)
  }

  @Test
  def shaclTest: Unit = {
    val webIdFile = testResourceDir / "wrongWebId.ttl"
    //    assert(!validate(webIdFile, shapeFile))


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

    val resulti = validateNew(webIdFile, shapeFile)
    println(resulti.violations.isEmpty)
    println(resulti.conforms)
  }
}

