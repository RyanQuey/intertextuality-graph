package com.ryanquey.intertextualitygraph.graphmodels

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import scala.collection.JavaConverters._ 
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

import java.util.UUID;
import java.time.Instant;
import com.datastax.oss.driver.api.core.cql._;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex

/*
 * - NOTE make sure to keep fields in sync with com.ryanquey.intertextualitygraph.models.chapters.BookBase
 */
case class BookVertex(
  name : String, // TEXT 
  yearWritten : Integer, // INT
  placeWritten : String, // TEXT 
  author : String, // TEXT
  bookOrder : Integer, // INT
  tyndaleAbbreviation : String, // TEXT
  osisAbbreviation : String, // TEXT
  slug : String, // TEXT
  theographicShortName : String, // TEXT
  scrollmapperId : String, // TEXT 
  canonical : Boolean, // BOOLEAN 
  bookSeries : String, // TEXT 
  testament : String, // TEXT 
  chapterCount : Integer, // INT
  verseCount : Integer, // INT
  comments : String, // TEXT
  updatedAt : Instant // TIMESTAMP, 
  )

object BookVertex {
  /*
   * overloading the apply method so we can instantiate the case class using the corresponding Java model class
   * - https://stackoverflow.com/a/2401153/6952495
   */
  def apply(javabean : Book) = {
    new BookVertex(
      javabean.getName(),
      javabean.getYearWritten(),
      javabean.getPlaceWritten(),
      javabean.getAuthor(),
      javabean.getBookOrder(),
      javabean.getTyndaleAbbreviation(),
      javabean.getOsisAbbreviation(),
      javabean.getSlug(),
      javabean.getTheographicShortName(),
      javabean.getScrollmapperId(),
      javabean.getCanonical(),
      javabean.getBookSeries(),
      javabean.getTestament(),
      javabean.getChapterCount(),
      javabean.getVerseCount(),
      javabean.getComments(),
      javabean.getUpdatedAt()
      )
  }

  ///////////////////////////////////////////////////////////
  // GRAPH HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * 
   * There should only be one result, but rather than returning a Vertex, just return the traversal, so can continue traversing off of it without hitting the database yet
   * - can call `next()` on the return value to get a Vertex (I think...)
   */
  def buildVertexTraversalFromPK (bookName : String) : GraphTraversal[Vertex, Vertex] = {
    val g : GraphTraversalSource = CassandraDb.graph
    val bookVertex = g.V().hasLabel("text")
      .has("starting_book", bookName)

    bookVertex
  }

}
