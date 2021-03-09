package org.dbpedia.walloffame.tools

import org.apache.jena.rdf.model.{ModelFactory, ResourceFactory, Statement}
import org.apache.jena.riot.{Lang, RDFDataMgr}

import java.io.FileOutputStream

object JsonLDLogger {

//  val logFile = "./tmp/errors.jsonld"
  val model = ModelFactory.createDefaultModel()

  def resetModel()={
    model.removeAll()
  }

  def writeOut(logFile:String):Unit ={

//    val stmts = model.listStatements()
//    while (stmts.hasNext) println(stmts.nextStatement())

    val fos = new FileOutputStream(logFile)
    RDFDataMgr.write(fos, model, Lang.JSONLD)
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

  def addException(webid:String, exc:Exception)={
    this.add(webid, "https://example.org/hasException", exc.toString)
  }


  //  def append(string: String)={
  //
  //    val fw = new FileWriter(logFile, true)
  //    try {
  //      fw.write(s"$string\n")
  //    }
  //    finally fw.close()
  //  }
  //
  //  def deleteLogFile()={
  //    File(logFile).delete(true)
  //  }


  //  def logAccountException(acc:String, exc:Exception): Unit ={
  //    JsonLDLogger.append(s"$acc : ${exc.toString} occured while Download Process\n")
  //  }
}
