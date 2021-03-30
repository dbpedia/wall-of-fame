package org.dbpedia.walloffame

import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.jena.shacl.{ShaclValidator, Shapes}
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.SelectQueries
import org.dbpedia.walloffame.spring.model.Result
import org.junit.jupiter.api.Test

import java.io.ByteArrayOutputStream

class ShaclTest {

  val shapeModel:Model = RDFDataMgr.loadModel("./src/main/resources/shacl/shapes.ttl")
  val testResourceDir = "./src/test/resources/"

  @Test
  def shaclTest(): Unit = {
    val webIdModel = RDFDataMgr.loadModel(testResourceDir.concat("jan.ttl"))
    val result = validate(webIdModel, shapeModel)

    println(s"Conforms: ${result.conforms()}")
    println("Violations:")
    result.violations.foreach(println(_))
    println("Infos:")
    result.infos.foreach(println(_))
  }

  def validate(webId: Model, shapesModel: Model): Result = {
    val shapesGraph = shapesModel.getGraph
    val dataGraph = webId.getGraph
    val shapes = Shapes.parse(shapesGraph)

    val report = ShaclValidator.get().validate(shapes, dataGraph)
    val result = new Result

    //full result
    val out = new ByteArrayOutputStream()
    RDFDataMgr.write(out, report.getModel, Lang.TTL)
    println(out.toString)
    result.setResult(out.toString)
    out.close()

    //set infos and violations
    QueryHandler.executeQuery(SelectQueries.resultSeverity(), report.getModel).foreach(
      solution => {
        if (solution.getResource("severity").getURI == "http://www.w3.org/ns/shacl#Violation") {
          result.addViolation(solution.getResource("focusNode").getURI, solution.getLiteral("message").getLexicalForm, solution.getResource("property").getURI)
        }
        else if (solution.getResource("severity").getURI == "http://www.w3.org/ns/shacl#Info") {
          result.addInfo(solution.getResource("focusNode").getURI, solution.getLiteral("message").getLexicalForm, solution.getResource("property").getURI)
        }
      }
    )
    result
  }

}

