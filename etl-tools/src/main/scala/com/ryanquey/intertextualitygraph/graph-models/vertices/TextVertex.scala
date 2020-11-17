package com.ryanquey.intertextualitygraph.graphmodels

import java.util.{UUID}
import java.time.Instant;

//import scala.collection.JavaConverters._ 
//import scala.collection.JavaConverters._ 
import scala.jdk.CollectionConverters._

import com.datastax.oss.driver.api.core.cql._;
import scala.collection.immutable.List
import scala.util.{Try, Success, Failure}

import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.{Vertex, Edge}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold

import gremlin.scala._

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.ryanquey.datautils.helpers.StringHelpers._;
import java.time.Instant;

import com.ryanquey.intertextualitygraph.graphmodels.GraphModel
import com.ryanquey.intertextualitygraph.graphmodels.GraphReferenceVertex
import com.ryanquey.intertextualitygraph.graphmodels.BookVertex
import com.ryanquey.intertextualitygraph.graphmodels.BookVertex._

import com.ryanquey.intertextualitygraph.helpers.shapeless.{CaseClassFromMap}
import com.ryanquey.datautils.models.{Model, Record}
import com.ryanquey.intertextualitygraph.helpers.Reflection.{fromMap, getFieldsOfTypeForClass}

// TODO these are just temp to test
import com.datastax.dse.driver.api.core.graph._;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;

import scala.reflect._
import scala.reflect.runtime.universe._

import com.datastax.oss.driver.api.querybuilder.QueryBuilder._;

import org.crosswire.jsword.passage.OsisParser;
import org.crosswire.jsword.passage.VerseRange;
import org.crosswire.jsword.versification.Versification;
import org.crosswire.jsword.versification.system.Versifications;
import org.crosswire.jsword.passage.{Verse => JswordVerse}
import org.crosswire.jsword.book.Books;
import org.crosswire.jsword.versification.BibleNames

/*
 * - NOTE make sure to keep fields in sync with com.ryanquey.intertextualitygraph.models.chapters.ChapterBase
 */
