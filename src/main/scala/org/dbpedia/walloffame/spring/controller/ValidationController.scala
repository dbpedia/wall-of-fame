package org.dbpedia.walloffame.spring.controller

import better.files.File
import org.apache.commons.io.IOUtils
import org.apache.jena.atlas.web.HttpException
import org.apache.jena.rdf.model.{Model, ModelFactory, RDFWriter}
import org.apache.jena.riot.{Lang, RDFDataMgr, RiotException, RiotNotFoundException}
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.convert.ModelToJSONConverter
import org.dbpedia.walloffame.spring.model.{Result, WebId}
import org.dbpedia.walloffame.uniform.WebIdUniformer
import org.dbpedia.walloffame.utils.Util
import org.dbpedia.walloffame.validation.WebIdValidator
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{GetMapping, ModelAttribute, PostMapping, RequestParam}
import org.springframework.web.servlet.ModelAndView

import java.io
import java.io.{ByteArrayOutputStream, FileInputStream, IOException}
import java.net.{MalformedURLException, SocketTimeoutException, URL, UnknownHostException}
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@Controller
class ValidationController(config: Config) {

  //value = Array("url") is the url the resulting site will be located at
  //viewname is the path to the related jsp file
  @GetMapping(value = Array("/", "/validate"))
  def getValidate(): ModelAndView = {

    val str =
      """
        |@base <https://raw.githubusercontent.com/Eisenbahnplatte/eisenbahnplatte.github.io/master/webid.ttl> .
        |@prefix foaf: <http://xmlns.com/foaf/0.1/> .
        |@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
        |@prefix cert: <http://www.w3.org/ns/auth/cert#> .
        |@prefix rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        |
        |<> a foaf:PersonalProfileDocument ;
        |   foaf:maker <#this> ;
        |   foaf:primaryTopic <#this> .
        |
        |<#this> a foaf:Person ;
        |   foaf:name "Eisenbahnplatte";
        |   foaf:img <https://eisenbahnplatte.github.io/eisenbahnplatte.jpeg>;
        |   foaf:gender "male";
        |   foaf:geekcode "GMU GCS s: d? !a L++ PS+++ PE- G h";
        |   foaf:firstname "Fabian";
        |
        |cert:key [
        |      a cert:RSAPublicKey;
        |      rdfs:label "HP Elitebook";
        |      cert:modulus "C133F14349AC1035EC007228975FA276E52A7D4E2F227710D645C616E92666C861838AFF268491990F9C30F6999E2C62DF3379DA0FDCE300CF1BED6B37F25FF9ADD5BD242E346E1C25E33891A95BD9B998D177D389A163B150383FE6EE1D9F479B2F186EF0BB11B4E8AC87AEB2414BA653741E87E8E72A083D00C813B1242158FFC957089C97044241DBC9CAE553CEE5B869A3667596E4E6A34998CEE9A588617B54432010CCDCF5EC7C4140B6AA3422AB089E5676847F727DA8762D1BA35FA4F0593AF91BFFA5AA4B433C07F1982CA22F1BEB1B538C8890632608C04E4A4E9129C1AA4575BAAE9014E30C0D7A5F96D98BCB4C5D0C794A8B5A2A7D823ECC5411"^^xsd:hexBinary;
        |      cert:exponent "65537"^^xsd:nonNegativeInteger
        |     ] .
        |""".stripMargin

    val model: Model = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(str, "UTF-8"), "", "TURTLE")

    val webId = new WebId(model)
    webId.setTurtle(str)
    webId.setUrl("https://raw.githubusercontent.com/Eisenbahnplatte/eisenbahnplatte.github.io/master/webid.ttl")

    val modelAndView = new ModelAndView("validate")
    modelAndView.addObject("webid", webId)
    modelAndView.addObject("result", new Result)
  }

  @PostMapping(value = Array("/", "validate"))
  def sendWebIdToValidate(@ModelAttribute("webid") webId: WebId): ModelAndView = {
    handlePostedWebId(webId)
  }

  def handlePostedWebId(webId: WebId): ModelAndView = {

    val modelAndView = new ModelAndView("validate")
    val model: Model = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(webId.turtle, "UTF-8"), "", "TURTLE")

    try {
      val result = WebIdValidator.validate(model)

      if (result.conforms()) {
        //valid webid
        val newWebId = new WebId(WebIdUniformer.uniform(model))
        newWebId.setTurtle(webId.turtle)
        newWebId.setUrl(webId.url)
        modelAndView.addObject("webid", newWebId)
        modelAndView.addObject("result", result)
      }
      else {
        //invalide webid
        modelAndView.addObject("webid", webId)
        modelAndView.addObject("result", result)
      }
    } catch {
      case riot: RiotException => handleException(modelAndView, webId, riot.toString)
    }
  }

  def handleException(modelAndView: ModelAndView, webId: WebId, exception: String): ModelAndView = {
    val result = new Result
    println(s"EXCEPTION:${exception}")
    result.addViolation("Exception", exception)
    modelAndView.addObject("webid", webId)
    modelAndView.addObject("result", result)
  }

  @PostMapping(value = Array("/fetchAndValidate"))
  def fetchAndValidate(@ModelAttribute("webid") webId: WebId): ModelAndView = {

    try {
      val model = RDFDataMgr.loadModel(webId.url.trim)

      val newWebId = new WebId(model)
      newWebId.setUrl(webId.url)

      val out = new ByteArrayOutputStream()
      RDFDataMgr.write(out, model, Lang.TURTLE)
      newWebId.setTurtle(out.toString)

      handlePostedWebId(newWebId)

    } catch {
      case unknownHostException: UnknownHostException => handleException(new ModelAndView("validate"), webId, unknownHostException.toString)
      case malformedURLException: MalformedURLException => handleException(new ModelAndView("validate"), webId, malformedURLException.toString)
      case ioexception: IOException => handleException(new ModelAndView("validate"), webId, ioexception.toString)
      case httpException: HttpException => handleException(new ModelAndView("validate"), webId, httpException.toString)
      case socketTimeoutException: SocketTimeoutException => handleException(new ModelAndView("validate"), webId, socketTimeoutException.toString)
      case riotNotFoundException: RiotNotFoundException => handleException(new ModelAndView("validate"), webId, riotNotFoundException.toString)
    }

    //      val turtle = Util.get(new URL(webId.url.trim))
    //      if (turtle.isDefined) {
    //        webId.setTurtle(turtle.get)
    //        validateStr(webId)
    //      }
    //      else {
    //        val result = new Result
    //        result.setResult("The URL you entered is not correct")
    //        new ModelAndView("validate", "result", result)
    //      }
  }


  @GetMapping(value = Array("/webids.js"), produces = Array("application/json"))
  def getJson(response: HttpServletResponse): Unit = {
    try {
      IOUtils.copy(new FileInputStream(new io.File(config.exhibit.file)), response.getOutputStream)
      response.setStatus(200)
    } catch {
      case e: Exception => response.setStatus(500)
    }
  }

}
