package org.dbpedia.walloffame

import com.google.gson.Gson
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.message.BasicHeader
import org.junit.jupiter.api.Test

import scala.beans.BeanProperty
import scala.collection.JavaConversions._

class GithubTest {

  @Test
  def testest()={
    countAllCommitsPerUser()
  }

  def countAllCommitsPerUser(base_url:String="https://api.github.com", owner:String="dbpedia", repo:String="extraction-framework"):collection.mutable.Map[String,Int]={
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
      val httpResponse = getRequest(httpclient, s"$url?page=$a")
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
