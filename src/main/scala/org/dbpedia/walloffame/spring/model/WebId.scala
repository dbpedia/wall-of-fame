package org.dbpedia.walloffame.spring.model

import org.apache.jena.rdf.model.Model
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.SelectQueries

import scala.beans.BeanProperty

class WebId() {

  @BeanProperty
  var general:GeneralInfo = new GeneralInfo

  @BeanProperty
  var others:AdditionalInfo = new AdditionalInfo

  @BeanProperty
  var databus:DatabusInfo = new DatabusInfo

  @BeanProperty
  var github:GithubInfo = new GithubInfo

  @BeanProperty
  var validation: Result = _

  def this(model: Model) {
    this()
    if(!model.isEmpty) fetchFieldsWithModel(model)
  }

  def fetchFieldsWithModel(model: Model): Unit = {
    val data = QueryHandler.executeQuery(SelectQueries.getWebIdData(), model).head
    this.general.url = data.getResource("webid").toString
    this.general.maker = data.getResource("maker").toString
    this.general.person = data.getResource("person").toString
    this.general.name = data.getLiteral("name").getLexicalForm

    Option(data.getResource("img")) match {
      case Some(value) => this.others.img = value.toString
      case None => ""
    }

    Option(data.getLiteral("geekcode")) match {
      case Some(value) => this.others.geekCode = value.getLexicalForm
      case None => ""
    }

    Option(data.getResource("dbpediaAccount")) match {
      case Some(value) =>
        this.databus.account = value.toString.splitAt(value.toString.lastIndexOf("/")+1)._2
        this.databus.numVersions = data.getLiteral("numVersions").getLexicalForm.toInt
        this.databus.numArtifacts = data.getLiteral("numArtifacts").getLexicalForm.toInt
        this.databus.uploadSize = data.getLiteral("uploadSize").getLexicalForm.toLong
      case None => ""
    }

    Option(data.getResource("githubAccount")) match {
      case Some(value) =>
        this.github.account = value.toString
      case None => ""
    }

    Option(data.getLiteral("gitHubCommits")) match {
      case Some(value) =>
        this.github.commits = value.getInt
      case None => ""
    }
  }

}

class GeneralInfo(){
  @BeanProperty
  var url: String = _

  @BeanProperty
  var maker: String = _

  @BeanProperty
  var person: String = _

  @BeanProperty
  var name: String = _

  @BeanProperty
  var turtle: String = _

  @BeanProperty
  var uniformedTurtle: String = _
}

class AdditionalInfo(){
  @BeanProperty
  var img: String = _

  @BeanProperty
  var geekCode: String = _
}

class DatabusInfo(){
  @BeanProperty
  var account: String = _

  @BeanProperty
  var numVersions: Int = _

  @BeanProperty
  var numArtifacts: Int = _

  @BeanProperty
  var uploadSize: Long = _
}

class GithubInfo(){
  @BeanProperty
  var account:String = _
  @BeanProperty
  var commits:Int= _
}