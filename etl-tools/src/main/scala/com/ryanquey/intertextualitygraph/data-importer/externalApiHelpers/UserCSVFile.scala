package com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers
// oops...this isn't external api! Time to rename package...TODO


import java.nio.file.{Path, Paths, Files}
import java.io.{File, Reader, FileReader, InputStreamReader, FileInputStream}
import java.util.{List, ArrayList, Arrays}
import org.apache.commons.io.input.BOMInputStream
import scalaj.http._
import scala.io.Source
import util.control.Breaks._
import scala.Array
import sys.process._
import scala.language.postfixOps
import java.util.UUID

import java.nio.charset.StandardCharsets
import org.apache.commons.csv.{CSVParser, CSVRecord, CSVFormat}
import com.ryanquey.intertextualitygraph.dataimporter.constants.TSKConstants._
import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers.Helpers._
import com.ryanquey.intertextualitygraph.modelhelpers._

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

// needed so I can call .asScala
import scala.collection.JavaConverters._


// for dynamic class instantiation
import scala.reflect.runtime.{universe => ru}

import com.ryanquey.datautils.helpers.StringHelpers.{snakeToCamel, snakeToUpperCamel}
import com.ryanquey.datautils.models.{Model, Record}

// currently, actually doing openbible.info's TSK data only
class UserCSVFile (filePath : String) {
  var headers : Array[String] = _;

  def parseFile() = {
    println(s"parsing $filePath")


    // convert file references using our node job. Since there's no lib doing it in java/scala that I know of yet...
    // https://alvinalexander.com/scala/scala-execute-exec-external-system-commands-in-scala/
    // built using node 12, but probably other versions should be fine
    // TODO if performance becomes an issue, read each line as node returns it and parse that instead (?)
    val pathToNodeScript = s"${sys.env("INTERTEXTUALITY_GRAPH_ETL_TOOLS_DIR")}/scripts/js/convert-references.js"

    // csv filepath is first arg

    
    println(s"running: " + s"node ${pathToNodeScript} ${filePath}")
    s"node ${pathToNodeScript} ${filePath}" !

    // there should now be a file written to formattedFilePath

    val formattedFilePath = getFormattedCSVFilePath(filePath)
    println("formatted file path", formattedFilePath)

    val fullPath : Path = Paths.get(formattedFilePath)
    val bufferedSource = Source.fromFile(fullPath.toString)
	  val csvDataFile : FileInputStream = new FileInputStream(fullPath.toString);
	  val createdBy = "user-upload"

    try {
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
      
        val allSrcRefsStr : String = csvRecord.get("source_texts") 
        val srcTextId : String = csvRecord.get("source_text_id") 

        // unfortunately this will create multiple records for something like jhn.1.1, jhn.1.3-5, even though really it should only be one edge. It's fine for now though
        println("allRefsStr: " + allSrcRefsStr)
        val sourceTexts : Array[Text] = makeSourceTexts(srcTextId, allSrcRefsStr, createdBy)

        // examples: 
        // ps.95.5-9,lev.1.1
        // ps 1.1
        val alludinOsisRange : String = csvRecord.get("alluding_text") 
        // TODO might want better error handling...but if don't throw error here, will throw it later
        if (alludinOsisRange == null) {
          println(s"ERROR!!! This isn't going to persist, no alluding text osis range passed in")
        }




        // might be empty string...
        val alludingTextId : String = csvRecord.get("alluding_text_id") 
        val alludingText = makeTextInstance(alludingTextId, alludinOsisRange, createdBy)

        // TODO probably eventually, make these userId??? Or is that just going to be on separate field "user_id"?

        // don't want dupes, so find or create
        println(s"update or create ${alludingText}")
        TextHelpers.persistText(alludingText)
        
        for (srcText <- sourceTexts) {
          breakable {
            println(s"update or create sourceText ${srcText}")
            // if ref was a blank string, skip it
            if (srcText.getStartingBook() == null) {
              // continue
              break
            }
            
            // don't want dupes, so find or create
            TextHelpers.persistText(srcText)

            println(s"---- Connecting text... ----")
            val connectionType = csvRecord.get("connection_type")
            val confidenceLevelStr = csvRecord.get("confidence_level")
            val confidenceLevel = if (confidenceLevelStr == "") 0 else confidenceLevelStr.toFloat
            val volumeLevelStr = csvRecord.get("volume_level")
            val volumeLevel = if (volumeLevelStr == "") 0 else volumeLevelStr.toFloat
            val userId = "" // TODO
            val bealeCategories = csvRecord.get("beale_categories").split(";").toList
            // TODO oops! forgot to put this in the CSV file!
            //val connectionSignificance = csvRecord.get("connection_significance")
            // TODO oops! forgot to put this in the db!
            //val description = csvRecord.get("description")
            val comments = csvRecord.get("comments")
            val sourceVersion = csvRecord.get("source_version")
            // TODO oops! forgot to put this in the CSV
            // but actually, maybe we want to derive dynamically from the version instead in our api, ratheer than having that come from csv
            //val sourceLanguage = csvRecord.get("source_language")

            IntertextualConnectionsHelpers.connectTexts(
              srcText, 
              alludingText, 
              connectionType, 
              confidenceLevel.toFloat, 
              Some(volumeLevel.toFloat), 
              None, 
              Some(bealeCategories), 
              // connection significance
              Some(""), 
              Some(comments), 
              Some(sourceVersion)
            )
          }
        }

        println(s"---- Persisted!! ----")

        println(s"---- Continuing to next ----")
      }
    } catch {
      case e: Exception => throw e
    } finally {
      csvDataFile.close() 
      bufferedSource.close() 
    }

    // TODO I think it does not stop itself because it opened a file and did not close it (?)
  }

  def makeTextInstance (id : String, osisRange : String, createdBy : String) : Text = {
    val text = new Text()
    if (osisRange != "") {
      TextHelpers.populateFieldsfromOsis(osisRange, text)
    }

    text.setCreatedBy(createdBy)
    text.setUpdatedBy(createdBy)
    if (id != "") {
      val idUUID : UUID = UUID.fromString(id)
      text.setId(idUUID)
    }

    text
  }

  // TODO use for TSK importing also
  def makeSourceTexts (srcTextId : String, allSrcRefsStr : String, createdBy : String) = {
    if (srcTextId == "") {
      // use the references each as a separate source text
      // all other fields ALL apply to ALL source texts
      allSrcRefsStr.split(";").map((osisRange : String) => {
        // examples: 
        // ps.95.5
        // ps.104.3,ps.104.5-ps.104.9
        
        // sometimes there are typos, and you ahve two semicolons together. In that case, just skip
        
        val srcText = makeTextInstance("", osisRange, createdBy)

        srcText
      })

    } else {
      // references are all for the one source text they're updating
      // all other fields apply to this one source text
      val osisRange = allSrcRefsStr
      val srcText = makeTextInstance(srcTextId, osisRange, createdBy)

      // make array to match expected return value
      Array(srcText)
    }
  }
}
