package org.dbpedia.walloffame

import better.files.File
import org.apache.commons.compress.compressors.{CompressorOutputStream, CompressorStreamFactory}
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.jena.shared.PrefixMapping
import org.dbpedia.walloffame.convert.ModelToJSONConverter
import org.dbpedia.walloffame.crawling.WebIdFetcher
import org.dbpedia.walloffame.uniform.WebIdUniformer
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

import java.io.{BufferedOutputStream, ByteArrayOutputStream, FileOutputStream}

@Component
class InitRunnerDatabus extends CommandLineRunner {

  @Autowired
  private var config: Config = _

  override def run(args: String*): Unit = {

    //    println(config.databus.file)
    //    val targetDir = File(config.databus.file)
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
    val file = targetDir / "webids.ttl"
    val out = new FileOutputStream(file.toJava)

    val gzippedOut: CompressorOutputStream = new CompressorStreamFactory()
      .createCompressorOutputStream(CompressorStreamFactory.GZIP, out);

    RDFDataMgr.write(gzippedOut, aggregatedModel, Lang.TTL)
  }
}

