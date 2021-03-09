package org.dbpedia.walloffame.sparql.queries

object ConstructQueries {

  def constructWebIdWithOptionals():String={
    """
      |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
      |PREFIX cert: <http://www.w3.org/ns/auth/cert#>
      |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      |PREFIX dbo: <http://dbpedia.org/ontology/>
      |
      |CONSTRUCT {
      |  ?webid a foaf:PersonalProfileDocument .
      |  ?webid foaf:maker ?maker .
      |  ?webid foaf:primaryTopic ?primaryTopic .
      |  ?maker a foaf:Person, dbo:DBpedian.
      |  ?maker foaf:name ?makerName .
      |  ?maker foaf:geekcode ?geekcode .
      |  ?maker foaf:img ?img .
      |  ?maker cert:key ?key .
      |  ?key a cert:RSAPublicKey .
      |  ?key rdf:label ?label .
      |  ?key cert:modulus ?modulus.
      |  ?key cert:exponent ?exponent .
      |}
      |WHERE {
      |  ?webid a foaf:PersonalProfileDocument .
      |  ?webid foaf:maker ?maker .
      |  ?webid foaf:primaryTopic ?primaryTopic .
      |  ?maker a foaf:Person, dbo:DBpedian .
      |  ?maker foaf:name ?makerName .
      |  ?maker cert:key ?key .
      |  ?key a cert:RSAPublicKey .
      |  ?key rdf:label ?label .
      |  ?key cert:modulus ?modulus.
      |  ?key cert:exponent ?exponent .
      |  OPTIONAL {
      |     ?maker foaf:geekcode ?geekcode .
      |  }
      |  OPTIONAL {
      |     ?maker foaf:img ?img .
      |  }
      |}
      |""".stripMargin
  }

//  def constructWebId(): String = {
//    s"""
//       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
//       |PREFIX cert: <http://www.w3.org/ns/auth/cert#>
//       |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
//       |PREFIX dbo: <http://dbpedia.org/ontology/>
//       |
//       |CONSTRUCT {
//       |  ?webid a foaf:PersonalProfileDocument .
//       |  ?webid foaf:maker ?maker .
//       |  ?webid foaf:primaryTopic ?primaryTopic .
//       |  ?maker a foaf:Person .
//       |  ?maker foaf:name ?makerName .
//       |  ?maker cert:key ?key .
//       |  ?key a cert:RSAPublicKey .
//       |  ?key rdf:label ?label .
//       |  ?key cert:modulus ?modulus.
//       |  ?key cert:exponent ?exponent .
//       |  }
//       |WHERE {
//       |  ?webid a foaf:PersonalProfileDocument .
//       |  ?webid foaf:maker ?maker .
//       |  ?webid foaf:primaryTopic ?primaryTopic .
//       |  ?maker a foaf:Person, dbo:DBpedian .
//       |  ?maker foaf:name ?makerName .
//       |  ?maker cert:key ?key .
//       |  ?key a cert:RSAPublicKey .
//       |  ?key rdf:label ?label .
//       |  ?key cert:modulus ?modulus.
//       |  ?key cert:exponent ?exponent .
//       |  }
//       |""".stripMargin
//
//  }




//  def constructWebIdURL(): String = {
//    """
//      |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
//      |
//      |CONSTRUCT   { ?webid a foaf:PersonalProfileDocument }
//      |WHERE       { ?webid a foaf:PersonalProfileDocument }
//      |""".stripMargin
//  }
//
//  def constructMakerURL(webId: Resource): String = {
//    s"""
//       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
//       |
//       |CONSTRUCT   { <${webId.toString}> foaf:maker ?maker }
//       |WHERE       { <${webId.toString}> foaf:maker ?maker }
//       |""".stripMargin
//  }
//
//  def constructMakerName(makerURL: Resource): String = {
//    s"""
//       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
//       |
//       |CONSTRUCT   { <${makerURL.toString}> foaf:name ?makerName }
//       |WHERE       { <${makerURL.toString}> foaf:name ?makerName }
//       |""".stripMargin
//  }
//
//  def constructCertValue(makerURL: Resource): String = {
//    s"""
//       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
//       |
//       |CONSTRUCT   { <${makerURL.toString}> <http://www.w3.org/ns/auth/cert#modulus> ?keyvalue }
//       |WHERE       {
//       |  <${makerURL.toString}> <http://www.w3.org/ns/auth/cert#key> ?key .
//       |  ?key <http://www.w3.org/ns/auth/cert#modulus> ?keyvalue .
//       |}
//       |""".stripMargin
//  }
//
//  def constructGeekCode():String={
//    s"""
//       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
//       |
//       |CONSTRUCT {
//       |  ?maker foaf:geekcode ?geekcode .
//       |  }
//       |WHERE {
//       |  ?maker foaf:geekcode ?geekcode .
//       |  }
//       |""".stripMargin
//  }
//
//  def constructImg():String={
//    s"""
//       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
//       |PREFIX dbo: <http://dbpedia.org/ontology/>
//       |
//       |CONSTRUCT {
//       |  ?maker foaf:img ?img .
//       |  }
//       |WHERE {
//       |  ?maker a foaf:Person, dbo:DBpedian .
//       |  ?maker foaf:img ?img .
//       |  }
//       |""".stripMargin
//  }

}
