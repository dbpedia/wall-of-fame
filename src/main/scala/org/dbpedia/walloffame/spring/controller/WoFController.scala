package org.dbpedia.walloffame.spring.controller

import better.files.File
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, ResponseBody}

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

  @RequestMapping(value = Array("/logs"), method = Array(RequestMethod.GET))
  def logs(response: HttpServletResponse): Unit = {

    val os = response.getOutputStream
    val source = Source.fromFile(File(config.log.file).toJava)

//    os.write("<html>".getBytes(StandardCharsets.UTF_8))
    source.getLines().foreach({line =>
      os.write(s"$line".getBytes(StandardCharsets.UTF_8))
    })
//    os.write("</html>".getBytes(StandardCharsets.UTF_8))

    response.setHeader("Content-Type","application/ld+json")
    response.setStatus(200)

    source.close()
  }

  @RequestMapping(value = Array("/webids.json"), method = Array(RequestMethod.GET), produces = Array("application/json; charset=utf-8"))
  @ResponseBody
  def getJson(response: HttpServletResponse): Unit = {

    val vos = new VirtuosoHandler(config.virtuoso)
    val json = vos.getAllWebIdsAsJson()

    if (json.nonEmpty) {
      println(json)

      val os = response.getOutputStream
      os.write(json.getBytes(StandardCharsets.UTF_8))
      os.close()
      response.setStatus(200)
    } else {
      response.setStatus(500)
    }

  }

}