// haven't gotten this to work at. from gremlin scala lib
@label("text")
case class TextVertex(
  // maybe use @id annotation, looks optional
  id : UUID,  
  startingBook : String,  
  splitPassages : List[String],  // LIST<TEXT>
  startingChapter : Integer,  
  endingBook : String,  
  endingChapter : Integer, 
  testament : String,  // TEXT 
  canonical : Boolean,  // BOOLEAN 
  updatedAt : Instant, // TIMESTAMP, 
  createdBy : String,
  updatedBy : String, 

  startingVerse : Option[Integer],  
  endingVerse : Option[Integer],  
  yearWritten : Option[Integer],  // INT 
  author : Option[String],  // TEXT
  canonicalText : Option[String],  // TEXT
  greekTranslation : Option[String],  // TEXT 
  englishTranslation : Option[String],  // TEXT 
  comments : Option[String],  // TEXT

) extends GraphReferenceVertex[TextVertex] {
  def companionObject = TextVertex
  /*
   *
   * - needs to maintain order, since we will pass in primary key in order sometimes (C* generally requires knowing the primary key in order). So use a list, not set
   */ 
  def getPrimaryKey() = {
    List(
      this.startingBook,
      this.id,
      )
  }

  ///////////////////////////////////////////////////////////
  // CRUD
  ///////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////
  // ASSOCIATION CRUD
  ///////////////////////////////////////////////////////////

  ///////////////
  // ASSOCIATION CREATE
  ///////////////

  /*
   * take a given text vertex and create references to all books, chapters, and verses
   * - Unfortunately, due to the way that C* works, never know if we are truly "creating" something, since it is just an upsert. We might never use this function
   * - keeping it as functional as possible, but not setting this on the case class
   * - TODO make this a transaction of some sort?
   *
   */ 
  def createReferenceVertices () : Unit = {
    // fetch metadata (as represented by case class instances) for each book/ch/v overlapping with this text
    val (books, chapters, verses) = getReferenceMetadata()

    books.foreach((b) => {
      println(s"creating book edge for $b");
      createEdgeToBook(b.name)}) 

    chapters.foreach((c) => createEdgeToChapter(c.book, c.number)) 
    verses.foreach((v) => {
      println(s"creating verse edge for $v");
      createEdgeToVerse(v.book, v.chapter, v.number)
      })
  }

  ///////////////
  // ASSOCIATION READ
  ///////////////

  /*
   * fetch all books that this text includes
   *
   */ 
  def fetchBooks () = {
    val textTraversal = buildVertexTraversal()
    val id = textTraversal.values("id").by(unfold()).next()
    
  }


  ///////////////
  // ASSOCIATION UPDATE
  ///////////////

  /*
   * take a given text vertex and update references to all books, chapters, and verses
   * - TODO don't just dropping all edges to references and creating new ones, create a lot of tombstones every time any edit on a text is made
   * - TODO really, if this is an update, don't need to drop/create book edges. This could be a fairly large performance gain, and enough to be worth doing a doing a read first before write
   *
   */ 
  def updateReferenceVertices () : Unit = {
    // 1) fetch all currently existing edges to reference vertices (book, ch, v) (do not delete the vertices themselves!)
    // TODO do it this way for better performance

    // 2) delete all old edges
    // TODO for now just dropping all
    buildTraversal.outE("from_book").drop
    buildTraversal.outE("from_chapter").drop
    buildTraversal.outE("from_verse").drop

    // 3) create vertices that don't exist yet
    // for each book that does not currently exist, create a new edge
    // TODO for now just creating edges for all, whether or not they previously existed
    
    createReferenceVertices() 

  }

  ///////////////////////////////////////////////////////////
  // METADATA HELPERS
  ///////////////////////////////////////////////////////////




  /*
   * for a given textVertex, return java model instances of all book, ch, and verses that this text intersects with
   * - could create a helper to get graphModels instead, but not bothering with that for now
   */ 
  def getReferenceMetadata () = {
    // extract out all books for this text
    val startingBook = BookVertex.getBookByName(this.startingBook)
    val startingChapter = ChapterVertex.getChapterByNum(startingBook, this.startingChapter)

    val endingBook = BookVertex.getBookByName(this.endingBook)
    val endingChapter = ChapterVertex.getChapterByNum(endingBook, this.endingChapter)

    val books = BookVertex.getBooksBetween(startingBook, endingBook)
    
    // extract out all chapters for this text
    val chapters = ChapterVertex.getChaptersBetween(startingChapter, endingChapter)
    
    // if verses are not specified, then getting for the whole chapter
    val startingVerseNumber : Int = if (this.startingVerse == None) 1 else startingChapter.verseCount
    val endingVerseNumber : Int = if (this.endingVerse == None) 1 else endingChapter.verseCount
    val startingVerse : VerseVertex = VerseVertex.getVerseByNum(startingBook, startingChapter, startingVerseNumber)
    val endingVerse : VerseVertex = VerseVertex.getVerseByNum(endingBook, endingChapter, endingVerseNumber)

    // extract out all verses for this text
    val verses = VerseVertex.getVersesBetween(startingVerse, endingVerse)

    (books, chapters, verses)
  }

  ///////////////////////////////////////////////////////////
  // GRAPH HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * 
   * There should only be one result, but rather than returning a Vertex, just return the traversal, so can continue traversing off of it without hitting the database yet
   * - can call `next()` on the return value to get a Vertex (I think...)
   */
  def buildVertexTraversal () : GraphTraversal[Vertex, Vertex] = {
    buildVertexTraversalFromPK(List(this.startingBook, this.id))
  }


  ///////////////////////////////////////////////////////////
  // PRIVATE METHODS
  ///////////////////////////////////////////////////////////


  /*
   * creates edge record between text and book
   * - assumes that book already exists (which always should, if we imported the seed that already)
   * - should not ever be needed to call directly, I don't think.
   *   TODO I'm sure there's a way to do all of this in a single traversal, getting the book name from the properties, assigning it using .as("bookName") or something, then using it to get all teh books? But I think it might be hard to find books in between...and chapters and verses would have the same issue
   */
  private def createEdgeToBook (bookName : String) : Unit = {
    try { 
      /*
       * TODO this is how I should be doing it, but going to try using cql since I'm not able to get this working
      val textVertexTraversal = buildVertexTraversal()
      val bookVertexTraversal = BookVertex.buildVertexTraversalFromPK(List(bookName))
      val bookVertex = bookVertexTraversal.next()

      println(s"adding edge from text ${this.id} to book $bookName");
      println(s"- using text traversal $textVertexTraversal");

      //textVertexTraversal.addE("from_book").from(bookVertexTraversal)
      val addETraversal : GraphTraversal[Vertex,Edge] = textVertexTraversal.addE("from_book").from(bookVertex)
      println(s"result of adding edge : $addETraversal");
      addETraversal.next()
     // textVertexTraversal.addOutE("from_book", bookVertex)
     */

      var query = insertInto("text_from_book_edges")
        .value("book_name", literal(bookName))
        .value("text_starting_book", literal(this.startingBook))
        .value("text_id", literal(this.id))
        .value("updated_at", literal(Instant.now()))

      CassandraDb.execute(s"$query ;");
      
    } catch {
      case e: InvalidQueryException => {
        e.printStackTrace
        e.getStackTrace.mkString("\n")
        throw e
      }
      case e: Exception => throw e
    }
  }




  /*
   * creates edge record between text and chapter
   * - assumes that book and chapter already exists (which always should, if we imported the seed that already)
   *  TODO update with changes made from createEdgeToBook once I get it working up there
   *
   */
  private def createEdgeToChapter (bookName : String, chapterNumber : Int) : Unit = {
      /*
       * TODO this is how I should be doing it, but going to try using cql since I'm not able to get this working
      val textVertexTraversal = buildVertexTraversal()
    val textVertexTraversal = buildVertexTraversal()
    val chapterVertexTraversal = ChapterVertex.buildVertexTraversalFromPK(List(bookName, chapterNumber))

    textVertexTraversal.addE("from_chapter").from(chapterVertexTraversal).next()
    */

      var query = insertInto("text_from_chapter_edges")
        .value("chapter_number", literal(chapterNumber))
        .value("chapter_book", literal(bookName))
        .value("text_starting_book", literal(this.startingBook))
        .value("text_id", literal(this.id))
        .value("updated_at", literal(Instant.now()))

      CassandraDb.execute(s"$query ;");

  }
  /*
   * creates edge record between text and verse
   * - assumes that book and chapter and verse already exists (which always should, if we imported the seed that already)
   *  TODO update with changes made from createEdgeToBook once I get it working up there
   *
   */
  private def createEdgeToVerse (bookName : String, chapterNumber : Int, verseNumber : Int) : Unit = {
      /*
       * TODO this is how I should be doing it, but going to try using cql since I'm not able to get this working
      val textVertexTraversal = buildVertexTraversal()
    val textTraversal = buildVertexTraversal()
    val verseTraversal = VerseVertex.buildVertexTraversalFromPK(List(bookName, chapterNumber, verseNumber))

    textTraversal.addE("from_verse").from(verseTraversal).next()
    */
      var query = insertInto("text_from_verse_edges")
        .value("verse_number", literal(verseNumber))
        .value("verse_chapter", literal(chapterNumber))
        .value("verse_book", literal(bookName))
        .value("text_starting_book", literal(this.startingBook))
        .value("text_id", literal(this.id))
        .value("updated_at", literal(Instant.now()))

      CassandraDb.execute(s"$query ;");
  }
}







