package org.dbpedia.walloffame.webid.enrich.github

import com.google.gson.Gson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}

import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import scala.beans.BeanProperty

object GithubTokenHandler {

  /**
   * get Token of Github (necessary for multiple Requests)
   */
  def setToken(client_id:String):String={

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