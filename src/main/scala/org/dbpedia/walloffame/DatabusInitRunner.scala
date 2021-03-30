package org.dbpedia.walloffame

import better.files.File
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.apache.juli.logging.{Log, LogFactory}
import org.dbpedia.walloffame.virtuoso.VirtuosoHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

import java.io.FileOutputStream
import java.nio.file.NoSuchFileException
import java.util.zip.GZIPOutputStream

@Component
class DatabusInitRunner extends CommandLineRunner {

  @Autowired
  private var config: Config = _

  val logger: Log = LogFactory.getLog(getClass)

  override def run(args: String*): Unit = {

    if (args.isEmpty) {
      println("Please set the target directory, where the WebId files need to be written to.")
      return
    }

    val targetDir = File(args.head)
    targetDir.createDirectoryIfNotExists()
    writeOutWebIdsFromWoF(targetDir)
  }

  def writeOutWebIdsFromWoF(targetDir: File):Unit = {

    val aggregatedModel = ModelFactory.createDefaultModel()
    val vos = new VirtuosoHandler(config.virtuoso)
    val webIdModels = vos.getAllWebIdGraphs()

    webIdModels.foreach(graph => aggregatedModel.add(ModelFactory.createModelForGraph(graph)))

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

    //write webids file
    val file = targetDir / targetDir.parent.name.concat("_webids.ttl.gz")
    val fos  = new FileOutputStream(file.toJava)
    val gzos = new GZIPOutputStream( fos )
    RDFDataMgr.write(gzos, aggregatedModel, Lang.TTL)
    gzos.close()
    fos.close()

    //write log file
    val logFile = targetDir / targetDir.parent.name.concat("_errorLog.jsonld")
    try{
      File(config.log.file).copyTo(logFile, overwrite = true)
    } catch {
      case noSuchFileException: NoSuchFileException => logger.error(noSuchFileException)
    }
  }
}
