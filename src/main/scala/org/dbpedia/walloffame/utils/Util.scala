package org.dbpedia.walloffame.utils


import org.slf4j.LoggerFactory

import java.io.IOException
import java.net.{MalformedURLException, SocketTimeoutException, URL, UnknownHostException}
import javax.net.ssl.HttpsURLConnection
import scala.io.Source

object Util {

  val logger = LoggerFactory.getLogger("UtilLogger")

  def get(url: URL,
          connectTimeout: Int = 5000,
          readTimeout: Int = 5000,
          requestMethod: String = "GET"): Option[String] = {
    try {
      val connection = url.openConnection
      if (url.openConnection().isInstanceOf[HttpsURLConnection]) {
        connection.setConnectTimeout(connectTimeout)
        connection.setReadTimeout(readTimeout)
        val inputStream = connection.getInputStream
        val content = Source.fromInputStream(inputStream).mkString
        if (inputStream != null) inputStream.close
        Option(content)
      } else {
        val message = "inserted URL is not Https"
        logger.error(message)
        None
      }
    } catch {
      case malformedURLException: MalformedURLException => {
        logger.error(s"$url is malformed")
        None
      }
      case unknownHostException: UnknownHostException => {
        logger.error(s"host unknown")
        None
      }
      case ioexception: IOException => {
        logger.error(s"ioexception")
        None
      }
      case socketTimeoutException: SocketTimeoutException => {
        logger.error(s"SocketTimeoutException for $url")
        None
      }
    }
  }
}
