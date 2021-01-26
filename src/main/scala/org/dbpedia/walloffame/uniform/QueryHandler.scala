package org.dbpedia.walloffame.uniform

import org.apache.jena.JenaRuntime
import org.apache.jena.query._
import org.apache.jena.rdf.model.{Model, ModelFactory, RDFNode}

object QueryHandler {

  JenaRuntime.isRDF11 = false

  def executeQuery(queryString: String, model: Model): Seq[QuerySolution] = {

    val query: Query = QueryFactory.create(queryString)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)

    var resultSeq: Seq[QuerySolution] = Seq.empty

    try {
      val results: ResultSet = qexec.execSelect
      while (results.hasNext) {
        val result = results.next()
        resultSeq = resultSeq :+ result
      }
    } finally qexec.close()

    resultSeq
  }

  def executeSingleResultQuery(queryString: String, model: Model): RDFNode = {

    val query: Query = QueryFactory.create(queryString)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)

    try {
      val result = qexec.execSelect.next
      val varName = result.varNames().next()
      result.get(varName)
    } finally qexec.close()
  }

  def executeConstructQuery(queryString: String, model: Model, constructedModel: Model = ModelFactory.createDefaultModel()): Boolean = {

    val query: Query = QueryFactory.create(queryString)
    val qexec: QueryExecution = QueryExecutionFactory.create(query, model)


    try {
      val sizeBefore = constructedModel.size()
      qexec.execConstruct(constructedModel)
      if (sizeBefore < constructedModel.size()) true
      else false
    } finally qexec.close()
  }

  def getSingleResult(solutions: Seq[QuerySolution]): RDFNode = {
    val sparqlVar = solutions.head.varNames().next()
    solutions.head.get(sparqlVar)
  }
}
