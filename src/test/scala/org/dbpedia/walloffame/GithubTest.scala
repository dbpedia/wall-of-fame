package org.dbpedia.walloffame

import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.junit.jupiter.api.Test

import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import scala.beans.BeanProperty

class GithubTest {

  //  @Test
  //  def testGetToken():Unit={
  //    println(getToken())
  //  }

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