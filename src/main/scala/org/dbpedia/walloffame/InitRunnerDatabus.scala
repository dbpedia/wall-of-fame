package org.dbpedia.walloffame

import better.files.File
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream

@Component
class InitRunnerDatabus extends CommandLineRunner {

  @Autowired
  private var config: Config = _

  override def run(args: String*): Unit = {

    if (args.isEmpty) {
      println("Please set the target directory, where the WebId files need to be written to.")
      return 1
    }

    val targetDir = File(args.head)
    targetDir.createDirectoryIfNotExists()
    getWebIdsFromWallOfFame(targetDir)
  }

  def getWebIdsFromWallOfFame(targetDir: File) = {

    val aggregatedModel = ModelFactory.createDefaultModel()
    val webIdModels = VirtuosoHandler.getAllWebIds(config.virtuoso)

    webIdModels.foreach(tuple => {
      aggregatedModel.add(tuple._2)
    })

    //fix prefixes
    val prefixes = Map(
      "foaf" -> "http://xmlns.com/foaf/0.1/",
      "cert" -> "http://www.w3.org/ns/auth/cert#",
      "dbo" -> "http://dbpedia.org/ontology/",
      "rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      "xsd" -> "http://www.w3.org/2001/XMLSchema#"
    )

    aggregatedModel.clearNsPrefixMap()

    import collection.JavaConversions._
    aggregatedModel.setNsPrefixes(prefixes)

    //write file
    val file = targetDir / "webids.ttl.gz"
    val fos  = new FileOutputStream(file.toJava)
    val gzos = new GZIPOutputStream( fos )

    RDFDataMgr.write(gzos, aggregatedModel, Lang.TTL)

    gzos.close()
    fos.close()
  }
}
