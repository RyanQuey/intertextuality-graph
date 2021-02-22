package com.ryanquey.intertextualitygraph.modelhelpers

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import scala.collection.JavaConverters._ 
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import org.apache.tinkerpop.gremlin.structure.Vertex
import com.ryanquey.intertextualitygraph.graphmodels.BookVertex
import com.ryanquey.intertextualitygraph.graphmodels.TextVertex
import com.ryanquey.intertextualitygraph.graphmodels.BookVertex._

import java.util.UUID;
import java.time.Instant;
import com.datastax.oss.driver.api.core.cql._;

/*
 * (DEPRECATED)
 * for helpers that interact with the java model Class; 
 * - for anything that interacts with the scala case class use TextVertex companion object
 * - And, in general, don't add to this much, so it's easy to migrate over from the java models in the future. Instead of writing methods here, convert the java class > case class, and then use method from TextVertex companion object
 */

object TextHelpers {

  ///////////////////////////////////////////////////////////
  // CRUD
  ///////////////////////////////////////////////////////////

  /*
   * - we should never use persistText(text) directly, or else we will not take advantage of this helpers hooks
   * - TODO as we pull more and more out of this file and put it into TextVertex class, will have to change this definition
   *   * eventually this should be no more than a pointer to the TextVertex method, and then after that, get rid of it altogether
   */ 
  def persistText (text : Text) = {
    // TODO create a system so that can check if changes are actually made, and what changes were made. 
    // ie, some sort of "dirty" flag, or getDirty method
    // only really helpful if we end up having a lot of people changing texts

    val textVertex = TextVertex(text)
    println("stextVertex: $textVertex");
    textVertex.updateReferenceVertices


    text.persist()
  }
  ///////////////
  // CREATE
  ///////////////

  /*
   *
   *  find by ref. Use the unchangeable columns, though eventually split_passages should work too. 
   * - you don't want to do this all the time, but only when you don't have access to the id of a text, and want to make sure to not create duplicates (e.g., when importing data from a file, and want to be able to do so multiple times without creating duplicates)
   * - NOTE does not update...that would require doing some stuff, and I dont need updating yet
   * - would do in dao, even though this si too specific and too many moving parts to entrust to their limited api in the dao. Makes it easy, you get your model back...but had way too much trouble. So just do string
   *
   */
  def createByRefIfNotExists (text : Text) = {

    val dbMatch = findMatchByRef(text)


    if (dbMatch != null) {
      // go ahead and create another
      persistText(text);
    }
  }

  ///////////////
  // READ
  ///////////////

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


  ///////////////
  // UPDATE
  ///////////////

  // TODO find a way to deal with when verses are not set. Since if not set, will get more than what we want, the search will be too broad
  // NOTE also by default searches by createdBy
  // This will update records we don't want updated potentially
  def updateOrCreateByRef (text : Text) = {
    // find by ref. Use the unchangeable columns, though eventually split_passages should work too. 

    val dbMatch = findMatchByRef(text)

    if (dbMatch == null) {
      // go ahead and create another
      persistText(text);
    } else {
      // basically, update the old record with data from the new record
      // this is if dbMatch is a Row instance
      // text.setId(dbMatch.getUuid("id"))
      text.setId(dbMatch.getId())
      persistText(text);
    }
  }

  ///////////////////////////////////////////////////////////
  // MUTATION HELPERS
  ///////////////////////////////////////////////////////////

  // NOTE could be a range (ps.1.1-ps.1.6), but osisRangeList could also be a single ref (ps.1.1) or list of refs (ps.1.1-ps.1.3,ps.1.4). You won't know until you parse
  // main use case for this one though is building a text model instance
  // For now, at least assuming that the list of refs are in order...we did make it a LIST after all in C* not a SET! Let's have some order here!
  // TODO eventually make a port for this using case class. Both could largely depend on a shared helper
  def populateFieldsfromOsis (osisRangeList : String, text : Text) { 
    println(s"osisRangeList is: $osisRangeList")
    val allRefs : Array[String] = osisRangeList.split(",")
    val startingRange : String = allRefs(0)
    // NOTE might be same as startingRange. Note that it might not be a range, but we're treating it as such
    val endingRange = allRefs.last


    // get it down to two single refs. E.g,. From Gen.1.1-Gen.10.1,Gen.11.1 to: startingRef is Gen.1.1 and endingRef is Gen.11.1
    // in this starting range
    val startingRef : String = startingRange.split("-")(0)
    // NOTE might be the same as the starting ref, if there was no commas (,) AND no dash (-)
    val endingRef : String = endingRange.split("-").last

    // parse startingRef
    val startingRefData = startingRef.split("\\.")
    println(s"starting ref is: $startingRef")
    val startingBookOsis = startingRefData(0)
    
    val startingBookData = BookHelpers.getBookByOsis(startingBookOsis)
    val startingBookName = startingBookData.getName

    println(s"starting book is: $startingBookName")
    text.setStartingBook(startingBookName)
    println(s"starting ch is: ${startingRefData(1).toInt}")
    val startingChapter = startingRefData(1).toInt
    text.setStartingChapter(startingChapter)
    val hasStartingVerse = startingRefData.length > 2
    if (hasStartingVerse) {
      // has a verse (most do for TSK)
      val startingVerse = startingRefData(2).toInt
      text.setStartingVerse(startingVerse)
    }
    val startingRefIndex = TextVertex.getIndexForRef(
      startingBookName, 
      startingChapter, 
      if (!hasStartingVerse) None else Some(text.getStartingVerse)
    )

    text.setStartingRefIndex(startingRefIndex)

    // parse endingRef
    val endingRefData = endingRef.split("\\.")
    val endingBookOsis = endingRefData(0)
    val endingBookName = BookHelpers.osisNameToName(endingBookOsis)
    text.setEndingBook(endingBookName)
    val endingChapter = endingRefData(1).toInt
    text.setEndingChapter(endingChapter)
    
    val hasEndingVerse = endingRefData.length > 2
    if (hasEndingVerse) {
      // has a verse (most do for TSK)
      val endingVerse = endingRefData(2).toInt
      text.setEndingVerse(endingVerse)
    }
    val endingRefIndex = TextVertex.getIndexForRef(
      endingBookName, 
      endingChapter, 
      if (!hasEndingVerse) None else Some(text.getEndingVerse)
    )
    text.setEndingRefIndex(endingRefIndex)
    
    // NOTE for TSK data at least, should not have any semicolon at this point, so will just be a single split passage.
    val splitPassages = osisRangeList.split(";").toList.asJava
    text.setSplitPassages(splitPassages)

    // set text metadata based on starting book (these should be same between starting and ending book)
    // TODO add validation to make sure this is true!
    val testament = startingBookData.getTestament
    text.setTestament(testament)
    val canonical = startingBookData.getCanonical
    text.setCanonical(canonical)
  }

}
