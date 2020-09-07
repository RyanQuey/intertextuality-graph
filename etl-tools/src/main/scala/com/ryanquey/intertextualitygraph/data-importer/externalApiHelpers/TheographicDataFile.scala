package com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers
import java.nio.file.{Path, Paths, Files}
import java.io.{File, Reader, FileReader}
import scalaj.http._
import scala.io.Source
import java.nio.charset.StandardCharsets
import org.apache.commons.csv.{CSVParser, CSVRecord, CSVFormat}
import com.ryanquey.intertextualitygraph.dataimporter.constants.TheographicConstants._
import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
// needed so I can call .asScala
import scala.collection.JavaConverters._


// for dynamic class instantiation
import scala.reflect.runtime.{universe => ru}

import com.ryanquey.datautils.helpers.StringHelpers.{snakeToCamel, snakeToUpperCamel}
import com.ryanquey.datautils.models.{Model, Record}


class TheographicDataFile (table : String, filename : String) {
  val dataDirPath : String = s"${sys.env("INTERTEXTUALITY_GRAPH_RAW_DATA_DIR")}/theographic-airtable-csvs"
  val fullPath : Path = Paths.get(dataDirPath, filename)
  var headers : Array[String] = _;


  def parseFile() = {
    println(s"parsing $filename")
    val bufferedSource = Source.fromFile(fullPath.toString)
    val fieldsMapping : Map[String, _] = table match {
      case "books" => booksFieldsMapping : Map[String, _]
      case "chapters" => chaptersFieldsMapping : Map[String, _]
      case "verses" => versesFieldsMapping : Map[String, _]
    }

    // do some reflection to get ready to instantiate our model for this table
    // following scala docs
    // https://stackoverflow.com/a/1589919/6952495
    // create dynamic getters and setters on...everything (?)
    implicit def reflector(ref: AnyRef) = new {
      def getV(name: String): Any = ref.getClass.getMethods.find(_.getName == s"get${snakeToUpperCamel(name)}").get.invoke(ref)
      def setV(name: String, value: Any): Unit = ref.getClass.getMethods.find(_.getName == s"set${snakeToUpperCamel(name)}").get.invoke(ref, value.asInstanceOf[AnyRef])
    }

	  val csvDataFile : File = new File(fullPath.toString);
	  val csvDataReader : Reader = new FileReader(csvDataFile);
    // cannot just split by comma, since many fields have multiples (so commas separate) or commas inside fields
		val csvRecords = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csvDataReader);

	 	for (csvRecord : CSVRecord <- csvRecords.asScala) {
	 	  // iterate over our mapping to get what fields we want into the db columns that they map to

      // first, instantiate a record of our model
      // need to typecast (https://alvinalexander.com/scala/how-to-cast-objects-class-instance-in-scala-asinstanceof/) since these models implement Model interface
      val record : Model = table match {
        case "books" => new Book().asInstanceOf[Model]
        case "chapters" => new Chapter().asInstanceOf[Model]
        case "verses" => new Verse().asInstanceOf[Model]
      }

      println(s"now should have a blank record: $record")
      for ((csvCol : String, dbCol : String) <- fieldsMapping) {  
        // use reflection to dynamically set field
        // https://stackoverflow.com/a/1589919/6952495
        println(s"from csv col $csvCol to db col $dbCol")
        val value = csvRecord.get(csvCol)

        println(s"value to set: $value")
        println(s"setting to: ${snakeToCamel(dbCol)}")
        println(record.getClass.getMethods)
        record.setV(snakeToCamel(dbCol), value)
      }

      record.persist();
    }

  }


}
