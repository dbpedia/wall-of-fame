package org.dbpedia.walloffame.validate

import better.files.File
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.RDFDataMgr
import org.dbpedia.walloffame.spring.model.WebId
import org.junit.jupiter.api.Test

class ValidateTest {

  @Test
  def setWebId() = {
    val str =
      """
        |@base <https://raw.githubusercontent.com/Eisenbahnplatte/eisenbahnplatte.github.io/master/webid.ttl> .
        |@prefix foaf: <http://xmlns.com/foaf/0.1/> .
        |@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
        |@prefix cert: <http://www.w3.org/ns/auth/cert#> .
        |@prefix rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        |
        |<> a foaf:PersonalProfileDocument ;
        |   foaf:maker <#this> ;
        |   foaf:primaryTopic <#this> .
        |
        |<#this> a foaf:Person ;
        |   foaf:name "Eisenbahnplatte";
        |   foaf:img <https://eisenbahnplatte.github.io/eisenbahnplatte.jpeg>;
        |   foaf:gender "male";
        |   foaf:geekcode "GMU GCS s: d? !a L++ PS+++ PE- G h";
        |   foaf:firstname "Fabian";
        |
        |cert:key [
        |      a cert:RSAPublicKey;
        |      rdfs:label "HP Elitebook";
        |      cert:modulus "C133F14349AC1035EC007228975FA276E52A7D4E2F227710D645C616E92666C861838AFF268491990F9C30F6999E2C62DF3379DA0FDCE300CF1BED6B37F25FF9ADD5BD242E346E1C25E33891A95BD9B998D177D389A163B150383FE6EE1D9F479B2F186EF0BB11B4E8AC87AEB2414BA653741E87E8E72A083D00C813B1242158FFC957089C97044241DBC9CAE553CEE5B869A3667596E4E6A34998CEE9A588617B54432010CCDCF5EC7C4140B6AA3422AB089E5676847F727DA8762D1BA35FA4F0593AF91BFFA5AA4B433C07F1982CA22F1BEB1B538C8890632608C04E4A4E9129C1AA4575BAAE9014E30C0D7A5F96D98BCB4C5D0C794A8B5A2A7D823ECC5411"^^xsd:hexBinary;
        |      cert:exponent "65537"^^xsd:nonNegativeInteger
        |     ] .
        |""".stripMargin

    import java.io.PrintWriter
    val fileToValidate = File("./tmp/webIdToValidate.ttl")
    new PrintWriter(fileToValidate.toJava) {
      write(str)
      close
    }

    val model = RDFDataMgr.loadModel(fileToValidate.pathAsString)

    val webid = new WebId(model)
  }
}
