package com.ryanquey.intertextualitygraph.graphmodels

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import com.ryanquey.intertextualitygraph.modelhelpers.BookHelpers
import scala.collection.JavaConverters._ 
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

import java.util.UUID;
import java.time.Instant;
import com.datastax.oss.driver.api.core.cql._;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex

import com.ryanquey.datautils.models.{Model, Record}
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
  ) extends GraphReferenceVertex

object BookVertex extends GraphReferenceVertexCompanion[BookVertex] {
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

  def convertJavabeanModelInstances(javabeanModelInstances : Iterable[Model]) : Iterable[BookVertex] = {
    javabeanModelInstances.map((instance) => {
      // BookVertex requires Book type as arg 
      val typecastedInstance : Book = instance.asInstanceOf[Book]

      BookVertex(typecastedInstance)
    })
  }

  ///////////////////////////////////////////////////////////
  // METADATA HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * for now, just make thin wrapper around what we have already with BookHelpers
   * - a little bit of performance overhead, because has to iterate over everything again to convert into case classes
   * - if it becomes a performance issue, can have TheographicDataFile do something to instantiate as case classes directly instead
   */
  val allBooksFromFile = convertJavabeanModelInstances(BookHelpers.allBooksFromFile)

  /*
   * retrieve book case class instance using standard name
   * - uses the same name that we use as db primary keys
   */ 
  def getBookByName (bookName : String) : BookVertex = {
    allBooksFromFile.find((b) => b.name == bookName).get
  }

  /*
   * taking a model instance of Chapter, returning book model instance
   *
   */
  def getBookForChapter (chapter : ChapterVertex) : BookVertex = {
    getBookByName(chapter.book) 
  }

  /*
   * retrieve book java model instances using for ALL books between the two provided, inclusive
   * - assumes English Bible book ordering
   */ 
  def getBooksBetween (startingBook : BookVertex, endingBook : BookVertex) : Iterable[BookVertex] = {
    allBooksFromFile.filter((b) => b.bookOrder >= startingBook.bookOrder && b.bookOrder <= endingBook.bookOrder)
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
