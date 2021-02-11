package org.dbpedia.walloffame.spring.model

import org.dbpedia.walloffame.logging.HtmlLogger

import scala.beans.BeanProperty

class Result {

  @BeanProperty
  var result: String = _

  @BeanProperty
  var violations: Array[(String, String)] = Array.empty

  @BeanProperty
  var infos: Array[(String, String)] = Array.empty

  def conforms(): Boolean = violations.isEmpty

  def addViolation(focusNode: String, message: String): Unit = {
    violations = violations :+ (focusNode, message)
  }

  def addInfo(focusNode: String, message: String): Unit = {
    infos = infos :+ (focusNode, message)
  }

  def logResults():Unit={
    log(violations)
    log(infos)
    if(violations.nonEmpty || infos.nonEmpty) HtmlLogger.append("")
  }

  def log(array:Array[(String,String)]):Unit={
    array.foreach(tuple=>{
      HtmlLogger.append(s"${tuple._1}: ${tuple._2}")
    })
  }
  //  def prepareResult():Unit={
  //    infos.foreach(tuple=>{
  //      if (tuple._2.contains("dbo:DBpedian")) {
  //        infos.update(infos.indexOf(tuple), (tuple._1, tuple._2.concat(" <img th:src=\"@{images/DBpedia_favicon.png}\"/>")))
  //      }
  //    })
  //  }
}

