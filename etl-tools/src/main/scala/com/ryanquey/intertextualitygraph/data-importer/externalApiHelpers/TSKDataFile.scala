package com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers
import java.nio.file.{Path, Paths, Files}
import java.io.{File, Reader, FileReader, InputStreamReader, FileInputStream}
import org.apache.commons.io.input.BOMInputStream
import scalaj.http._
import scala.io.Source
import util.control.Breaks._
import scala.Array

import java.nio.charset.StandardCharsets
import org.apache.commons.csv.{CSVParser, CSVRecord, CSVFormat}
import com.ryanquey.intertextualitygraph.dataimporter.constants.TSKConstants._
import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers.Helpers._

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse

// needed so I can call .asScala
import scala.collection.JavaConverters._


// for dynamic class instantiation
import scala.reflect.runtime.{universe => ru}

import com.ryanquey.datautils.helpers.StringHelpers.{snakeToCamel, snakeToUpperCamel}
import com.ryanquey.datautils.models.{Model, Record}

// currently, actually doing openbible.info's TSK data only
class TSKDataFile (table : String, filename : String) {
  val dataDirPath : String = s"${sys.env("INTERTEXTUALITY_GRAPH_RAW_DATA_DIR")}/treasury-of-scripture-knowledge"
  val fullPath : Path = Paths.get(dataDirPath, filename)
  var headers : Array[String] = _;

  def parseFile() = {
    println(s"parsing $filename")
    val bufferedSource = Source.fromFile(fullPath.toString)

	  val csvDataFile : FileInputStream = new FileInputStream(fullPath.toString);
	  // need to handle first bytes which mess up first column
	  // https://stackoverflow.com/a/61815006/6952495
	  // http://commons.apache.org/proper/commons-csv/user-guide.html#Handling_Byte_Order_Marks
	  val csvDataReader : Reader = new InputStreamReader(new BOMInputStream(csvDataFile), "UTF-8");
    // cannot just split by comma, since many fields have multiples (so commas separate) or commas inside fields
		val csvRecords = CSVFormat.RFC4180.withFirstRecordAsHeader().withDelimiter('\\t').parse(csvDataReader);

	 	for (csvRecord : CSVRecord <- csvRecords.asScala) {
	 	  // iterate over our mapping to get what fields we want into the db columns that they map to

      // first, instantiate a record of our model
      // need to typecast (https://alvinalexander.com/scala/how-to-cast-objects-class-instance-in-scala-asinstanceof/) since these models implement Model interface
    
      val alludingText = new Text()

      val allRefsStr : String = csvRecord.get("refs") 
      val sourceTexts : Array[String] = allRefsStr.split(";").map((ref : String) => {
        // examples: 
        // ps 95:5
        // ps.104.3;ps.104.5-9
        new Text()
      })

      println(s"now should have a blank modelInstance: $modelInstance")
      for ((csvCol : String, data : Map[String, String]) <- fieldsMapping) {  
        // for the break to function as "continue"
        breakable {
          val dbCol : String = data.get("db_col").get
          val modelField : String = snakeToCamel(dbCol)

          // use reflection to dynamically set field
          // https://stackoverflow.com/a/1589919/6952495
          println(s"from csv col $csvCol to db col $dbCol")
          val rawValue = csvRecord.get(csvCol) 

          // dbCol corresponds with field in our model, so use that
          // can be integer, or string, or anything that C* java driver takes
          println(s"model field is $modelField")
          val value = convertRawValue(modelInstance, modelField, rawValue)

          if (value == null) {
            break
          }

          println(s"value to set: $value")
          println(s"setting to: ${snakeToCamel(dbCol)} using modelInstance.set${snakeToUpperCamel(dbCol)}")

          modelInstance.setV(dbCol, value)
        }
      }

			println(s"Persisting")
      modelInstance.persist();
			println(s"---- Persisted!! ----")
			println(s"---- Continuing to next ----")
    }

    // TODO I think it does not stop itself because it opened a file and did not close it (?)
  }

}
