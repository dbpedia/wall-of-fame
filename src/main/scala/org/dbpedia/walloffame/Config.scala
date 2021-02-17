package org.dbpedia.walloffame

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

import scala.beans.BeanProperty

@Configuration
@ConfigurationProperties
case class Config() {
  @BeanProperty
  var virtuoso: VosConfig = new VosConfig
  @BeanProperty
  var shacl: ShaclConfig = new ShaclConfig
  @BeanProperty
  var log: LogConfig = new LogConfig
}

case class VosConfig() {
  @BeanProperty
  var url: String = _
  @BeanProperty
  var usr: String = _
  @BeanProperty
  var psw: String = _
  @BeanProperty
  var graph: String = _
}

case class ShaclConfig() {
  @BeanProperty
  var url: String = _
}

case class LogConfig(){
  @BeanProperty
  var file: String =_
}