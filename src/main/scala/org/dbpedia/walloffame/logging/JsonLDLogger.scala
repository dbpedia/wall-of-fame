package org.dbpedia.walloffame.logging

import org.apache.jena.rdf.model.{ModelFactory, ResourceFactory, Statement}
import org.apache.jena.riot.{Lang, RDFDataMgr}

import java.io.{FileOutputStream, FileWriter}

object JsonLDLogger {

  val logFile = "./tmp/errors.jsonld"
  val model = ModelFactory.createDefaultModel()

  def append(string: String)={

    val fw = new FileWriter(logFile, true)
    try {
      fw.write(s"$string\n")
    }
    finally fw.close()
  }

  def logAccountException(acc:String, exc:Exception): Unit ={
    JsonLDLogger.append(s"$acc : ${exc.toString} occured while Download Process\n")
  }

  def writeOut():Unit ={
    val fos = new FileOutputStream(logFile)
    RDFDataMgr.write(fos, model, Lang.RDFJSON)
  }

  def add(stmt: Statement):Unit ={
    model.add(stmt)
  }

  def add(sub:String, pre:String, obj:String):Unit ={
    model.add(
        ResourceFactory.createResource(sub),
        ResourceFactory.createProperty(pre),
        ResourceFactory.createStringLiteral(obj)
    )
  }
}
