package org.dbpedia.walloffame.uniformTest

import org.apache.commons.io.IOUtils
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.dbpedia.walloffame.uniform.WebIdUniformer
import org.junit.jupiter.api.Test

class UniformTests {


  @Test
  def shouldUniformCorrect:Unit ={

    val str2 =
      """
        |@base <https://raw.githubusercontent.com/Eisenbahnplatte/eisenbahnplatte.github.io/master/webid.ttl> .
        |@prefix foaf: <http://xmlns.com/foaf/0.1/> .
        |@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
        |@prefix cert: <http://www.w3.org/ns/auth/cert#> .
        |@prefix rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        |
        |<> a foaf:PersonalProfileDocument ;
        |    foaf:maker <#this> ;
        |    foaf:primaryTopic <#this> .
        |
        |<#this> a foaf:Person ;
        |     foaf:name "Eisenbahnplatte";
        |     foaf:img <https://eisenbahnplatte.github.io/eisenbahnplatte.jpeg>;
        |     foaf:gender "male";
        |     foaf:geekcode "GMU GCS s: d? !a L++ PS+++ PE- G h";
        |     foaf:firstname "Fabian";
        |
        |cert:key [
        |    a cert:RSAPublicKey;
        |    rdfs:label "HP Elitebook";
        |    cert:modulus "C133F14349AC1035EC007228975FA276E52A7D4E2F227710D645C616E92666C861838AFF268491990F9C30F6999E2C62DF3379DA0FDCE300CF1BED6B37F25FF9ADD5BD242E346E1C25E33891A95BD9B998D177D389A163B150383FE6EE1D9F479B2F186EF0BB11B4E8AC87AEB2414BA653741E87E8E72A083D00C813B1242158FFC957089C97044241DBC9CAE553CEE5B869A3667596E4E6A34998CEE9A588617B54432010CCDCF5EC7C4140B6AA3422AB089E5676847F727DA8762D1BA35FA4F0593AF91BFFA5AA4B433C07F1982CA22F1BEB1B538C8890632608C04E4A4E9129C1AA4575BAAE9014E30C0D7A5F96D98BCB4C5D0C794A8B5A2A7D823ECC5411"^^xsd:hexBinary;
        |    cert:exponent "65537"^^xsd:nonNegativeInteger
        |] .
        |""".stripMargin

    val str1 =
      """
        |@base <https://raw.githubusercontent.com/Eisenbahnplatte/eisenbahnplatte.github.io/master/webid.ttl> .
        |@prefix foaf: <http://xmlns.com/foaf/0.1/> .
        |@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
        |@prefix cert: <http://www.w3.org/ns/auth/cert#> .
        |@prefix rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        |
        |
        |<> a foaf:PersonalProfileDocument ;
        |   foaf:maker <#this> ;
        |   foaf:primaryTopic <#this> .
        |
        |
        |<#this> a foaf:Person ;
        |   foaf:name "Fabian";
        |   foaf:img <https://eisenbahnplatte.github.io/eisenbahnplatte.jpeg>;
        |   foaf:gender "male";
        |   foaf:firstName "also";
        |
        |cert:key [
        |      a cert:RSAPublicKey;
        |      rdfs:label "HP Elitebook";
        |      cert:modulus "C133F14349AC1035EC007228975FA276E52A7D4E2F227710D645C616E92666C861838AFF268491990F9C30F6999E2C62DF3379DA0FDCE300CF1BED6B37F25FF9ADD5BD242E346E1C25E33891A95BD9B998D177D389A163B150383FE6EE1D9F479B2F186EF0BB11B4E8AC87AEB2414BA653741E87E8E72A083D00C813B1242158FFC957089C97044241DBC9CAE553CEE5B869A3667596E4E6A34998CEE9A588617B54432010CCDCF5EC7C4140B6AA3422AB089E5676847F727DA8762D1BA35FA4F0593AF91BFFA5AA4B433C07F1982CA22F1BEB1B538C8890632608C04E4A4E9129C1AA4575BAAE9014E30C0D7A5F96D98BCB4C5D0C794A8B5A2A7D823ECC5411"^^xsd:hexBinary;
        |      cert:exponent "65537"^^xsd:nonNegativeInteger
        |     ] ;
        |
        |cert:key [
        |      a cert:RSAPublicKey;
        |      rdfs:label "Dell Laptop";
        |      cert:modulus "DC90A2B0BA8589751600D12EC14685A5593081BA586430917E9AC3A83BA4420F0D7586BF913F2D64DE4DCB214E25FBBA03AC54B29F7990B04690DAFB7E58324E9E41ABD3AF67E2015CB6FF1C702584EB0F720F226CCBC1764C2BE28D511C3EF90AA81F1D90AF665A7E68162EF808E041A0B0A238767AC08F9683764BA400761DDA0C1947982F2A1A0FFBD122387EECA527E8BB3A2E0400EDC57B071764FC1C4CBE7FD627C428F5DD9A15016A8CC7C2757D58B113EBC6641101C3AA67377C563C61FC4934ADE76071C8C7BE9B7A33F2AB98DAAA19BD1B37E97F997029BB9EC222EBF8BB40C294A1EF3A18C0C5D27514A0549817F3CAC0F294EBFD685D01B32AA1"^^xsd:hexBinary;
        |      cert:exponent "65537"^^xsd:nonNegativeInteger
        |     ] .
        |""".stripMargin

    val model: Model = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(str1, "UTF-8"), "", "TURTLE")

    val uniModel = WebIdUniformer.uniform(model)

    RDFDataMgr.write(System.out, uniModel, Lang.TTL)
  }

}
