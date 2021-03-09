package org.dbpedia.walloffame.sparql.queries

object SelectQueries {

  //  QUERIES FOR UNIFORMED WEBIDS
  def getWebIdData(): String =
    s"""
       |PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
       |PREFIX dbo:   <http://dbpedia.org/ontology/>
       |PREFIX cert:  <http://www.w3.org/ns/auth/cert#>
       |PREFIX rdfs:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
       |
       |SELECT ?webid ?maker ?name ?img ?geekcode ?account ?numVersions ?numArtifacts ?uploadSize
       |WHERE {
       |  ?webid a foaf:PersonalProfileDocument ;
       |      foaf:maker ?maker .
       |  ?maker a dbo:DBpedian, foaf:Person ;
       |      foaf:name ?name ;
       |
       |  OPTIONAL {
       |    SELECT * WHERE {
       |      ?maker foaf:img ?img .
       |    }
       |  }
       |  OPTIONAL {
       |    SELECT * WHERE {
       |      ?maker foaf:geekcode ?geekcode .
       |    }
       |  }
       |
       |  OPTIONAL {
       |    SELECT * WHERE {
       |      ?maker foaf:account ?account .
       |      ?account dbo:numVersions ?numVersions ;
       |        dbo:numArtifacts ?numArtifacts ;
       |        dbo:uploadSize ?uploadSize .
       |    }
       |  }
       |}
    """.stripMargin

  def getMakerURL(): String =
    s"""
       |PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
       |
       |SELECT ?maker {
       |  ?webid a foaf:PersonalProfileDocument .
       |  ?webid foaf:maker ?maker .
       |}
       |""".stripMargin

  def getOptionals(): String =
    s"""
       |PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
       |
       |SELECT * WHERE {
       |?webid a foaf:PersonalProfileDocument ;
       |    foaf:maker ?maker .
       |  OPTIONAL {
       |    SELECT * WHERE {
       |      ?maker foaf:img ?img .
       |    }
       |  }
       |  OPTIONAL {
       |    SELECT * WHERE {
       |      ?maker foaf:geekcode ?geekcode .
       |    }
       |  }
       |}
       |""".stripMargin
  //  QUERIES FOR UNIFORMED WEBIDS


  // QUERIES FOR SHACL RESULT
  def resultSeverity():String=
    """
      |PREFIX sh: <http://www.w3.org/ns/shacl#>
      |
      |SELECT ?severity ?focusNode ?property ?message
      |WHERE {
      |  ?report a sh:ValidationReport ;
      |     sh:result ?result .
      |  ?result sh:resultSeverity ?severity ;
      |     sh:focusNode ?focusNode ;
      |     sh:resultPath ?property ;
      |     sh:resultMessage ?message .
      |}
      |""".stripMargin
  // QUERIES FOR SHACL RESULT


  // QUERIES FOR DBPEDIA AND DBPEDIA DATABUS
  def checkForRelatedDatabusAccount(webid:String):String=
    s"""
       |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
       |
       |SELECT * {
       |  <$webid> foaf:account ?acc .
       |}
       |""".stripMargin

  def getDatabusUserData(webid:String):String =
    s"""
      |PREFIX dcat: <http://www.w3.org/ns/dcat#>
      |PREFIX dataid: <http://dataid.dbpedia.org/ns/core#>
      |PREFIX dct: <http://purl.org/dc/terms/>
      |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
      |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      |PREFIX databus: <https://databus.dbpedia.org/>
      |
      |SELECT DISTINCT ?numVersions ?numArtifacts ?uploadSize WHERE {
      |  OPTIONAL {
      |    SELECT (count(?version) as ?numVersions) WHERE {
      |      ?dataset dataid:maintainer|dct:publisher <$webid>.
      |      ?dataset dataid:version ?version .
      |    }
      |  }
      |  OPTIONAL {
      |    SELECT (count(distinct ?artifact) as ?numArtifacts) WHERE {
      |      ?dataset dataid:maintainer|dct:publisher <$webid>.
      |      ?dataset dataid:artifact ?artifact .
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
  // QUERIES FOR DBPEDIA AND DBPEDIA DATABUS


  // QUERIES FOR WEBID
  def getTypeOf(url:String)={
    s"""
       |PREFIX foaf:  <http://xmlns.com/foaf/0.1/>
       |
       |SELECT ?type {
       |  <$url> a ?type .
       |}
       |""".stripMargin

  }
}
