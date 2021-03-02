package org.dbpedia.walloffame.spring.controller

import com.google.gson.Gson
import org.apache.commons.io.IOUtils
import org.apache.jena.atlas.web.HttpException
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.RiotException
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.spring.model.{Result, WebId}
import org.dbpedia.walloffame.uniform.WebIdUniformer
import org.dbpedia.walloffame.validation.WebIdValidator
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView

import java.io.IOException
import java.net.{MalformedURLException, SocketTimeoutException, URL, UnknownHostException}
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletResponse

@Controller
class ValidationController(config: Config) {

  //value = Array("url") is the url the resulting site will be located at
  //viewname is the path to the related jsp file
  @RequestMapping(value = Array("/validator"), method = Array(RequestMethod.GET))
  def getValidator(): ModelAndView = {
    new ModelAndView("validator")
  }

  @RequestMapping(value = Array("/validate"), method= Array(RequestMethod.GET))
  def validate(@RequestParam webid:String, response: HttpServletResponse): Unit = {

    response.setHeader("Content-Type","application/json")
    val newWebId = new WebId()

    try {
      //if webid is send as url
      new URL(webid)
      newWebId.setUrl(webid)

      val vos = new VirtuosoHandler(config.virtuoso)
      println("nun")
      println(vos.getAccountOfWebId(webid))
      newWebId.setAccount(vos.getAccountOfWebId(webid).getOrElse(""))
      val src =  scala.io.Source.fromURL(webid)
      val turtle =
        s"""@base <$webid> .
           |${src.mkString}
           |""".stripMargin
      src.close()
      newWebId.setTurtle(turtle)

      handleWebId(newWebId, response)
    } catch {
      case malformedURLException: MalformedURLException => {
        //if webId is send as plain text
        newWebId.setTurtle(webid.trim)
        handleWebId(newWebId, response)
      }
      case unknownHostException: UnknownHostException => handleException(newWebId, response, unknownHostException)
      case ioexception: IOException => handleException(newWebId, response, ioexception)
      case httpException: HttpException => handleException(newWebId, response, httpException)
      case socketTimeoutException: SocketTimeoutException => handleException(newWebId, response, socketTimeoutException)
    }
//    val newWebId = new WebId()
//
//    try {
//      //check if webid was send as url or as plain text
//      new URL(webid)
//      newWebId.setUrl(webid)
//
//      try {
//        //if webid is send as url
//        val src =  scala.io.Source.fromURL(webid)
//        val turtle =
//          s"""@base <$webid> .
//             |${src.mkString.trim}
//             |""".stripMargin
//
//        src.close()
//
//        newWebId.setTurtle(turtle)
//        handleWebId(newWebId, response)
//      } catch {
//        case unknownHostException: UnknownHostException => handleException(newWebId, response, unknownHostException)
//        case malformedURLException: MalformedURLException => handleException(newWebId, response, malformedURLException)
//        case ioexception: IOException => handleException(newWebId, response, ioexception)
//        case httpException: HttpException => handleException(newWebId, response, httpException)
//        case socketTimeoutException: SocketTimeoutException => handleException(newWebId, response, socketTimeoutException)
//      }
//    } catch {
//      case malformedURLException: MalformedURLException => {
//        //if webId is send as plain text
//        newWebId.setTurtle(webid.trim)
//        handleWebId(newWebId, response)
//      }
//    }

  }

  def handleWebId(webId: WebId, response: HttpServletResponse): Unit = {
    try{
      val model: Model = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(webId.turtle, "UTF-8"), "", "TURTLE")
      val result = WebIdValidator.validate(model, config.shacl.url)

      if (result.conforms()) {
        //valid webid
        val newModel = WebIdUniformer.uniform(model, result.getInfos)
        val newWebId = new WebId(newModel)
        newWebId.setTurtle(webId.turtle)
        newWebId.setUrl(webId.url)
        newWebId.setAccount(webId.account)
        newWebId.setValidation(result)

        writeWholeJsonOnRespone(newWebId, response)
        response.setStatus(200)
      }
      else {
        webId.setValidation(result)
        writeWholeJsonOnRespone(webId, response)
      }
    }
    catch{
      case riot: RiotException => handleException(webId, response,riot)
    }
  }

  def handleException(webId:WebId, response: HttpServletResponse, exception: Exception): Unit = {
    val result = new Result
    println(exception.toString)
    result.addViolation("Exception", exception.toString)
    webId.setValidation(result)

    writeWholeJsonOnRespone(webId, response)
    response.setStatus(200)
  }

  def writeWholeJsonOnRespone(webId:WebId, response: HttpServletResponse): Unit = {
    val gson = new Gson()
    val os = response.getOutputStream
    os.write(gson.toJson(webId).getBytes(StandardCharsets.UTF_8))
//    os.write(s""" "Result":${gson.toJson(result)}}""".getBytes(StandardCharsets.UTF_8))
  }

}
