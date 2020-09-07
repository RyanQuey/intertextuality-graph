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

import com.ryanquey.datautils.helpers.StringHelpers.snakeToCamel
import com.ryanquey.datautils.models.{Model, Record}


class TheographicDataFile (tablename : String, filename : String) {
  val dataFilePath : String = s"${sys.env("INTERTEXTUALITY_GRAPH_RAW_DATA_DIR")}/theographic-airtable-csvs"
  val fullPath : Path = Paths.get(dataFilePath, filename)
  var headers : Array[String] = _;
  val mirror = ru.runtimeMirror(getClass.getClassLoader)


  def parseFile (table : String) = {
    val bufferedSource = Source.fromFile(dataFilePath)
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
      def getV(name: String): Any = ref.getClass.getMethods.find(_.getName == name).get.invoke(ref)
      def setV(name: String, value: Any): Unit = ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.asInstanceOf[AnyRef])
    }

	  val csvDataFile : File = new File(fullPath.toString);
	  val csvDataReader : Reader = new FileReader(csvDataFile);
    // cannot just split by comma, since many fields have multiples (so commas separate) or commas inside fields
		val parser : CSVParser = CSVParser.parse(csvDataReader, CSVFormat.RFC4180);

	 	for (csvRecord : CSVRecord <- parser.asScala) {
	 	  // iterate over our mapping to get what fields we want into the db columns that they map to

      // first, instantiate a record of our model
      // Should work...right? "If you define a reference variable whose type is an interface, any object you assign to it must be an instance of a class that implements the interface." (https://docs.oracle.com/javase/tutorial/java/IandI/interfaceAsType.html)
      val record : Model = table match {
        case "books" => new Book().asInstanceOf[Model]
        case "chapters" => new Chapter().asInstanceOf[Model]
        case "verses" => new Verse().asInstanceOf[Model]
      }

      for ((csvCol : String, dbCol : String) <- fieldsMapping) {  
        // use reflection to dynamically set field
        // https://stackoverflow.com/a/1589919/6952495
        val value = csvRecord.getV(csvCol)
        record.setV(snakeToCamel(dbCol), value)
      }

      record.persist();
    }

  }


}
