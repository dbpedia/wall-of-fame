package org.dbpedia.walloffame

import org.dbpedia.walloffame.spring.controller
import org.dbpedia.walloffame.spring.controller.{ValidationController, WoFController}
import org.springframework.boot.{SpringApplication, WebApplicationType}
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.{ComponentScan, Configuration, FilterType}
import org.springframework.core.`type`.filter.RegexPatternTypeFilter

import java.util.regex.Pattern

@Configuration
@EnableAutoConfiguration
@ComponentScan(
  basePackages = Array("org.dbpedia.walloffame"),
  useDefaultFilters = false,
  includeFilters = Array(new ComponentScan.Filter(`type` = FilterType.REGEX, pattern = Array("org.dbpedia.walloffame.Config", "org.dbpedia.walloffame.InitRunnerDatabus")))
 )
class DatabusApplication {
}


object DatabusApplication {
  def main(args: Array[String]): Unit = {
    val app = new SpringApplicationBuilder(classOf[DatabusApplication])
    app.web(WebApplicationType.NONE)
    app.run(args: _*)
    //    SpringApplication.exit(app)
  }
}
