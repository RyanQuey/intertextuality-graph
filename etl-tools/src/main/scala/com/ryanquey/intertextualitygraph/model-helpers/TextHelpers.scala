package com.ryanquey.intertextualitygraph.modelhelpers

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import scala.collection.JavaConverters._ 
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

import java.util.UUID;
import java.time.Instant;
import com.datastax.oss.driver.api.core.cql._;

// TODO make this, I think it's helpful. Can have better helpers
// case class Reference()

// avoiding 

object TextHelpers {
  // NOTE could be a range, but osisRange could also be a single ref. You won't know until you parse
  // main use case for this one though is building a text model instance
  def populateFieldsfromOsis (osisRange : String, text : Text) { 
    println(s"osisRange is: $osisRange")
    val refs = osisRange.split("-")

    val startingRef = refs(0)
    // NOTE might be the same as the starting ref, if there was no - 
    val endingRef = refs.last

    // parse startingRef
    val startingRefData = startingRef.split("\\.")
    println(s"starting ref is: $startingRef")
    val startingBookOsis = startingRefData(0)
    
    val startingBookName = BookHelpers.osisNameToName(startingBookOsis)
    text.setStartingBook(startingBookName)
    val startingChapter = startingRefData(1).toInt
    text.setStartingChapter(startingChapter)
    if (startingRefData.length > 2) {
      // has a verse (most do for TSK)
      val startingVerse = startingRefData(2).toInt
      text.setStartingVerse(startingVerse)
    }

    // parse endingRef
    val endingRefData = endingRef.split("\\.")
    val endingBookOsis = endingRefData(0)
    val endingBookName = BookHelpers.osisNameToName(endingBookOsis)
    text.setEndingBook(endingBookName)
    val endingChapter = endingRefData(1).toInt
    text.setEndingChapter(endingChapter)
    
    if (endingRefData.length > 2) {
      // has a verse (most do for TSK)
      val endingVerse = endingRefData(2).toInt
      text.setEndingVerse(endingVerse)
    }

    text.setCreatedBy("treasury-of-scripture-knowledge")
    text.setUpdatedBy("treasury-of-scripture-knowledge")
    
    // NOTE for TSK data at least, should not have any semicolon at this point, so will just be a single split passage.
    val splitPassages = osisRange.split(";").toList.asJava
    text.setSplitPassages(splitPassages)
  }

  // NOTE hits db, using solr
  // TODO find a way to deal with when verses are not set. Since if not set, will get more than what we want, the search will be too broad
  def findByReference (startingBook : String, startingChapter : Option[Int], startingVerse : Option[Int], endingBook : String, endingChapter : Option[Int], endingVerse : Option[Int], createdBy : Option[String]) : Text = {
    val startingChapterPart = startingChapter match {
      case None => ""
      case Some(str) => s" AND starting_chapter:${str}"
    }

    val endingChapterPart = endingChapter match {
      case None => ""
      case Some(str) => s" AND ending_chapter:${str}"
    }

    val startingVersePart = startingVerse match {
      case None => ""
      case Some(str) => s" AND starting_verse:${str}"
    }

    val endingVersePart = endingVerse match {
      case None => ""
      case Some(str) => s" AND ending_verse:${str}"
    }

    val createdByStr = createdBy match {
      case None => ""
      case Some(str) => s" AND created_by:${str}"
    }

    val solrQuery = s"""
      starting_book:"${startingBook}"
      $startingVersePart
      $startingChapterPart

      AND ending_book:"${endingBook}"
      $endingVersePart
      $endingChapterPart

      $createdByStr
      """

    val query = s"SELECT * FROM intertextuality_graph.texts WHERE solr_query = $solrQuery"

    println("running query: " + query)
    val dbMatch : Text = Text.findOneBySolr(solrQuery);

    dbMatch
  }

  // basically just assumes starting and ending reference are identical
  // NOTE hits db, using solr
  def findBySingleReference (book : String, chapter : Option[Int], verse : Option[Int]) : Text  = {
    // TODO could make just a shorter query for this, maybe faster? Are less params in a query faster?
    //

    TextHelpers.findByReference(book, chapter, verse, book, chapter, verse, None)
  }

  def testSolrQuery () : Text = {
    val dbMatch : Text = Text.findOneBySolr("starting_book:Genesis");
    dbMatch
  }

  // takes a Text instance and checks for mathcing text instance with teh same reference and creator
  // NOTE hits db, using solr
  def findMatchByRef (text : Text) : Text = {
    // https://stackoverflow.com/a/31977225/6952495
    // chapter and verse are optional, so be ready for some None values
    val startC = Option(text.getStartingChapter).map {_.toInt}
    val startV = Option(text.getStartingVerse).map {_.toInt}
    val endC = Option(text.getEndingChapter).map {_.toInt}
    val endV = Option(text.getEndingVerse).map {_.toInt}

    val dbMatch = findByReference(text.getStartingBook, startC, startV, text.getEndingBook, endC, endV, Some(text.getCreatedBy))
    dbMatch
  }

  // TODO find a way to deal with when verses are not set. Since if not set, will get more than what we want, the search will be too broad
  // This will update records we don't want updated potentially
  def updateOrCreateByRef (text : Text) = {
    // find by ref. Use the unchangeable columns, though eventually split_passages should work too. 

    val dbMatch = findMatchByRef(text)


    if (dbMatch == null) {
      // go ahead and create another
      text.persist();
    } else {
      // basically, update the old record with data from the new record
      // this is if dbMatch is a Row instance
      // text.setId(dbMatch.getUuid("id"))
      text.setId(dbMatch.getId())
      text.persist();
    }
  }

  // you don't want to do this all the time, but only when you don't have access to the id of a text, and want to make sure to not create duplicates (e.g., when importing data from a file, and want to be able to do so multiple times without creating duplicates)
  // NOTE does not update...that would require doing some stuff, and I dont need updating yet
  def createByRefIfNotExists (text : Text) = {
    // find by ref. Use the unchangeable columns, though eventually split_passages should work too. 
    // would do in dao, even though this si too specific and too many moving parts to entrust to their limited api in the dao. Makes it easy, you get your model back...but had way too much trouble. So just do string

    val dbMatch = findMatchByRef(text)


    if (dbMatch != null) {
      // go ahead and create another
      text.persist();
    }
  }
}
