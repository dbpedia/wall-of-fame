package org.dbpedia.walloffame.webid.enrich.github

import com.google.gson.Gson
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.message.BasicHeader
import org.apache.jena.rdf.model.{Model, ResourceFactory}
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.SelectQueries

import java.io.File
import scala.beans.BeanProperty
import scala.collection.JavaConversions._

object GitHubEnricher {

  def countAllGithubCommitsPerUser(gitHubToken:String, owner:String="dbpedia", repo:String="extraction-framework"):collection.mutable.Map[String,Int]={
    val authorCount = collection.mutable.Map[String, Int]().withDefaultValue(0)

    try{
      val url = s"https://api.github.com/repos/$owner/$repo/commits"
      val header = new BasicHeader(HttpHeaders.AUTHORIZATION, s"token $gitHubToken")
      val httpclient:CloseableHttpClient = HttpClients.custom().setDefaultHeaders(List(header)).build()

      def getRequest(httpClient: CloseableHttpClient, url:String):CloseableHttpResponse={
        val httpget:HttpGet = new HttpGet(url)
        httpClient.execute(httpget)
      }

      val httpResponse = getRequest(httpclient, url)
      val lastPage = httpResponse.getFirstHeader("Link").getElements.last.toString.split(">").head.split("=").last.toInt
      httpResponse.close()

//      println(s"LASTPAGE:$lastPage")
      for(page <- 1 to lastPage) {
        val httpResponse = getRequest(httpclient, s"$url?page=$page&per_page=100")
        val partResult = scala.io.Source.fromInputStream(httpResponse.getEntity.getContent).mkString
        httpResponse.close()
        val commits = new Gson().fromJson(partResult, classOf[Array[GithubCommit]])
        commits.foreach(commit => {
          Option(commit.author) match {
            case Some(author) => authorCount(author.html_url) +=1
            case None => println(s"${commit.url} has no Author")
          }
        })
      }

      writeMapToFile(authorCount)
      authorCount
    } catch {
      case nullPointerException: NullPointerException =>
        authorCount
    }

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
        case None => ""
      }
    }
    model
  }

  def writeMapToFile(map:collection.mutable.Map[String, Int]):Unit={
    val file = new File("./tmp/github.csv")
    if (file.exists()) file.delete()
    val pw = new java.io.PrintWriter(file)
    map.foreach(tuple => pw.write(s"${tuple._1},${tuple._2}\n"))
    pw.close()
  }
}

class GithubCommit(){
  @BeanProperty
  var url:String = _
  @BeanProperty
  var sha:String = _
  @BeanProperty
  var author:Author = _
}

class Author(){
  @BeanProperty
  var login:String = _
  @BeanProperty
  var html_url:String =_
}

