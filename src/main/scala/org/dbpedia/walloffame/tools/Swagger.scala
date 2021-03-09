package org.dbpedia.walloffame.tools

import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.http.HttpMethod
import springfox.documentation.builders.{PathSelectors, RequestHandlerSelectors}
import springfox.documentation.service.{ApiInfo, Contact, Response, VendorExtension}
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
class Swagger {  def apiInfo: ApiInfo = {
  new ApiInfo(
    "DBpedia Wall of Fame",
    "Wall of Fame",
    "1.0",
    "#",
    new Contact("Fabian Goetz", "", "fabian.goetz (at) infai.org"),
    "License pending...",
    "#",
    new java.util.ArrayList[VendorExtension[_]]()
  )
}

@Bean
def api: Docket = {    new Docket(DocumentationType.SWAGGER_2).select()
  .apis(RequestHandlerSelectors.basePackage("org.dbpedia.walloffame"))
  .paths(PathSelectors.any())
  .build()
  .globalResponses(HttpMethod.POST, new java.util.ArrayList[Response]())
  .apiInfo(apiInfo)
}
}
