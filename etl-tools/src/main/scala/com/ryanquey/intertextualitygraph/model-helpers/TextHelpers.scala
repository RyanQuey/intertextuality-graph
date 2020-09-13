package com.ryanquey.intertextualitygraph.modelhelpers

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import scala.collection.JavaConverters._ 
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

import java.util.UUID;
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

  def findMatchByRef (text : Text) : Row = {
    val startingVersePart = if (text.getStartingVerse != null) s" AND starting_verse:${text.getStartingVerse} " else "";

    val endingVersePart = if (text.getEndingVerse != null) s" AND ending_verse:${text.getEndingVerse} " else "";
    val solrQuery = s"""solr_query = 'created_by:"${text.getCreatedBy}"
      AND ending_book:"${text.getEndingBook}"
      AND ending_chapter:${text.getEndingChapter}
      $endingVersePart

      AND starting_book:"${text.getStartingBook}"
      AND starting_chapter:${text.getStartingChapter}
      $startingVersePart
      
      ' LIMIT 1;
      """

    val query = s"SELECT * FROM intertextuality_graph.texts WHERE $solrQuery"

    println("running query: " + query)
    // val dbMatch : Text = Text.findOneBySolr(solrQuery);
    // val dbMatch : Text = Text.findOneByQuery(query);
    // consider using bindMarker() for performance if we end up using this a lot
    // val query = selectFrom("texts").all().whereColumn("solr_query").isEqualTo(literal(solrQuery).limit(1);
    val rs : ResultSet = CassandraDb.execute(query)
    val dbMatch : Row = rs.one()

    dbMatch
  }

  def updateOrCreateByRef (text : Text) = {
    // find by ref. Use the unchangeable columns, though eventually split_passages should work too. 

    val dbMatch = findMatchByRef(text)


    if (dbMatch == null) {
      // go ahead and create another
      text.persist();
    } else {
      text.setId(dbMatch.getUuid("id"))
      text.persist();
    }
  }

  // you don't want to do this all the time, but only when you don't have access to the id of a text, and want to make sure to not create duplicates (e.g., when importing data from a file, and want to be able to do so multiple times without creating duplicates)
  // NOTE does not update...that would require doing some stuff, and I dont need updating yet
  def createByRefIfNotExists (text : Text) = {
    // find by ref. Use the unchangeable columns, though eventually split_passages should work too. 
    // would do in dao, even though this si too specific and too many moving parts to entrust to their limited api in the dao. Makes it easy, you get your model back...but had way too much trouble. So just do string
    // using solr query, would be like so if we didn't use dao

    val dbMatch = findMatchByRef(text)


    if (dbMatch != null) {
      // go ahead and create another
      text.persist();
    }
  }
}
