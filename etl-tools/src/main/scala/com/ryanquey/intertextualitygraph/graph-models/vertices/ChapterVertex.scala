package com.ryanquey.intertextualitygraph.graphmodels

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import scala.collection.JavaConverters._ 
import scala.collection.mutable.ListBuffer
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.ryanquey.intertextualitygraph.modelhelpers.BookHelpers
import com.ryanquey.intertextualitygraph.modelhelpers.ChapterHelpers
import com.ryanquey.datautils.helpers.StringHelpers._;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex

import java.util.UUID;
import java.time.Instant;
import com.datastax.oss.driver.api.core.cql._;

import com.ryanquey.intertextualitygraph.helpers.shapeless.{CaseClassFromMap}
import com.ryanquey.datautils.models.{Model, Record}
/*
 * - NOTE make sure to keep fields in sync with com.ryanquey.intertextualitygraph.models.chapters.ChapterBase
 */
case class ChapterVertex(
  book : String,  // TEXT 
  number : Integer,  // INT
  canonical : Boolean,  // BOOLEAN 
  testament : String,  // TEXT 
  updatedAt : Instant,  // TIMESTAMP 

  yearWritten : Option[Integer],  // INT 
  author : Option[String],  // TEXT
  scrollmapperId : Option[String],  // TEXT 
  osisRef : Option[String],  // TEXT 
  bookSeriesId : Option[String],  // TEXT 
  // is actually just the number of the last verse. So even if a chapter skips verses, is just that last number
  verseCount : Option[Integer],  // INT
  comments : Option[String],  // TEXT
  ) extends GraphReferenceVertex[ChapterVertex] {
    def companionObject = ChapterVertex
  }

object ChapterVertex extends GraphReferenceVertexCompanion[ChapterVertex] {
  def apply(javabean : Chapter) = {
    new ChapterVertex(
      book=javabean.getBook(),
      number=javabean.getNumber(),
      canonical=javabean.getCanonical(),
      testament=javabean.getTestament(),
      updatedAt=javabean.getUpdatedAt(),

      yearWritten=Option(javabean.getYearWritten()),
      author=Option(javabean.getAuthor()),
      scrollmapperId=Option(javabean.getScrollmapperId()),
      osisRef=Option(javabean.getOsisRef()),
      bookSeriesId=Option(javabean.getBookSeriesId()),
      verseCount=Option(javabean.getVerseCount()),
      comments=Option(javabean.getComments()),
    )
  }
  def convertJavabeanModelInstances(javabeanModelInstances : Iterable[Model]) : Iterable[ChapterVertex] = {
    javabeanModelInstances.map((instance) => {
      // ChapterVertex requires Chapter type as arg 
      val typecastedInstance : Chapter = instance.asInstanceOf[Chapter]

      ChapterVertex(typecastedInstance)
    })
  }

  def valueMapToCaseClass(valueMap: java.util.Map[String, Any]) : ChapterVertex = {
     val typecastedMap = valueMap.asScala.toMap
     val mapWithFixedKeys = typecastedMap.map { case (key, value) => snakeToCamel(key) -> value }

     CaseClassFromMap[ChapterVertex](mapWithFixedKeys)
  }

  ///////////////////////////////////////////////////////////
  // METADATA HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * for now, just make thin wrapper around what we have already with BookHelpers
   * - a little bit of performance overhead, because has to iterate over everything again to convert into case classes
   * - if it becomes a performance issue, can have TheographicDataFile do something to instantiate as case classes directly instead
   */
  val allChaptersFromFile = convertJavabeanModelInstances(ChapterHelpers.allChaptersFromFile)

  /*
   * grab data for a chapter from the Theographic data File
   * - assumes English Canon 
   * - Does not touch the db
   * - only reads from file one time, when class is loaded
   * - provides an easy way to get chapter metadata quickly
   */ 
  def getChapterByRefData (bookName : String, chapterNum : Int) : ChapterVertex = {
    allChaptersFromFile.find((c) => c.book == bookName && c.number == chapterNum).get
  }

  def getChapterByNum (book : BookVertex, chapterNum : Int) : ChapterVertex = {
    getChapterByRefData(book.name, chapterNum) 
  }

  def getChapterForVerse (verse : VerseVertex) : ChapterVertex = {
    getChapterByRefData(verse.book, verse.chapter) 
  }


  /*
   * retrieve chapter java model instance using for ALL chapters between the two provided
   * - uses the same name that we use as db primary keys
   */ 
  def getChaptersBetween (startingChapter : ChapterVertex, endingChapter : ChapterVertex) : Iterable[ChapterVertex] = {
    val endingBookNumber = endingChapter.book
    val startingBook = BookVertex.getBookForChapter(startingChapter)
    val endingBook = BookVertex.getBookForChapter(endingChapter)
    val books = BookVertex.getBooksBetween(startingBook, endingBook)

    // https://alvinalexander.com/scala/how-to-create-mutable-list-in-scala-listbuffer-cookbook/
    val chaptersBetween = new ListBuffer[ChapterVertex]()

    // iterate over the books
    // - TODO probably could do this using chapters.filter, and probably better performance?? Except, would have to iterate over all chapters in bible a few times.
    books.foreach(book => {
      // note that endingBook could also be first book, if there' only one book
      val isFirstBook = book.name == startingBook.name
      val isFinalBook = book.name == endingBook.name

      // if this is NOT the first book, then start at beginning of the book
      val initialChapterNum : Int = if (isFirstBook) startingChapter.number else 1
      val finalChapterNum : Int = if (isFinalBook) endingChapter.number else book.chapterCount

      val chapterRange = initialChapterNum to finalChapterNum
      for (chapterNum <- chapterRange) {
        chaptersBetween += getChapterByNum(book, chapterNum)
      }

    }) 

    chaptersBetween
  }

  ///////////////////////////////////////////////////////////
  // GRAPH HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * 
   * There should only be one result, but rather than returning a Vertex, just return the traversal, so can continue traversing off of it without hitting the database yet
   * - can call `next()` on the return value to get a Vertex (I think...)
   */
  //def buildVertexTraversalFromPK (bookName : String, chapterNumber : Int) : GraphTraversal[Vertex, Vertex] = {
  def buildVertexTraversalFromPK (pk : List[Any]) : GraphTraversal[Vertex, Vertex] = {
    val g : GraphTraversalSource = CassandraDb.graph
    val bookName : String = pk(0).asInstanceOf[String]
    val chapterNumber : Int = pk(1).asInstanceOf[Int]

    val traversal = g.V().hasLabel("chapter")
      .has("book", bookName)
      .has("number", chapterNumber)

    traversal
  }

}
