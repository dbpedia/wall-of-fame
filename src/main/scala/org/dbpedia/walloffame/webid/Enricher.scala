package org.dbpedia.walloffame.webid

import com.google.gson.Gson
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.message.BasicHeader
import org.apache.jena.rdf.model.{Literal, Model, ResourceFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.SelectQueries
import org.dbpedia.walloffame.webid.WebIdUniformer.logger

import scala.beans.BeanProperty
import scala.collection.JavaConversions._

object Enricher {

  def enrichModelWithDatabusData(model:Model, personURL:String):Model={

    //    println(s"MAKER: $maker")
    checkForRelatedDatabusAccount(personURL) match {
      case None => logger.warn(s"No Dbpedia-Databus account found for $personURL")
      case Some(account) =>
        val result = QueryHandler.executeQuery(SelectQueries.getDatabusUserData(personURL)).head

        model.add(
          ResourceFactory.createStatement(
            ResourceFactory.createResource(personURL),
            ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/account"),
            ResourceFactory.createResource(account)
          )
        )

        def addDecimalLiteralToModel(prop:String, value:Literal) ={
          val ontology = "http://dbpedia.org/ontology/"
          model.add(
            ResourceFactory.createStatement(
              ResourceFactory.createResource(account),
              ResourceFactory.createProperty(ontology+prop),
              value
            )
          )
        }

        val numVersions = "numVersions"
        var value = {
          if (result.getLiteral(numVersions) != null) result.getLiteral(numVersions)
          else ResourceFactory.createTypedLiteral(0)
        }
        addDecimalLiteralToModel(numVersions, value)

        val numArtifacts = "numArtifacts"
        value = {
          if (result.getLiteral(numArtifacts) != null) result.getLiteral(numArtifacts)
          else ResourceFactory.createTypedLiteral(0)
        }
        addDecimalLiteralToModel(numArtifacts, value)

        val uploadSize = "uploadSize"
        value = {
          if(result.getLiteral(uploadSize) != null) {
            val uploadSizeAsMB = result.getLiteral(uploadSize).getLong  / 1024 / 1024
            ResourceFactory.createTypedLiteral(uploadSizeAsMB)
          } else {
            ResourceFactory.createTypedLiteral(0)
          }
        }
        addDecimalLiteralToModel(uploadSize, value)
    }

    model
  }

  def checkForRelatedDatabusAccount(webid:String):Option[String]={
    val model = RDFDataMgr.loadModel("https://databus.dbpedia.org/system/api/accounts", Lang.NTRIPLES)
    val solutions = QueryHandler.executeQuery(SelectQueries.checkForRelatedDatabusAccount(webid),model)

    if(solutions.nonEmpty) Option(solutions.head.getResource("acc").toString)
    else None
  }

  def countAllGithubCommitsPerUser(base_url:String="https://api.github.com", owner:String="dbpedia", repo:String="extraction-framework"):collection.mutable.Map[String,Int]={
    val url = s"$base_url/repos/$owner/$repo/commits"
    val header = new BasicHeader(HttpHeaders.AUTHORIZATION, "token 652b949998ceb5a4154b95123711aa401a6d8c36")
    val httpclient:CloseableHttpClient = HttpClients.custom().setDefaultHeaders(List(header)).build()

    def getRequest(httpClient: CloseableHttpClient, url:String):CloseableHttpResponse={
      val httpget:HttpGet = new HttpGet(url)
      httpClient.execute(httpget)
    }

    val httpResponse = getRequest(httpclient, url)
    val lastPage = httpResponse.getFirstHeader("Link").getElements.last.toString.split(">").head.split("=").last.toInt
    httpResponse.close()

    val authorCount = collection.mutable.Map[String, Int]().withDefaultValue(0)
    for( a <- 1 to lastPage) {
      val httpResponse = getRequest(httpclient, s"$url?page=$a&per_page=100")
      val partResult = scala.io.Source.fromInputStream(httpResponse.getEntity.getContent).mkString
      httpResponse.close()
      val commits = new Gson().fromJson(partResult, classOf[Array[GithubCommit]])
      commits.foreach(commit => {
        Option(commit.committer) match {
          case Some(committer) => authorCount(committer.html_url) +=1
          case None => println(s"${commit.url} has no Commiter")
        }
      })
    }
    authorCount
  }

  def enrichModelWithGithubData(model: Model, gitHubMap:collection.mutable.Map[String,Int], personURL:String):Model={
    val results = QueryHandler.executeQuery(SelectQueries.getGitHubAccount(personURL),model)
    if (results.nonEmpty) {
      Option(results.head.getResource("githubAccount")) match {
        case Some(githubAccount) =>
          if (gitHubMap.exists(_._1 == githubAccount.getURI)) {
            model.add(
              ResourceFactory.createStatement(
                ResourceFactory.createResource(personURL),
                ResourceFactory.createProperty("http://example.org/gitHubCommits"),
                ResourceFactory.createTypedLiteral(gitHubMap(githubAccount.getURI))
              )
            )
          }
        case None => println("no github account")
      }
    }
    model
  }


}

class GithubCommit(){
  @BeanProperty
  var url:String = _
  @BeanProperty
  var sha:String = _
  @BeanProperty
  var committer:Committer = _
}

class Committer(){
  @BeanProperty
  var login:String = _
  @BeanProperty
  var html_url:String =_
}