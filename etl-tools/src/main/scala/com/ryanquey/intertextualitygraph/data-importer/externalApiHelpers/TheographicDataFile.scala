package com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers
import java.nio.file.{Path, Paths, Files}
import scalaj.http._
import scala.io.Source
import java.nio.charset.StandardCharsets
import org.apache.commons.csv.{CSVParser, CSVRecord, CSVFormat}
import com.ryanquey.intertextualitygraph.dataimporter.constants.TheographicConstants._
import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
// for dynamic class instantiation
import scala.reflect.runtime.{universe => ru}

import com.ryanquey.datautils.helpers.StringHelpers._


class TheographicDataFile (tablename : String, filename : String) {
  val dataFilePath : String = s"${sys.env("INTERTEXTUALITY_GRAPH_RAW_DATA_DIR")}/theographic-airtable-csvs"
  val fullPath : Path = Paths.get(dataFilePath, filename)
  var headers : Array[String] = _;
  val mirror = ru.runtimeMirror(getClass.getClassLoader)

  // https://stackoverflow.com/a/1589919/6952495
  implicit def reflector(ref: AnyRef) = new {
    def getV(name: String): Any = ref.getClass.getMethods.find(_.getName == name).get.invoke(ref)
    def setV(name: String, value: Any): Unit = ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.asInstanceOf[AnyRef])
  }

  def parseFile (table : String) = {
    val bufferedSource = Source.fromFile()
    val fieldsMapping = () => { 
      table match {
        case "books" => booksFieldsMapping
        case "chapters" => chaptersFieldsMapping
        case "verses" => versesFieldsMapping
      }
    }

    // do some reflection to get ready to instantiate our model for this table
    // following scala docs
    val model = table match {
      case "books" => Book
      case "chapters" => Chapter
      case "verses" => Verse 
    }
    val modelClass = ru.typeOf[model].typeSymbol.asClass
    val classMirror = mirror.reflectClass(modelClass)
    val classConstructor = ru.typeOf[model].decl(ru.termNames.CONSTRUCTOR).asMethod
    val constructorMirror = classMirror.reflectConstructor(classConstructor)

	  val csvData : File = new File(fullPath);
    // cannot just split by comma, since many fields have multiples (so commas separate) or commas inside fields
		val parser : CSVParser = CSVParser.parse(csvData, CSVFormat.RFC4180);

	 	for (csvRecord : CSVRecord <- parser) {
	 	  // iterate over our mapping to get what fields we want into the db columns that they map to
      val record = constructorMirror()
      for ((csvCol, dbCol) <- fieldsMapping) {  
        // use reflection to dynamically set field
        // https://stackoverflow.com/a/1589919/6952495
        val value = recordcsvRecord.get(csvCol)
        record.setV(snakeToCamel("dbCol"), value)
      }

      record.persist();
    }

  }


}
