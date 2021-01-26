package org.dbpedia.walloffame.uniform.queries

object SelectOptionalQueries {

  def getOptionalValues():String = {
    s"""
       |PREFIX foaf:<http://xmlns.com/foaf/0.1/>
       |
       |SELECT ?webid ?maker ?name ?img ?gender ?geekcode ?firstname {
       |  ?webid a foaf:PersonalProfileDocument ;
       |        foaf:maker ?maker .
       |  ?maker foaf:name ?name ;
       |         foaf:img ?img ;
       |         foaf:gender ?gender ;
       |         foaf:geekcode ?geekcode ;
       |         foaf:firstname ?firstname .
       |}
    """.stripMargin
  }

  def queryName():String={
    s"""
       |PREFIX foaf:<http://xmlns.com/foaf/0.1/>
       |
       |SELECT ?name {
       |  ?maker foaf:name ?name .
       |}
  """.stripMargin
  }


  def queryGeekCode():String={
    s"""
       |PREFIX foaf:<http://xmlns.com/foaf/0.1/>
       |
       |SELECT ?geekcode {
       |  ?maker foaf:geekcode ?geekcode .
       |}
  """.stripMargin
  }
  def queryImg():String={
    s"""
       |PREFIX foaf:<http://xmlns.com/foaf/0.1/>
       |
       |SELECT ?img {
       |  ?maker foaf:img ?img .
       |}
  """.stripMargin
  }
  def queryGender():String={
    s"""
       |PREFIX foaf:<http://xmlns.com/foaf/0.1/>
       |
       |SELECT ?gender {
       |  ?maker foaf:gender ?gender .
       |}
  """.stripMargin

  }
  def queryFirstName():String={
    s"""
       |PREFIX foaf:<http://xmlns.com/foaf/0.1/>
       |
       |SELECT ?firstname {
       |  ?maker foaf:firstname ?firstname .
       |}
  """.stripMargin
  }
}
