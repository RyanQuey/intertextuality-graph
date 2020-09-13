package com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers
import java.nio.file.{Path, Paths, Files}
import java.io.{File, Reader, FileReader, InputStreamReader, FileInputStream}
import java.util.{List, ArrayList, Arrays}
import org.apache.commons.io.input.BOMInputStream
import scalaj.http._
import scala.io.Source
import util.control.Breaks._
import scala.Array

import java.nio.charset.StandardCharsets
import org.apache.commons.csv.{CSVParser, CSVRecord, CSVFormat}
import com.ryanquey.intertextualitygraph.dataimporter.constants.TSKConstants._
import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers.Helpers._
import com.ryanquey.intertextualitygraph.modelhelpers._

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text

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
		val csvRecords = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csvDataReader);

	 	for (csvRecord : CSVRecord <- csvRecords.asScala) {
	 	  // iterate over our mapping to get what fields we want into the db columns that they map to

      // first, instantiate a record of our model
      // need to typecast (https://alvinalexander.com/scala/how-to-cast-objects-class-instance-in-scala-asinstanceof/) since these models implement Model interface
    
      val allRefsStr : String = csvRecord.get("refs") 

      // unfortunately this will create multiple records for something like jhn.1.1, jhn.1.3-5, even though really it should only be one edge. It's fine for now though
      println("allRefsStr: " + allRefsStr)
      val sourceTexts : Array[Text] = allRefsStr.split(";").map((osisRange : String) => {
        // examples: 
        // ps.95.5
        // ps.104.3;ps.104.5-ps.104.9
        
        // sometimes there are typos, and you ahve two semicolons together. In that case, just skip
        
        val srcText = new Text()
        if (osisRange != "") {
          TextHelpers.populateFieldsfromOsis(osisRange, srcText)
        }

        srcText
      })

      // examples: 
      // ps.95.5
      // ps.104.3;ps.104.5-ps.104.9
      val alludingText = new Text()

      val bookNum = csvRecord.get("book").toInt
      val atBook = BookHelpers.bookNumToName(bookNum)
      val atChapter = csvRecord.get("chapter").toInt
      val atVerse = csvRecord.get("verse").toInt
      alludingText.setStartingBook(atBook)
      alludingText.setStartingChapter(atChapter)
      alludingText.setStartingVerse(atVerse)
      // start and end are the same here
      alludingText.setEndingBook(atBook)
      alludingText.setEndingChapter(atChapter)
      alludingText.setEndingVerse(atVerse)

      alludingText.setCreatedBy("treasury-of-scripture-knowledge")
      alludingText.setUpdatedBy("treasury-of-scripture-knowledge")
      
      // make sur eto set type as Java string, or you will get Scala array back which doesn't convert to list the same way
      val splitPassages = allRefsStr.split(";").toList.asJava
      
      alludingText.setSplitPassages(splitPassages)

      
      for (srcText <- sourceTexts) {
        breakable {
  			  println(s"Persisting sourceText ${srcText} if not exists")
  			  // if ref was a blank string, skip it
  			  if (srcText.getStartingBook() == null) {
  			    // continue
  			    break
  			  }
  			  
  			  // don't want dupes, so find or create
          TextHelpers.updateOrCreateByRef(srcText)
        }
      }
      // don't want dupes, so find or create
			println(s"Persisting alludingText ${alludingText} if not exists")
      TextHelpers.updateOrCreateByRef(alludingText)

			println(s"---- Persisted!! ----")
			println(s"---- Continuing to next ----")
    }

    // TODO I think it does not stop itself because it opened a file and did not close it (?)
  }

}
