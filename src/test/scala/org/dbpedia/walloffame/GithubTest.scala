package org.dbpedia.walloffame

import com.google.gson.Gson
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.message.BasicHeader
import org.apache.juli.logging.LogFactory
import org.dbpedia.walloffame.webid.enrich.github.GitHubEnricher.{catchCommitCountExceptions, writeMapToFile}
import org.dbpedia.walloffame.webid.enrich.github.{GithubTokenHandler, Repo}
import org.junit.jupiter.api.Test

import java.net.SocketException
import scala.collection.JavaConversions._
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import javax.net.ssl.SSLException
import scala.beans.BeanProperty

class GithubTest {

  val logger=LogFactory.getLog(getClass)
//  val token ="b94fce080ed19ba15beab030ec660ee6640816ff"
  val token = GithubTokenHandler.getToken("4b7a8dc331564a418882")

  @Test
  def testCountAllCommits():Unit={

    val repoStats = countCommitsPerUserOfAllRepos(token)

    repoStats.foreach(repo => {
      println(s"REPO :${repo.name}")
      repo.commitsPerUser.foreach(x => println(s"${x._1} => ${x._2}"))
    })
  }

//  @Test
//  def testCountMappingToolRepo():Unit={
//    val a = countAllGithubCommitsPerUser(token, repo= "mapping-tool")
//
//  }

  def getRequest(httpClient: CloseableHttpClient, url:String):CloseableHttpResponse={
    val httpget:HttpGet = new HttpGet(url)
    println(url)
    val response = httpClient.execute(httpget)
    response.getAllHeaders.foreach(println(_))
    println("Got Response")
    response
  }

  def countAllGithubCommitsPerUser(gitHubToken:String, owner:String="dbpedia", repo:String="extraction-framework"):collection.mutable.Map[String,Int]={

    val authorCount = collection.mutable.Map[String, Int]().withDefaultValue(0)
    val url = s"https://api.github.com/repos/$owner/$repo/commits"

    println(s"""
               |Github Commit Counter:
               |Count all commits of Repo: https://github.com/$owner/$repo (API-URL: $url)
               |""".stripMargin)

    val header = new BasicHeader(HttpHeaders.AUTHORIZATION, s"token $gitHubToken")
    val httpclient:CloseableHttpClient = HttpClients.custom().setDefaultHeaders(List(header)).build()

    try{
      val httpResponse = getRequest(httpclient, url)
      if (httpResponse.getStatusLine.getStatusCode == 200) {
        println("Statuscode 200")
        if(httpResponse.getFirstHeader("Link") == null) {
          println("only one page")
          countCommitsPerUser(httpclient, url, 1, authorCount)
        }
        else {
          val lastPage = httpResponse.getFirstHeader("Link").getElements.last.toString.split(">").head.split("=").last.toInt
          val newLastPage = math.ceil(lastPage.toDouble * 30 / 100).toInt
          println(s"LASTPAGE= $newLastPage")
          for(page <- 1 to newLastPage) {
            countCommitsPerUser(httpclient, url, page, authorCount)
          }
        }
      } else {
        println("Statuscode 500")
        println(httpResponse.getStatusLine)
      }
      httpResponse.close()
      writeMapToFile(owner, repo, authorCount)
      authorCount.foreach(x => println(s"${x._1} => ${x._2}"))
      authorCount
    } catch {
      case sslException: SSLException => catchCommitCountExceptions(sslException)
      case socketException: SocketException => catchCommitCountExceptions(socketException)
      case e:Exception => catchCommitCountExceptions(e)
    } finally {
      println("finally")
      httpclient.close()
    }

  }

  def countCommitsPerUser(httpClient: CloseableHttpClient, url:String, page:Int, authorCount:collection.mutable.Map[String, Int])={
    val httpResponse = getRequest(httpClient, s"$url?page=$page&per_page=100")
    println(s"RATE LIMIT: ${httpResponse.getFirstHeader("x-ratelimit-remaining").getValue}")
    val partResult = scala.io.Source.fromInputStream(httpResponse.getEntity.getContent).mkString
    httpResponse.close()
    val commits = new Gson().fromJson(partResult, classOf[Array[GithubCommit]])
    commits.foreach(commit => {
      Option(commit.author) match {
        case Some(author) => authorCount(author.html_url) +=1
        case None =>
          println(s"${commit.url} has no Author")
          logger.debug(s"${commit.url} has no Author")
      }
    })
  }

