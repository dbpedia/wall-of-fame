package org.dbpedia.walloffame

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication
class Application extends SpringBootServletInitializer{
  @Override
  protected override def configure(application:SpringApplicationBuilder):SpringApplicationBuilder ={
    application.sources(Application.getClass)
  }
}


object Application {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[Application], args: _ *)
  }
}