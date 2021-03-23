package org.dbpedia.walloffame

import com.google.gson.Gson
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.message.BasicHeader
import org.junit.jupiter.api.Test

import java.io.File
import scala.beans.BeanProperty
import scala.collection.JavaConversions._

class GithubTest {

  val gitHubToken = "6f7654b2f4e816f2f0a1fa500cca58ea468c2480"
  @Test
  def testest():Unit={
    println("start")
    val map = countAllGithubCommitsPerUser()
    val pw = new java.io.PrintWriter(new File("github.csv"))

    map.foreach(tuple => pw.write(s"${tuple._1},${tuple._2}\n"))
    pw.close()
  }

  def countAllGithubCommitsPerUser(owner:String="dbpedia", repo:String="extraction-framework"):collection.mutable.Map[String,Int]={
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

    val authorCount = collection.mutable.Map[String, Int]().withDefaultValue(0)
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
    authorCount
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