    @Test
    def testGetToken():Unit={
      countCommitsPerUserOfAllRepos(" b94fce080ed19ba15beab030ec660ee6640816ff").foreach(repo => {
        println(repo.name)
        repo.commitsPerUser.foreach(x => println(s"User: ${x._1} \t Commits: ${x._2}"))
      })
    }

  def getLastPage(gitHubUrl:String, httpclient:CloseableHttpClient, gitHubToken:String):Int ={
    try{
      val httpResponse = getRequest(httpclient, gitHubUrl)
      val lastPage = httpResponse.getFirstHeader("link").getElements.last.toString.split(">").head.split("=").last.toInt
      httpResponse.close()

      lastPage
    } catch {
      case exception: Exception =>
        logger.error(exception)
        0
    }
  }

  def countCommitsPerUserOfAllRepos(gitHubToken:String, owner:String="dbpedia"):Seq[Repo]={

    val header = new BasicHeader(HttpHeaders.AUTHORIZATION, s"token $gitHubToken")
    val httpclient:CloseableHttpClient = HttpClients.custom().setDefaultHeaders(List(header)).build()

    try{
      val url = s"https://api.github.com/orgs/$owner/repos"
      val lastPage = getLastPage(url, httpclient, gitHubToken)
      var repos = Array.empty[Repo]

      for(page <- 1 to lastPage) {
        val httpResponse = getRequest(httpclient, s"$url?page=$page")
        val partResult = scala.io.Source.fromInputStream(httpResponse.getEntity.getContent).mkString
        httpResponse.close()
        val newRepos = new Gson().fromJson(partResult, classOf[Array[Repo]])
        repos ++= newRepos
      }

      repos.foreach(repo => println(repo.name))
      repos.foreach(repo =>{
        repo.commitsPerUser = countAllGithubCommitsPerUser(gitHubToken, owner=owner, repo=repo.name)
      })

      repos
    } catch {
      case e:Exception =>
        logger.error(e)
        Seq.empty[Repo]
    } finally {
      httpclient.close()
    }
  }

  def requestToken():String={
    val client_id="4b7a8dc331564a418882"

    val httpclient:CloseableHttpClient = HttpClients.createDefault()
    val response_DeviceCode = requestVerification(httpclient,client_id)

    println(s"Please the following Code your device here: ${response_DeviceCode.verification_uri}")
    println(s"Code: ${response_DeviceCode.user_code}")

    var access = new Response_AccessToken()
    val executor = new ScheduledThreadPoolExecutor(20)
    val runnable = new Runnable {
      def run():Unit = {
        val time = System.currentTimeMillis()
        access = requestToken(httpclient,client_id,response_DeviceCode.device_code)
        if (access.access_token==null) {
          println("Still waiting for the verification.")
          executor.schedule(this, response_DeviceCode.interval - (System.currentTimeMillis() - time)/1000, TimeUnit.SECONDS).get
        }
      }
    }
    executor.schedule(runnable, response_DeviceCode.interval, TimeUnit.SECONDS).get

    access.access_token
  }

  def requestToken(httpclient:CloseableHttpClient, client_id:String, deviceCode: String):Response_AccessToken={
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

  @Test
  def test()={
    for(page <- 1 to 0) {
      println("asd")
    }
  }
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


//  @Test
//  def testestest():Unit={
//    val httpclient:CloseableHttpClient = HttpClients.createDefault()
//    val httpPost = new HttpPost("https://github.com/login/oauth/access_token")
//    httpPost.setHeader("Content-type", "application/json")
//    httpPost.setHeader("Accept", "application/json")
//
//    val json =
//      s"""
//         |{"client_id":"4b7a8dc331564a418882",
//         |"device_code":"b576173b6d3cb66a2a4b9239e2fe9ec0e8e7ed4c",
//         |"grant_type":"urn:ietf:params:oauth:grant-type:device_code"}
//         |""".stripMargin
//    httpPost.setEntity(new StringEntity(json))
//    val response = httpclient.execute(httpPost)
//    val responseBody = scala.io.Source.fromInputStream(response.getEntity.getContent, "UTF-8").mkString
//    response.close()
//    val accessToken = new Gson().fromJson(responseBody, classOf[Response_AccessToken]).access_token
//    println(accessToken)
//  }
//
//  @Test
//  def testScheduled():Unit={
//    val ex = new ScheduledThreadPoolExecutor(1)
//    val task = new Runnable {
//      def run() = println("Beep!")
//    }
//    val f = ex.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS)
//
//    println(f.get())
//    f.cancel(false)
//  }