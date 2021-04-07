package org.dbpedia.walloffame.webid.enrich.github

import better.files.File
import com.google.gson.Gson
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.message.BasicHeader
import org.apache.jena.rdf.model.{Model, ResourceFactory}
import org.apache.juli.logging.{Log, LogFactory}
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.SelectQueries

import java.net.SocketException
import javax.net.ssl.SSLException
import scala.beans.BeanProperty
import scala.collection.JavaConversions._
import scala.collection.mutable

object GitHubEnricher {

  val logger: Log = LogFactory.getLog(getClass)

  def getRequest(httpClient: CloseableHttpClient, url:String):CloseableHttpResponse={
    val httpget:HttpGet = new HttpGet(url)
    println(url)
    val response = httpClient.execute(httpget)
    response.getAllHeaders.foreach(println(_))
    println("Got Response")
    response
  }

  def countCommitsPerUserOfAllRepos(gitHubToken:String, owner:String="dbpedia"):Seq[Repo]={
    try {
      val url = s"https://api.github.com/users/$owner/repos"
      val header = new BasicHeader(HttpHeaders.AUTHORIZATION, s"token $gitHubToken")
      val httpclient: CloseableHttpClient = HttpClients.custom().setDefaultHeaders(List(header)).build()


      val httpResponse = getRequest(httpclient, url)
      val lastPage = httpResponse.getFirstHeader("Link").getElements.last.toString.split(">").head.split("=").last.toInt
      httpResponse.close()
    }

    Seq.empty[Repo]
  }

  def countAllGithubCommitsPerUser(gitHubToken:String, owner:String="dbpedia", repo:String="extraction-framework"):collection.mutable.Map[String,Int]={

    println(s"""
               |Github Commit Counter:
               |Count all commits of Repo: https://github.com/$owner/$repo
               |""".stripMargin)

    val authorCount = collection.mutable.Map[String, Int]().withDefaultValue(0)

    try{
      val url = s"https://api.github.com/repos/$owner/$repo/commits"
      val header = new BasicHeader(HttpHeaders.AUTHORIZATION, s"token $gitHubToken")
      val httpclient:CloseableHttpClient = HttpClients.custom().setDefaultHeaders(List(header)).build()



      val httpResponse = getRequest(httpclient, url)
      val lastPage = httpResponse.getFirstHeader("Link").getElements.last.toString.split(">").head.split("=").last.toInt
      println(s"LASTPAGE: $lastPage")
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
            case None =>
//              println(s"${commit.url} has no Author")
              logger.debug(s"${commit.url} has no Author")
          }
        })
      }

      writeMapToFile(owner, repo, authorCount)
      authorCount
    } catch {
      case nullPointerException: NullPointerException => catchCommitCountExceptions(nullPointerException)
      case sslException: SSLException => catchCommitCountExceptions(sslException)
      case socketException: SocketException => catchCommitCountExceptions(socketException)
      case e:Exception => catchCommitCountExceptions(e)
    }

  }

  def catchCommitCountExceptions(e:Exception):mutable.Map[String, Int] ={
    logger.error(e)
    collection.mutable.Map[String, Int]().withDefaultValue(0)
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

  def writeMapToFile(owner:String, repo:String, map:collection.mutable.Map[String, Int]):Unit={
    val file = File(s"./tmp/github/$owner/$repo.csv")
    file.parent.createDirectoryIfNotExists(createParents = true)
    file.delete(swallowIOExceptions = true)

    val pw = new java.io.PrintWriter(file.toJava)
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

class Repo(){
  @BeanProperty
  var name:String =_
  @BeanProperty
  var commitsPerUser:collection.mutable.Map[String,Int] = _
}