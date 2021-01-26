package org.dbpedia.walloffame.spring.controller

import better.files.File
import org.apache.jena.rdf.model.Model
import org.dbpedia.walloffame.Config
import org.dbpedia.walloffame.convert.ModelToJSONConverter
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import virtuoso.jdbc4.VirtuosoException


@Controller
class WoFController {

  @Autowired
  private var config: Config = _

  @RequestMapping(value = Array("/walloffame"), method = Array(RequestMethod.GET))
  def getIndexPage(): String = {
    val optModels =
      try {
        VirtuosoHandler.getAllWebIds(config.virtuoso)
      } catch {
        case virtuosoException: VirtuosoException => Seq.empty[(String, Model)]
      }

    if (optModels.nonEmpty) ModelToJSONConverter.createJSONFile(optModels, File(config.exhibit.file))
    "walloffame"
  }

  @RequestMapping(value = Array("/result"), method = Array(RequestMethod.GET))
  def getResultPage(): String = {
    "result"
  }

  @RequestMapping(value = Array("/new"), method = Array(RequestMethod.GET))
  def getWOF(): String={
    val optModels =
      try {
        VirtuosoHandler.getAllWebIds(config.virtuoso)
      } catch {
        case virtuosoException: VirtuosoException => Seq.empty[(String, Model)]
      }

    if (optModels.nonEmpty) ModelToJSONConverter.createJSONFile(optModels, File(config.exhibit.file))
    "newwof"
  }

}
