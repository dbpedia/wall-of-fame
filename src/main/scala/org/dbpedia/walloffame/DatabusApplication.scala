package org.dbpedia.walloffame

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.{ComponentScan, Configuration, FilterType}

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
