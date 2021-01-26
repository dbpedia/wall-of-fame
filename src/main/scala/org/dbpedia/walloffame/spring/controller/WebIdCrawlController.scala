//package org.dbpedia.walloffame.spring.controller
//
//import java.io.ByteArrayOutputStream
//
//import org.apache.jena.riot.{Lang, RDFDataMgr}
//import org.dbpedia.walloffame.crawling.WebIdCrawler
//import org.dbpedia.walloffame.uniform.WebIdUniformer
//import org.springframework.stereotype.Controller
//import org.springframework.web.bind.annotation.RequestMethod.GET
//import org.springframework.web.bind.annotation.{RequestMapping, ResponseBody}
//
//@Controller
//class WebIdCrawlController {
//
//  @RequestMapping(value = Array("/getWebIds"), method = Array(GET))
//  //viewname is the path to the related jsp file
//  @ResponseBody
//  def crawlAndUniformWebIds(): String = {
//    val uniformedModel = WebIdUniformer.uniformWebIds(WebIdCrawler.crawl())
//
//    val out = new ByteArrayOutputStream()
//    RDFDataMgr.write(out, uniformedModel, Lang.TTL)
//    out.toString
//  }
//
//}
