package org.dbpedia.walloffame.spring.model

import org.apache.jena.rdf.model.Model
import org.dbpedia.walloffame.uniform.QueryHandler
import org.dbpedia.walloffame.uniform.queries.{SelectOptionalQueries, SelectQueries}

import scala.beans.BeanProperty

class WebId() {

  @BeanProperty
  var account: String = _

  @BeanProperty
  //  @Pattern(regexp = "^https://.*")
  var url: String = _

  @BeanProperty
  var name: String = _

  @BeanProperty
  var maker: String = _

  @BeanProperty
  var img: String = _

  @BeanProperty
  var numUploads: Int = _

  @BeanProperty
  var uploadSize: Long = _

  @BeanProperty
  var geekCode: String = _

  @BeanProperty
  var turtle: String = _

  @BeanProperty
  var validation: Result = _


  def this(model: Model) {
    this()
    fetchFieldsWithModel(model)
  }

  def fetchFieldsWithModel(model: Model): Unit = {
    val thisModel = model.listStatements()
    while(thisModel.hasNext) println(thisModel.nextStatement())

    val mandatory = QueryHandler.executeQuery(SelectQueries.getQueryWebIdData(), model).head

    this.url = mandatory.getResource("?webid").toString
    this.name = mandatory.getLiteral("?name").getLexicalForm
    this.maker = mandatory.getResource("?maker").toString


    val userUploadData = QueryHandler.executeQuery(SelectQueries.getUploadData(), model).head
//    println("jetzt")
//    println(userUploadData)
//    println(userUploadData.getLiteral("numUploads"))
    this.account = userUploadData.getResource("account").toString
    this.account = this.account.splitAt(this.account.lastIndexOf("/")+1)._2
//    println(account)
    this.numUploads = userUploadData.getLiteral("numUploads").getLexicalForm.toInt
    this.uploadSize = userUploadData.getLiteral("uploadSize").getLexicalForm.toLong

//    println(this.uploadSize)

    var optional = QueryHandler.executeQuery(SelectOptionalQueries.queryImg(), model)
    if (optional.nonEmpty) this.img = optional.head.getResource("?img").toString

    optional = QueryHandler.executeQuery(SelectOptionalQueries.queryGeekCode(), model)
    if (optional.nonEmpty) this.geekCode = optional.head.getLiteral("?geekcode").getLexicalForm
  }

}
