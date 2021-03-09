package org.dbpedia.walloffame.spring.model

import org.apache.jena.rdf.model.Model
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.SelectQueries

import scala.beans.BeanProperty

class WebId() {

  @BeanProperty
  var url: String = _

  @BeanProperty
  var maker: String = _

  @BeanProperty
  var name: String = _

  @BeanProperty
  var img: String = _

  @BeanProperty
  var account: String = _

  @BeanProperty
  var numVersions: Int = _

  @BeanProperty
  var numArtifacts: Int = _

  @BeanProperty
  var uploadSize: Long = _

  @BeanProperty
  var geekCode: String = _

  @BeanProperty
  var turtle: String = _

  @BeanProperty
  var uniformedTurtle: String = _

  @BeanProperty
  var validation: Result = _

  def this(model: Model) {
    this()
    fetchFieldsWithModel(model)
  }

  def fetchFieldsWithModel(model: Model): Unit = {
    val data = QueryHandler.executeQuery(SelectQueries.getWebIdData(), model).head

    this.url = data.getResource("webid").toString
    this.maker = data.getResource("maker").toString
    this.name = data.getLiteral("name").getLexicalForm

    Option(data.getResource("img")) match {
      case Some(value) => this.img = value.toString
      case None => ""
    }

    Option(data.getLiteral("geekcode")) match {
      case Some(value) => this.geekCode = value.getLexicalForm
      case None => ""
    }

    Option(data.getResource("account")) match {
      case Some(value) =>
        this.account = value.toString.splitAt(value.toString.lastIndexOf("/")+1)._2
        this.numVersions = data.getLiteral("numVersions").getLexicalForm.toInt
        this.numArtifacts = data.getLiteral("numArtifacts").getLexicalForm.toInt
        this.uploadSize = data.getLiteral("uploadSize").getLexicalForm.toLong
      case None => ""
    }

  }

}
