package org.dbpedia.walloffame.spring.controller

import com.google.gson.Gson
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.spring.model.{Result, WebId}
import org.dbpedia.walloffame.webid.WebIdHandler
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView

import java.net.{MalformedURLException, URL}
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

    val webIdHandler = new WebIdHandler
    val result = webIdHandler.validateWebId(webid, false)

    val newWebId = new WebId(result._1)
    newWebId.setValidation(result._2)

    try {
      //if webid is send as url
      new URL(webid)
      val src =  scala.io.Source.fromURL(webid)
      val turtle =
        s"""@base <$webid> .
           |${src.mkString}
           |""".stripMargin
      src.close()
      newWebId.general.setMaker(webid)
      newWebId.general.setTurtle(turtle.trim)
    } catch {
      case malformedURLException: MalformedURLException => {
        //if webId is send as plain text
        newWebId.general.setTurtle(webid.trim)
      }
      case e: Exception => ""
    }

    response.setStatus(200)
    writeWholeJsonOnRespone(newWebId, response)
  }

//  def handleWebId(webId: WebId, response: HttpServletResponse): Unit = {
//    try{
//      val model: Model = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(webId.turtle, "UTF-8"), "", "TURTLE")
//      val result = WebIdValidator.validate(model, config.shacl.url)
//
//      if (result.conforms()) {
//        //valid webid
//        val newModel = WebIdUniformer.uniform(model)
//        val newWebId = new WebId(newModel)
//        newWebId.setTurtle(webId.turtle)
//        newWebId.setUrl(webId.url)
//        newWebId.setAccount(webId.account)
//        newWebId.setValidation(result)
//
//        writeWholeJsonOnRespone(newWebId, response)
//        response.setStatus(200)
//      }
//      else {
//        webId.setValidation(result)
//        writeWholeJsonOnRespone(webId, response)
//      }
//    }
//    catch{
//      case riot: RiotException => handleException(webId, response,riot)
//    }
//  }
//
  def handleException(webId:WebId, response: HttpServletResponse, exception: Exception): Unit = {
    val result = new Result
    result.addViolation("Exception", exception.toString)
    webId.setValidation(result)

//    writeWholeJsonOnRespone(webId, response)
//    response.setStatus(200)
  }

  def writeWholeJsonOnRespone(webId:WebId, response: HttpServletResponse): Unit = {
    val gson = new Gson()
    val os = response.getOutputStream
    os.write(gson.toJson(webId).getBytes(StandardCharsets.UTF_8))
//    os.write(s""" "Result":${gson.toJson(result)}}""".getBytes(StandardCharsets.UTF_8))
  }

}