object TextVertex extends GraphReferenceVertexCompanion[TextVertex] {
  /*
   * overloading the apply method so we can instantiate the case class using the corresponding Java model class
   * - https://stackoverflow.com/a/2401153/6952495
   * - http://alvinalexander.com/source-code/scala-how-create-case-class-multiple-alternate-constructors/
   */
  def apply(javabean : Text) = {
    new TextVertex(
      id = javabean.getId,
      startingBook = javabean.getStartingBook,
      createdBy = javabean.getCreatedBy,
      updatedBy = javabean.getUpdatedBy,
      updatedAt = javabean.getUpdatedAt,
      canonical = javabean.getCanonical,
      splitPassages = javabean.getSplitPassages.asScala.toList,
      startingChapter = javabean.getStartingChapter,
      endingBook = javabean.getEndingBook,
      endingChapter = javabean.getEndingChapter,
      testament = javabean.getTestament,

      endingVerse = Option(javabean.getEndingVerse),
      startingVerse = Option(javabean.getStartingVerse),
      author = Option(javabean.getAuthor),
      canonicalText = Option(javabean.getCanonicalText),
      greekTranslation = Option(javabean.getGreekTranslation),
      englishTranslation = Option(javabean.getEnglishTranslation),
      comments = Option(javabean.getComments),
      yearWritten = Option(javabean.getYearWritten),
      )
  }

  /*
   */
  def convertJavabeanModelInstances(javabeanModelInstances : Iterable[Model]) : Iterable[TextVertex] = {
    javabeanModelInstances.map((instance) => {
      // TextVertex requires Text type as arg 
      val typecastedInstance : Text = instance.asInstanceOf[Text]

      TextVertex(typecastedInstance)
    })
  }

  def preparedValueMapToCaseClass(preparedValueMap: Map[String, Any]) : TextVertex = {

    // CaseClassFromMap[TextVertex](preparedValueMap)
    fromMap[TextVertex](preparedValueMap) 
  }

  def getOptionalFields() = {
    Set(
      "yearWritten",
      "startingVerse",
      "endingVerse",
      "author",
      "canonicalText",
      "greekTranslation",
      "englishTranslation",
      "comments"
    )
  }

  def getFieldsOfType[T: TypeTag: ClassTag] () : List[String] = getFieldsOfTypeForClass[TextVertex, T]

