package org.dbpedia.walloffame.convert

import java.io.FileWriter
import java.nio.file.{Files, Paths}
import better.files.File
import better.files.File.OpenOptions
import org.apache.jena.rdf.model.Model
import org.dbpedia.walloffame.uniform.QueryHandler
import org.dbpedia.walloffame.uniform.queries.{SelectOptionalQueries, SelectQueries}

import scala.collection.mutable.ListBuffer

object ModelToJSONConverter {


  def createJSONFile(models: Seq[(String, Model)], outFile: File): File = {
    val json = toJSON(models)

    val bw = outFile.newBufferedWriter
    bw.write(json)
    bw.close()

    outFile
  }

  def toJSON(models: Seq[(String, Model)]): String = {
    val items = new ListBuffer[ListBuffer[String]]

    models.foreach(model => {
      val results = QueryHandler.executeQuery(SelectQueries.getQueryWebIdData(), model._2)

      results.foreach(result => {
        val item = new ListBuffer[String]

        def addToListBuffer(varName: String, entry: String) {
          item +=
            s"""
               |"$varName": "$entry"
               |""".stripMargin
        }


        addToListBuffer("account", model._1)
        addToListBuffer("webid", result.getResource("?webid").toString)
        addToListBuffer("maker", result.getResource("?maker").toString)
        addToListBuffer("name", result.getLiteral("?name").getLexicalForm)
        addToListBuffer("keyname", result.getLiteral("?keyname").getLexicalForm)
        addToListBuffer("keyvalue", result.getLiteral("?keyvalue").getLexicalForm)


        var optional = QueryHandler.executeQuery(SelectOptionalQueries.queryGeekCode(), model._2)
        if (optional.nonEmpty) addToListBuffer("geekcode", optional.head.getLiteral("?geekcode").getLexicalForm)

        optional = QueryHandler.executeQuery(SelectOptionalQueries.queryImg(), model._2)
        if (optional.nonEmpty) {
          try {
            addToListBuffer("img", optional.head.getResource("?img").toString)
          } catch {
            case classCastException: ClassCastException => addToListBuffer("img", optional.head.getLiteral("?img").getLexicalForm)
          }
        }

        optional = QueryHandler.executeQuery(SelectOptionalQueries.queryGender(), model._2)
        if (optional.nonEmpty) addToListBuffer("gender", optional.head.getLiteral("?gender").getLexicalForm)

        items += item
      })
    })


    var rawJSON =
      s"""
         |{
         |    "types": {
         |        "WebId": {
         |            "pluralLabel": "WebIds"
         |        }
         |    },
         |    "properties": {
         |        "webid": {
         |            "valueType": "url"
         |        },
         |        "type": {
         |            "valueType": "url"
         |        },
         |        "maker": {
         |            "valueType": "url"
         |        },
         |        "primaryTopic": {
         |            "valueType": "url"
         |        },
         |        "img": {
         |            "valueType": "url"
         |        }
         |    },
         |    "items": [
      """.stripMargin

    items.foreach(item => rawJSON = rawJSON.concat(s"{ ${item.toList.mkString(",")} },"))

    rawJSON = rawJSON.dropRight(1).concat("]}")

    import spray.json._
    rawJSON.parseJson.prettyPrint
  }
}
