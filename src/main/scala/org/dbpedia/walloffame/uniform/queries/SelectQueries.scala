package org.dbpedia.walloffame.uniform.queries

import org.apache.jena.rdf.model.Resource

object SelectQueries {

  def getWebIdURL() = {
    """
      |SELECT ?webid {
      |   ?webid a <http://xmlns.com/foaf/0.1/PersonalProfileDocument> .
      |}
      |""".stripMargin
  }

  def getMakerURL(webId: Resource): String = {
    s"""
       |SELECT ?maker {
       |  <${webId.toString}> <http://xmlns.com/foaf/0.1/maker> ?maker .
       |}
       |""".stripMargin
  }

  def getMakerName(makerURL: Resource): String = {
    s"""
       |SELECT ?name {
       |  <$makerURL> <http://xmlns.com/foaf/0.1/name> ?name ;
       |}
       |""".stripMargin
  }


  def getQueryWebIdData(): String = {
    s"""
       |SELECT ?webid ?maker ?name ?keyname ?keyvalue {
       |  ?webid a <http://xmlns.com/foaf/0.1/PersonalProfileDocument> ;
       |        <http://xmlns.com/foaf/0.1/maker> ?maker .
       |  ?maker <http://xmlns.com/foaf/0.1/name> ?name ;
       |         <http://www.w3.org/ns/auth/cert#key> ?key .
       |  ?key <http://www.w3.org/1999/02/22-rdf-syntax-ns#label> ?keyname ;
       |       <http://www.w3.org/ns/auth/cert#modulus> ?keyvalue .
       |}
    """.stripMargin

  }

  val resultSeverity =
    """
      |PREFIX sh: <http://www.w3.org/ns/shacl#>
      |
      |SELECT ?severity ?focusNode ?property ?message
      |WHERE {
      |  ?report  a  sh:ValidationReport ;
      |           sh:result ?result .
      |  ?result  sh:resultSeverity ?severity ;
      |           sh:focusNode ?focusNode ;
      |           sh:resultPath ?property ;
      |           sh:resultMessage ?message .
      |}
      |""".stripMargin


  def getDatabusUserData(webid:String):String = {
    s"""
      |PREFIX dcat: <http://www.w3.org/ns/dcat#>
      |PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>
      |PREFIX dct: <http://purl.org/dc/terms/>
      |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
      |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      |PREFIX databus: <https://databus.dbpedia.org/>
      |
      |SELECT DISTINCT ?numUploads ?uploadSize WHERE {
      |  OPTIONAL {
      |    SELECT (count(?version) as ?numUploads) WHERE {
      |      ?dataset dataid:maintainer|dct:publisher <$webid>.
      |      ?dataset dataid:version ?version .
      |    }
      |  }
      |  OPTIONAL {
      |    SELECT (sum(?size) as ?uploadSize) WHERE {
      |      ?dataset dataid:maintainer|dct:publisher <$webid> .
      |      ?dataset dcat:distribution ?distribution .
      |      ?distribution dcat:byteSize ?size .
      |    }
      |  }
      |}
      |""".stripMargin
  }

}
