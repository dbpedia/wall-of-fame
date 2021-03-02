package org.dbpedia.walloffame.spring.model

import scala.beans.BeanProperty

class Result {

  @BeanProperty
  var result: String = _

  @BeanProperty
  var violations: Array[(String, String, String)] = Array.empty

  @BeanProperty
  var infos: Array[(String, String, String)] = Array.empty

  def conforms(): Boolean = violations.isEmpty

  def addViolation(focusNode: String, message: String, property: String =""): Unit = {
    violations = violations :+ (focusNode, message, property)
  }

  def addInfo(focusNode: String, message: String, property: String =""): Unit = {
    infos = infos :+ (focusNode, message, property)
  }

}

