package org.dbpedia.walloffame.validation

import better.files.File
import org.apache.jena.rdf.model.{ModelFactory, Model}
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.jena.shacl.{ShaclValidator, Shapes}
import org.apache.jena.sparql.graph.GraphFactory
import org.dbpedia.walloffame.spring.model.Result
import org.dbpedia.walloffame.uniform.QueryHandler
import org.dbpedia.walloffame.uniform.queries.SelectQueries

import java.io.{ByteArrayOutputStream, File => JavaFile}

object WebIdValidator {


  def writeFile(str: String): File = {

    import java.io.PrintWriter
    val fileToValidate = File("./tmp/webIdToValidate.ttl")
    new PrintWriter(fileToValidate.toJava) {
      write(str)
      close
    }

    fileToValidate
  }


  def validate(webId: Model): Result = {

    import org.springframework.core.io.support.PathMatchingResourcePatternResolver
    val resolver = new PathMatchingResourcePatternResolver
    val resources = resolver.getResources("classpath:shacl/*.ttl")

    var result = new Result

    //iterate over all shapeFiles and validate for each
    val tmpShapeFile = File("./tmp/tmpShapeFile.ttl")
    for (resource <- resources) {
      //write shacl file out of jar, because Jena can't handle stream
      val is = resource.getInputStream
      val in = scala.io.Source.fromInputStream(is)
      val out = new java.io.PrintWriter(tmpShapeFile.toJava)
      try {
        in.getLines().foreach(out.println(_))
      }
      finally {
        out.close
      }

      val partResult = validate(webId, RDFDataMgr.loadModel(tmpShapeFile.pathAsString))

      tmpShapeFile.delete()
      result = partResult
    }

    result
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
}
