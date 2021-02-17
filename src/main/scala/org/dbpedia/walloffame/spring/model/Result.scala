package org.dbpedia.walloffame.spring.model

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

}

