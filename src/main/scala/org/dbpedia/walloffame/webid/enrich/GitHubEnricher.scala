package org.dbpedia.walloffame.webid.enrich

import com.google.gson.Gson
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.message.BasicHeader
import org.apache.jena.rdf.model.{Model, ResourceFactory}
import org.dbpedia.walloffame.sparql.QueryHandler
import org.dbpedia.walloffame.sparql.queries.SelectQueries

import java.io.File
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import scala.beans.BeanProperty
import scala.collection.JavaConversions._

object GitHubEnricher {

  var gitHubToken = ""

  /**
   * get Token of Github (necessary for multiple Requests)
   */
  def setToken(client_id:String):Unit={

    val httpclient:CloseableHttpClient = HttpClients.createDefault()
    val response_DeviceCode = requestVerification(httpclient,client_id)

    println(s"""
         |Github Verification:
         |
         |Please enter the following Code here: ${response_DeviceCode.verification_uri}
         |Code: ${response_DeviceCode.user_code}
         |""".stripMargin)

    var access = new Response_AccessToken()
    val executor = new ScheduledThreadPoolExecutor(20)

    val runnable = new Runnable {
      def run():Unit = {
        val time = System.currentTimeMillis()
        access = requestToken(httpclient,client_id,response_DeviceCode.device_code)
        Option(access.access_token) match {
          case Some(token) =>
            executor.shutdownNow()
          case None =>
            println("Still waiting for the verification.")
            executor.schedule(this, response_DeviceCode.interval - (System.currentTimeMillis() - time)/1000, TimeUnit.SECONDS)
        }
      }
    }
    executor.schedule(runnable, response_DeviceCode.interval, TimeUnit.SECONDS)

    executor.awaitTermination(5, TimeUnit.MINUTES)

    gitHubToken = access.access_token
  }

  def requestToken(httpclient:CloseableHttpClient, client_id:String, deviceCode: String)={
    val url = "https://github.com/login/oauth/access_token"
    val json =
      s"""
         |{"client_id":"$client_id",
         |"device_code":"$deviceCode",
         |"grant_type":"urn:ietf:params:oauth:grant-type:device_code"}
         |""".stripMargin

    val responseBody = postRequest(httpclient, url, json)
    new Gson().fromJson(responseBody, classOf[Response_AccessToken])
  }

  def requestVerification(httpclient:CloseableHttpClient,client_id:String):Response_DeviceCode={
    val url="https://github.com/login/device/code"
    val json =
      s"""
         |{"client_id":"$client_id"}
         |""".stripMargin

    val responseBody = postRequest(httpclient, url, json)
    new Gson().fromJson(responseBody, classOf[Response_DeviceCode])
  }

  def postRequest(httpclient:CloseableHttpClient, url:String, jsonHeader:String):String={
    val httpPost = new HttpPost(url)
    httpPost.setHeader("Content-type", "application/json")
    httpPost.setHeader("Accept", "application/json")

    httpPost.setEntity(new StringEntity(jsonHeader))

    val response = httpclient.execute(httpPost)
    val responseBody = scala.io.Source.fromInputStream(response.getEntity.getContent, "UTF-8").mkString
    response.close()
    responseBody
  }



  def countAllGithubCommitsPerUser(owner:String="dbpedia", repo:String="extraction-framework"):collection.mutable.Map[String,Int]={
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
      case nullPointerException: NullPointerException => authorCount
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

class Response_DeviceCode(){
  @BeanProperty
  var device_code:String=_
  @BeanProperty
  var user_code:String=_
  @BeanProperty
  var verification_uri:String=_
  @BeanProperty
  var expires_in:Int=_
  @BeanProperty
  var interval:Int=_
}

class Response_AccessToken(){
  @BeanProperty
  var access_token:String=_
  @BeanProperty
  var token_type:String=_
  @BeanProperty
  var scope:String=_
}