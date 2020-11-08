package com.ryanquey.intertextualitygraph.graphmodels

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import scala.collection.JavaConverters._ 
import scala.collection.mutable.ListBuffer
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex

import com.ryanquey.intertextualitygraph.modelhelpers.BookHelpers
import com.ryanquey.intertextualitygraph.modelhelpers.ChapterHelpers
import com.ryanquey.intertextualitygraph.modelhelpers.VerseHelpers
import com.ryanquey.intertextualitygraph.helpers.shapeless.{CaseClassFromMap}
import com.ryanquey.datautils.helpers.StringHelpers._;

import java.util.UUID;
import java.time.Instant;
import com.datastax.oss.driver.api.core.cql._;
import com.ryanquey.datautils.models.{Model, Record}

/*
 * - I put the CQL data type to the right of the field definition
 * - NOTE make sure to keep fields in sync with com.ryanquey.intertextualitygraph.models.chapters.VerseBase
 */
case class VerseVertex(
  number : Integer,  // INT
  chapter : Integer,  // INT 
  book : String,  // TEXT 
  canonical : Boolean,  // BOOLEAN 
  testament : String,  // TEXT 
  updatedAt : Instant, // TIMESTAMP, 

  yearWritten : Option[Integer],  // INT 
  author : Option[String],  // TEXT
  osisRef : Option[String],  // TEXT 
  scrollmapperId : Option[String],  // TEXT 
  canonicalText : Option[String],  // TEXT
  kjvText : Option[String],  // TEXT
  mtText : Option[String],  // TEXT
  rahlfsLxxText : Option[String],  // TEXT
  sblGntText : Option[String],  // TEXT
  byzGntText : Option[String],  // TEXT
  bookSeries : Option[String],  // TEXT 
  comments : Option[String],  // TEXT
  ) extends GraphReferenceVertex[VerseVertex] {
    def companionObject = VerseVertex
  }

object VerseVertex extends GraphReferenceVertexCompanion[VerseVertex] {
  /*
   * overloading the apply method so we can instantiate the case class using the corresponding Java model class
   * - https://stackoverflow.com/a/2401153/6952495
   */
  def apply(javabean : Verse) = {
    new VerseVertex(
      number = javabean.getNumber,
      chapter = javabean.getChapter,
      book = javabean.getBook,
      canonical = javabean.getCanonical,
      testament = javabean.getTestament,
      updatedAt = javabean.getUpdatedAt,

      yearWritten = Option(javabean.getYearWritten),
      author = Option(javabean.getAuthor),
      osisRef = Option(javabean.getOsisRef),
      scrollmapperId = Option(javabean.getScrollmapperId),
      canonicalText = Option(javabean.getCanonicalText),
      kjvText = Option(javabean.getKjvText),
      mtText = Option(javabean.getMtText),
      rahlfsLxxText = Option(javabean.getRahlfsLxxText),
      sblGntText = Option(javabean.getSblGntText),
      byzGntText = Option(javabean.getByzGntText),
      bookSeries = Option(javabean.getBookSeries),
      comments = Option(javabean.getComments),
      )
  }

  def convertJavabeanModelInstances(javabeanModelInstances : Iterable[Model]) : Iterable[VerseVertex] = {
    javabeanModelInstances.map((instance) => {
      // VerseVertex requires Verse type as arg 
      val typecastedInstance : Verse = instance.asInstanceOf[Verse]

      VerseVertex(typecastedInstance)
    })
  }

  def valueMapToCaseClass(valueMap: java.util.Map[String, Any]) : VerseVertex = {
     val typecastedMap = valueMap.asScala.toMap

     val mapWithFixedKeys = typecastedMap.map { case (key, value) => snakeToCamel(key) -> value }

     CaseClassFromMap[VerseVertex](mapWithFixedKeys)
  }

  ///////////////////////////////////////////////////////////
  // METADATA HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * for now, just make thin wrapper around what we have already with BookHelpers
   * - a little bit of performance overhead, because has to iterate over everything again to convert into case classes
   * - if it becomes a performance issue, can have TheographicDataFile do something to instantiate as case classes directly instead
   */
  val allVersesFromFile = convertJavabeanModelInstances(VerseHelpers.verses)

  ///////////////////////////
  // Get verses by filter
  ///////////////////////////
  def getVerseByNum (book : BookVertex, chapter : ChapterVertex, verseNum : Int) : VerseVertex = {
    getVerseByRefData(book.name, chapter.number, verseNum) 
  }

  /*
   * grab data for a verse from the Theographic data File
   * - assumes English Canon 
   * - Does not touch the db
   * - only reads from file one time, when class is loaded
   * - provides an easy way to get verse metadata quickly
   */ 
  def getVerseByRefData (bookName : String, chapterNum : Int, verseNum : Int) : VerseVertex = {
    allVersesFromFile.find((v) => v.book == bookName && v.chapter == chapterNum && v.number == verseNum).get
  }
  
  /*
   * retrieve verse java model instance using for ALL chapters between the two provided
   * - uses the same name that we use as db primary keys
   *   TODO try not to use this one, use VerseVertex implementation instead
   */ 
  def getVersesBetween (startingVerse : VerseVertex, endingVerse : VerseVertex) : Iterable[VerseVertex] = {
    val endingChapterNumber = endingVerse.chapter
    val startingChapter = ChapterVertex.getChapterForVerse(startingVerse)
    val endingChapter = ChapterVertex.getChapterForVerse(endingVerse)
    val chapters = ChapterVertex.getChaptersBetween(startingChapter, endingChapter)

    // https://alvinalexander.com/scala/how-to-create-mutable-list-in-scala-listbuffer-cookbook/
    val versesBetween = new ListBuffer[VerseVertex]()

    // iterate over the chapters
    // - TODO probably could do this using verses.filter, and probably better performance?? Except, would have to iterate over all verses in bible a few times.
    chapters.foreach(chapter => {
      // note that endingChapter could also be first chapter, if there' only one chapter
      val isFirstChapter = chapter.number == startingChapter.number
      val isFinalChapter = chapter.number == endingChapter.number

      // if this is NOT the first chapter, then start at beginning of the chapter
      val initialVerseNum : Int = if (isFirstChapter) startingVerse.number else 1
      val finalVerseNum : Int = if (isFinalChapter) endingVerse.number else chapter.verseCount.get

      val verseRange = initialVerseNum to finalVerseNum
      for (verseNum <- verseRange) {
        versesBetween += getVerseByRefData(chapter.book, chapter.number, verseNum)
      }

    }) 

    versesBetween
  }

  ///////////////////////////////////////////////////////////
  // GRAPH HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * 
   * There should only be one result, but rather than returning a Vertex, just return the traversal, so can continue traversing off of it without hitting the database yet
   * - can call `next()` on the return value to get a Vertex (I think...)
   */
  //def buildVertexTraversalFromPK (bookName : String, chapterNumber : Int, verseNumber : Int) : GraphTraversal[Vertex, Vertex] = {
  def buildVertexTraversalFromPK (pk : List[Any]) : GraphTraversal[Vertex, Vertex] = {
    val g : GraphTraversalSource = CassandraDb.graph
    // tuple version
    //val (bookName : String, chapterNumber : Int, verseNumber : Int) = pk
    val bookName : String = pk(0).asInstanceOf[String]
    val chapterNumber : Int = pk(1).asInstanceOf[Int]
    val verseNumber : Int = pk(2).asInstanceOf[Int]

    val traversal = g.V().hasLabel("chapter")
      .has("book", bookName)
      .has("chapter", chapterNumber)
      .has("number", verseNumber)

    traversal
  }

}
