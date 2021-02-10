package org.dbpedia.walloffame.logging

import java.io.FileWriter

object HtmlLogger {

  val logFile = "log.html"

  def append(string: String)={

    val fw = new FileWriter("./tmp/errors.log", true)
    try {
      fw.write(s"$string\n")
    }
    finally fw.close()
  }

  def logAccountException(acc:String, exc:String): Unit ={
    HtmlLogger.append(s"$acc : $exc occured while Download Process")
  }
}
