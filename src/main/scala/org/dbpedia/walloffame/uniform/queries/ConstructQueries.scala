package org.dbpedia.walloffame.uniform.queries

import org.apache.jena.rdf.model.Resource

object ConstructQueries {

  def constructWebId(): String = {
    s"""
       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
       |PREFIX cert: <http://www.w3.org/ns/auth/cert#>
       |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
       |
       |CONSTRUCT {
       |  ?webid a foaf:PersonalProfileDocument .
       |  ?webid foaf:maker ?maker .
       |  ?webid foaf:primaryTopic ?primaryTopic .
       |  ?maker a foaf:Person .
       |  ?maker foaf:name ?makerName .
       |  ?maker cert:key ?key .
       |  ?key a cert:RSAPublicKey .
       |  ?key rdf:label ?label .
       |  ?key cert:modulus ?modulus.
       |  ?key cert:exponent ?exponent .
       |  }
       |WHERE {
       |  ?webid a foaf:PersonalProfileDocument .
       |  ?webid foaf:maker ?maker .
       |  ?webid foaf:primaryTopic ?primaryTopic .
       |  ?maker a foaf:Person .
       |  ?maker foaf:name ?makerName .
       |  ?maker cert:key ?key .
       |  ?key a cert:RSAPublicKey .
       |  ?key rdf:label ?label .
       |  ?key cert:modulus ?modulus.
       |  ?key cert:exponent ?exponent .
       |  }
       |""".stripMargin

  }



  def constructWebIdURL(): String = {
    """
      |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
      |
      |CONSTRUCT   { ?webid a foaf:PersonalProfileDocument }
      |WHERE       { ?webid a foaf:PersonalProfileDocument }
      |""".stripMargin
  }

  def constructMakerURL(webId: Resource): String = {
    s"""
       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
       |
       |CONSTRUCT   { <${webId.toString}> foaf:maker ?maker }
       |WHERE       { <${webId.toString}> foaf:maker ?maker }
       |""".stripMargin
  }

  def constructMakerName(makerURL: Resource): String = {
    s"""
       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
       |
       |CONSTRUCT   { <${makerURL.toString}> foaf:name ?makerName }
       |WHERE       { <${makerURL.toString}> foaf:name ?makerName }
       |""".stripMargin
  }

  def constructCertValue(makerURL: Resource): String = {
    s"""
       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
       |
       |CONSTRUCT   { <${makerURL.toString}> <http://www.w3.org/ns/auth/cert#modulus> ?keyvalue }
       |WHERE       {
       |  <${makerURL.toString}> <http://www.w3.org/ns/auth/cert#key> ?key .
       |  ?key <http://www.w3.org/ns/auth/cert#modulus> ?keyvalue .
       |}
       |""".stripMargin
  }


}
