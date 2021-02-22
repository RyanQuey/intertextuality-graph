package com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers
import java.nio.file.{Path, Paths, Files}
import java.io.{File, Reader, FileReader, InputStreamReader, FileInputStream}
import java.util.{Arrays}
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
import com.ryanquey.intertextualitygraph.graphmodels.TextVertex
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

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
	  try {
      // cannot just split by comma, since many fields have multiples (so commas separate) or commas inside fields
      val csvRecords = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csvDataReader);

      for (csvRecord : CSVRecord <- csvRecords.asScala) {
        // iterate over our mapping to get what fields we want into the db columns that they map to

        // first, instantiate a record of our model
        // need to typecast (https://alvinalexander.com/scala/how-to-cast-objects-class-instance-in-scala-asinstanceof/) since these models implement Model interface
      
        // counting refs as source texts. Why not....and the original passage that TSK indexed these by is going to be the alluding passage
        val allRefsStr : String = csvRecord.get("refs") 

        // unfortunately this will create multiple records for something like jhn.1.1; jhn.1.3-5, even though really it should only be one edge. It's fine for now though
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
          srcText.setCreatedBy("treasury-of-scripture-knowledge")
          srcText.setUpdatedBy("treasury-of-scripture-knowledge")

          srcText
        })

        // examples: 
        // 1 1 1 (for Gen 1.1)
        // no ranges possible here
        val alludingText = new Text()

        val bookNum = csvRecord.get("book").toInt
        // at == alludingText
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
        val atOsisBookName = BookHelpers.bookNumToOsisName(bookNum)
        val splitPassages = List(s"$atOsisBookName.$atChapter.$atVerse").asJava
        
        val startingRefIndex = TextVertex.getIndexForStartingRef(
          atBook, 
          atChapter, 
          Some(atVerse)
        )
        val endingRefIndex = TextVertex.getIndexForEndingRef(
          atBook, 
          atChapter, 
          Some(atVerse)
        )
        alludingText.setStartingRefIndex(startingRefIndex)
        alludingText.setEndingRefIndex(endingRefIndex)


        alludingText.setSplitPassages(splitPassages)

        // don't want dupes, so find or create
        println(s"Persisting alludingText ${alludingText} if not exists")
        TextHelpers.updateOrCreateByRef(alludingText)
        
        // we are using the "words" given by TSK as the description
        val icDescription = s"relevant word: ${csvRecord.get("words")}"
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
            println(s"---- Connecting text... ----")
            
            IntertextualConnectionsHelpers.connectTexts(srcText, alludingText, "from-generic-list", 30.toFloat, None, None, None, None, None, Some(icDescription))
          }
        }

        println(s"---- Persisted!! ----")

        println(s"---- Continuing to next ----")
      }

    } catch {
      case e: Exception => throw e
    } finally {
      // TODO I just added this, need to test
      csvDataFile.close() 
      bufferedSource.close() 
    }
  }

}