  /*
   *
   * - needs to maintain order, since we will pass in primary key in order sometimes (C* generally requires knowing the primary key in order). So use a list, not set
   */ 
  def getPrimaryKeyFields() = {
    List(
      "startingBook",
      "id",
      )
  }

  ///////////////////////////////////////////////////////////
  // OSIS stuff
  ///////////////////////////////////////////////////////////

  // probably english?
  val defaultV11n : Versification = Versifications.instance().getVersification(Versifications.DEFAULT_V11N);
  val osisParser : OsisParser = new OsisParser();
  val bibleNames = BibleNames.instance()

  ///////////////////////////////////////////////////////////
  // CRUD
  ///////////////////////////////////////////////////////////

  ///////////////
  // CREATE
  ///////////////

  ///////////////
  // READ
  ///////////////

  ///////////////
  // UPDATE
  ///////////////

  ///////////////
  // DELETE
  ///////////////




  ///////////////////////////////////////////////////////////
  // GRAPH HELPERS
  ///////////////////////////////////////////////////////////

  def buildVertexTraversalFromPK (pk : List[Any]) : GraphTraversal[Vertex, Vertex] = {
    val g : GraphTraversalSource = CassandraDb.graph
    val startingBook : String = pk(0).asInstanceOf[String]
    val id : UUID = pk(1).asInstanceOf[UUID]
    val traversal = g.V().hasLabel("text")
      .has("starting_book", startingBook)
      .has("id", id)

    traversal
  }

  ///////////////////////////////////////////////////////////
  // STRING HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * For parsing when there might or might not be a hyphen (e.g., Gen.1.1-Exod OR Gen.1.1)
   * - TODO add versification options
   */ 
  def parseOsisRange (osis : String) : VerseRange = {
    println(s"about to parse $osis");
    osisParser.parseOsisRef(defaultV11n, osis)
  }
  /*
   * For parsing a single verse (e.g., Gen.1.1)
   * - does not work with e.g., Gen.1, needs a chapter
   * - TODO add versification options
   */ 
  def parseOsisID (osis : String) : JswordVerse = {
    osisParser.parseOsisID(defaultV11n, osis)
  }

  /*
   * Takes an osis string (which is potentially a range, but not necessarily) and returns the first reference for it
   * - note that parseOsisID cannot take e.g., Gen.4 and work. Needs a verse. 
   *  - Accordingly, we're getting verse for this osis range, and returning it
   * - specifically the first verse, because this is starting range, so first verse will give starting book, starting chapter, and starting verse
   *   (e.g., Gen.1 > Genesis 1:1, which is correct, that is the first verse of Genesis 1)
   *
   */ 
  def osisToStartingRef (osis : String) : JswordVerse = {
    val parsedRange = parseOsisRange(osis)
    val allVerses = parsedRange.toVerseArray()

    if (allVerses.length == 0) {
      throw new java.lang.IllegalArgumentException(s"No reference found for osis $osis")
    }

    // first verse will be starting ref
    allVerses(0)
  }

  /*
   *
   * - http://www.crosswire.org/jsword/cobertura/org.crosswire.jsword.examples.APIExamples.html
   */ 
  def osisToStartingBook (osis : String) : String= {
    // we want full, hopefully these are teh same as what theographic uses!
    //osisToStartingRef(osis).getBook // would be e.g., Gen
    // e.g., Gen.1.1 => Genesis 1:1 => (Genesis,1:1) => Genesis
    val bookInitials = osisToStartingRef(osis).getBook

    //Books.installed().getBook(bookInitials.toString);
    bibleNames.getLongName(bookInitials);
  }

  def osisToStartingChapter (osis : String) : Int = {
    osisToStartingRef(osis).getChapter 
  }

  def osisToStartingVerse (osis : String) : Int= {
    osisToStartingRef(osis).getChapter 
  }
  def osisToEndingRef (osis : String) : JswordVerse = {
    parseOsisRange(osis).toVerseArray().last 
  }
  def osisToEndingBook (osis : String) : String= {
    // we want full, hopefully these are teh same as what theographic uses!
    //osisToEndingRef(osis).getBook // would be e.g., Gen
    // e.g., Gen.1.1 => Genesis 1:1 => (Genesis,1:1) => Genesis
    val bookInitials = osisToEndingRef(osis).getBook
    bibleNames.getLongName(bookInitials);
  }
  def osisToEndingChapter (osis : String) : Int = {
    osisToEndingRef(osis).getChapter 
  }
  def osisToEndingVerse (osis : String) : Int = {
    osisToEndingRef(osis).getVerse
  }

}
