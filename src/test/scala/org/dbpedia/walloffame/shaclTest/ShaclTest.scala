package org.dbpedia.walloffame.shaclTest

import better.files.File
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.jena.shacl.lib.ShLib
import org.apache.jena.shacl.{ShaclValidator, Shapes}
import org.dbpedia.walloffame.spring.model.Result
import org.dbpedia.walloffame.uniform.QueryHandler
import org.dbpedia.walloffame.uniform.queries.SelectQueries
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

  @Test
  def asdasd():Unit ={
  val str =
    """
      |@prefix dash: <http://datashapes.org/dash#> .
      |@prefix ex: <http://example.com/ns#> .
      |@prefix owl: <http://www.w3.org/2002/07/owl#> .
      |@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
      |@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
      |@prefix sh: <http://www.w3.org/ns/shacl#> .
      |@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
      |@prefix cert: <http://www.w3.org/ns/auth/cert#> .
      |@prefix foaf: <http://xmlns.com/foaf/0.1/> .
      |@prefix dbo: <http://dbpedia.org/ontology/> .
      |
      |
      |<#WebIdShape> a sh:NodeShape ;
      |	sh:targetClass foaf:PersonalProfileDocument ;
      |	sh:property <#maker> ;
      |	sh:property <#primaryTopic> .
      |
      |<#PersonShape> a sh:NodeShape ;
      |	sh:targetClass foaf:Person ;
      |	sh:property <#name> ;
      |	sh:property <#key> ;
      |	sh:property <#img> ;
      |	sh:property <#gender> ;
      |	sh:property <#geekcode> ;
      |	sh:property <#firstname> ;
      |	sh:property <#dbpedia-support-info> .
      |
      |<#KeyShape> a sh:NodeShape ;
      |	sh:targetClass cert:RSAPublicKey ;
      |	sh:property <#label> ;
      |	sh:property <#cert-modulus> ;
      |	sh:property <#cert-exponent> .
      |
      |#===WebIdShapeProperties===#
      |<#maker> a sh:PropertyShape ;
      |	sh:severity sh:Violation ;
      |	sh:message "Required property foaf:maker MUST occur once."@en ;
      |	sh:path foaf:maker;
      |	sh:minCount 1 ;
      |	sh:maxCount 1.
      |
      |<#primaryTopic> a sh:PropertyShape;
      |	sh:severity sh:Violation ;
      |	sh:message "Required property foaf:primaryTopic MUST occur once."@en ;
      |	sh:path foaf:primaryTopic  ;
      |	sh:minCount 1 ;
      |	sh:maxCount 1.
      |#===WebIdShapeProperties===#
      |
      |#===PersonShapeProperties===#
      |<#name> a sh:PropertyShape ;
      |	sh:path foaf:name ;
      |	sh:minCount 1 ;
      |	sh:maxCount 1 ;
      |	sh:severity sh:Violation ;
      |	sh:message "Required property foaf:name MUST occur once."@en .
      |
      |<#key> a sh:PropertyShape ;
      |	sh:path cert:key ;
      |	sh:minCount 1 ;
      |	sh:severity sh:Violation ;
      |	sh:message "Required property cert:key MUST occur at least once."@en .
      |
      |<#gender> a sh:PropertyShape ;
      |	sh:path foaf:gender ;
      |	sh:minCount 1 ;
      |	sh:maxCount 1 ;
      |	sh:severity sh:Info ;
      |	sh:message "Optional property foaf:gender is missing."@en .
      |
      |<#img> a sh:PropertyShape ;
      |	sh:path foaf:img ;
      |	sh:minCount 1 ;
      |	sh:maxCount 1 ;
      |	sh:severity sh:Info ;
      |	sh:message "Optional property foaf:img is missing."@en .
      |
      |<#geekcode> a sh:PropertyShape ;
      |			sh:path foaf:geekcode ;
      |			sh:minCount 1 ;
      |			sh:maxCount 1 ;
      |			sh:severity sh:Info ;
      |			sh:message "Optional property foaf:geekcode is missing."@en .
      |
      |<#dbpedia-support-info>
      |	sh:path rdf:type ;
      |	sh:hasValue dbo:DBpedian ;
      |	sh:severity sh:Info ;
      |	sh:message
      |		"Show your support to DBpedia by adding '<#me> a dbo:DBpedian' to your WebID/FOAF. If you do, we will add you to our website."@en .
      |#===PersonShapeProperties===#
      |
      |
      |#===KeyShapeProperties===#
      |<#label> a sh:PropertyShape ;
      |	sh:path rdf:label ;
      |	sh:minCount 1 ;
      |	sh:severity sh:Violation ;
      |	sh:message "Required property rdf:label MUST occur once at least."@en .
      |
      |<#cert-modulus> a sh:PropertyShape ;
      |	sh:path cert:modulus ;
      |	sh:minCount 1 ;
      |	sh:maxCount 1;
      |	sh:dataType xsd:hexBinary ;
      |	sh:severity sh:Violation ;
      |	sh:message "Required property cert:modulus MUST occur once."@en .
      |
      |<#cert-exponent> a sh:PropertyShape ;
      |	sh:path cert:modulus ;
      |	sh:minCount 1 ;
      |	sh:maxCount 1;
      |	sh:dataType xsd:nonNegativeInteger ;
      |	sh:severity sh:Violation ;
      |	sh:message "Required property cert:exponent MUST occur once."@en .
      |#===KeyShapeProperties===#
      |
      |""".stripMargin

    val shapesModel = RDFDataMgr.loadModel("https://raw.githubusercontent.com/dbpedia/wall-of-fame/master/src/main/resources/shacl/shapes.ttl")
    val shapesGraph = shapesModel.getGraph
    val shapes = Shapes.parse(shapesGraph)
  }
}

