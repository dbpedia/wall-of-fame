package org.dbpedia.walloffame.webid

import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.jena.shacl.{ShaclValidator, Shapes}
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.SelectQueries
import org.dbpedia.walloffame.spring.model.Result

import java.io.ByteArrayOutputStream

object WebIdValidator {

//  def validate(webId: Model): Result = {
//
//    import org.springframework.core.io.support.PathMatchingResourcePatternResolver
//    val resolver = new PathMatchingResourcePatternResolver
//    val resources = resolver.getResources("classpath:shacl/*.ttl")
//
//    var result = new Result
//
//    //iterate over all shapeFiles and validate for each
//    val tmpShapeFile = File("./tmp/tmpShapeFile.ttl")
//    tmpShapeFile.parent.createDirectoryIfNotExists()
//    for (resource <- resources) {
//      //write shacl file out of jar, because Jena can't handle stream
//      val is = resource.getInputStream
//      val in = scala.io.Source.fromInputStream(is)
//      val out = new java.io.PrintWriter(tmpShapeFile.toJava)
//      try {
//        in.getLines().foreach(out.println(_))
//      }
//      finally {
//        out.close
//      }
//
//      val partResult = validate(webId, RDFDataMgr.loadModel(tmpShapeFile.pathAsString))
//
//      tmpShapeFile.delete()
//      result = partResult
//    }
//
//    result
//  }

  def validate(webId: Model, shapesURL: String): Result = {
//        val shapes = RDFDataMgr.loadModel("./src/main/resources/shacl/shapes.ttl")
    val shapes = RDFDataMgr.loadModel(shapesURL)
    validate(webId, shapes)
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
    result.setResult(out.toString)
    out.close()

    //set infos and violations
    QueryHandler.executeQuery(SelectQueries.resultSeverity, report.getModel).foreach(
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
