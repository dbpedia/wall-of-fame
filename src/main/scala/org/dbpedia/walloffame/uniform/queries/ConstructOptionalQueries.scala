package org.dbpedia.walloffame.uniform.queries

object ConstructOptionalQueries {

  def constructGeekCode():String={
    s"""
       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
       |
       |CONSTRUCT {
       |  ?maker foaf:geekcode ?geekcode .
       |  }
       |WHERE {
       |  ?maker foaf:geekcode ?geekcode .
       |  }
       |""".stripMargin
  }

  def constructImg():String={
    s"""
       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
       |PREFIX dbo: <http://dbpedia.org/ontology/>
       |
       |CONSTRUCT {
       |  ?maker foaf:img ?img .
       |  }
       |WHERE {
       |  ?maker a foaf:Person, dbo:DBpedian .
       |  ?maker foaf:img ?img .
       |  }
       |""".stripMargin
  }



  //  def constructOptionals():String ={
//    s"""
//       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
//       |PREFIX cert: <http://www.w3.org/ns/auth/cert#>
//       |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
//       |
//       |CONSTRUCT {
//       |  ?maker foaf:name ?makerName .
//       |  ?maker foaf:img ?img .
//       |  ?maker foaf:gender ?gender .
//       |  ?maker foaf:geekcode ?geekcode .
//       |  ?maker foaf:firstname ?firstname .
//       |  }
//       |WHERE {
//       |  ?maker foaf:name ?makerName .
//       |  ?maker foaf:img ?img .
//       |  ?maker foaf:gender ?gender .
//       |  ?maker foaf:geekcode ?geekcode .
//       |  ?maker foaf:firstname ?firstname .
//       |  }
//       |""".stripMargin
//  }


//  def constructGender():String={
//    s"""
//       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
//       |
//       |CONSTRUCT {
//       |  ?maker foaf:gender ?gender .
//       |  }
//       |WHERE {
//       |  ?maker foaf:gender ?gender .
//       |  }
//       |""".stripMargin
//  }

//  def constructFirstName():String={
//    s"""
//       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
//       |
//       |CONSTRUCT {
//       |  ?maker foaf:firstname ?firstname .
//       |  }
//       |WHERE {
//       |  ?maker foaf:firstname ?firstname .
//       |  }
//       |""".stripMargin
//  }



//  def constructName():String={
//    s"""
//       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
//       |
//       |CONSTRUCT {
//       |  ?maker foaf:name ?name .
//       |  }
//       |WHERE {
//       |  ?maker foaf:name ?name .
//       |  }
//       |""".stripMargin
//  }
}
