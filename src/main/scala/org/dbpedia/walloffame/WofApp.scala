package org.dbpedia.walloffame

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.{ComponentScan, Configuration, FilterType}
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableAutoConfiguration
@ComponentScan(
  basePackages = Array("org.dbpedia.walloffame"),
  excludeFilters = Array(new ComponentScan.Filter(`type` = FilterType.REGEX, pattern = Array("org.dbpedia.walloffame.DatabusApp", "org.dbpedia.walloffame.DatabusInitRunner")))
)
@EnableScheduling
class WofApp extends SpringBootServletInitializer{
  @Override
  protected override def configure(application:SpringApplicationBuilder):SpringApplicationBuilder ={
    application.sources(WofApp.getClass)
  }
}


object WofApp {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[WofApp], args: _ *)
  }
}