package org.dbpedia.walloffame.spring.controller

import better.files.File
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, ResponseBody}

import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletResponse
import scala.io.Source


@Controller
class WoFController {

  @Autowired
  private var config: Config = _

  @RequestMapping(value = Array("/","/walloffame"), method = Array(RequestMethod.GET))
  def getIndexPage(): String = {
    "walloffame"
  }

  @RequestMapping(value = Array("/about"), method = Array(RequestMethod.GET))
  def getAboutPage(): String = {
    "about"
  }

  @RequestMapping(value = Array("/logs"), method = Array(RequestMethod.GET), produces = Array("application/ld+json; charset=utf-8"))
  def logs(response: HttpServletResponse): Unit = {
    response.setHeader("Content-Type","application/ld+json")

    val os = response.getOutputStream
    try{
      val source = Source.fromFile(File(config.log.file).toJava)
      source.getLines().foreach({line =>
        os.write(s"$line".getBytes(StandardCharsets.UTF_8))
      })
      source.close()
    } catch {
      case fileNotFoundException: FileNotFoundException => println("logfile not ready yet")
    }

    response.setStatus(200)
  }

  @RequestMapping(value = Array("/webids.json"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  @ResponseBody
  def getJson(response: HttpServletResponse): Unit = {
    val vos = new VirtuosoHandler(config.virtuoso)
    val json = vos.getAllWebIdsAsJson()

    if (json.nonEmpty) {
      val os = response.getOutputStream
      os.write(json.getBytes(StandardCharsets.UTF_8))
      os.close()
      response.setStatus(200)
    } else {
      response.setStatus(500)
    }

  }

}
