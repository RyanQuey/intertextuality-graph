package com.ryanquey.intertextualitygraph.graphmodels

import java.util.{UUID, Map}
import java.time.Instant;

//import scala.collection.JavaConverters._ 
//import scala.collection.JavaConverters._ 
import scala.jdk.CollectionConverters._

import com.datastax.oss.driver.api.core.cql._;
import scala.collection.immutable.List

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold

import gremlin.scala._

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.ryanquey.datautils.helpers.StringHelpers._;

import com.ryanquey.intertextualitygraph.graphmodels.GraphModel
import com.ryanquey.intertextualitygraph.graphmodels.GraphReferenceVertex
import com.ryanquey.intertextualitygraph.graphmodels.BookVertex
import com.ryanquey.intertextualitygraph.graphmodels.BookVertex._

import com.ryanquey.intertextualitygraph.helpers.shapeless.{CaseClassFromMap}
import com.ryanquey.datautils.models.{Model, Record}

// TODO these are just temp to test
import com.datastax.dse.driver.api.core.graph._;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;

import shapeless.{LabelledGeneric, Generic}
import shapeless.record.Record
import shapeless.syntax.std.maps._



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
  startingVerse : Integer,  
  endingBook : String,  
  endingChapter : Integer, 
  endingVerse : Integer,  
  testament : String,  // TEXT 
  canonical : Boolean,  // BOOLEAN 
  updatedAt : Instant, // TIMESTAMP, 
  createdBy : String,
  updatedBy : String, 

  yearWritten : Option[Integer],  // INT 
  author : Option[String],  // TEXT
  canonicalText : Option[String],  // TEXT
  greekTranslation : Option[String],  // TEXT 
  englishTranslation : Option[String],  // TEXT 
  comments : Option[String],  // TEXT

) extends GraphReferenceVertex[TextVertex] {
    def companionObject = TextVertex
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
      startingVerse = javabean.getStartingVerse,
      endingBook = javabean.getEndingBook,
      endingChapter = javabean.getEndingChapter,
      endingVerse = javabean.getEndingVerse,
      testament = javabean.getTestament,

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

  def valueMapToCaseClass(valueMap: java.util.Map[String, Any]) : TextVertex = {
     val typecastedMap = valueMap.asScala.toMap

     val mapWithFixedKeys = typecastedMap.map { case (key, value) => snakeToCamel(key) -> value }

     CaseClassFromMap[TextVertex](mapWithFixedKeys)
  }

  ///////////////////////////////////////////////////////////
  // CRUD
  ///////////////////////////////////////////////////////////

  ///////////////
  // CREATE
  ///////////////

  def findAllTexts(map : Map[Any, Any]) = {  
    //implicit val graph :  = g.asScala
    //graph.V.hasLabel[TextVertex]
    // val vertexClassGen = Generic[TextVertex]
    // val repr = FromMap[TextVertex]

    // vertexClassGen.from(map)
  }

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
  def createReferenceVertices (text : TextVertex) : Unit = {
    // fetch metadata (as represented by case class instances) for each book/ch/v overlapping with this text
    val (books, chapters, verses) = getReferenceMetadata(text)

    books.foreach((b) => createEdgeToBook(text, b.name)) 
    chapters.foreach((c) => createEdgeToChapter(text, c.book, c.number)) 
    verses.foreach((v) => createEdgeToVerse(text, v.book, v.chapter, v.number)) 
  }

  ///////////////
  // ASSOCIATION READ
  ///////////////

  /*
   * fetch all books that this text includes
   *
   */ 
  def fetchBooks (text : TextVertex) = {
    val textTraversal = TextVertex.buildVertexTraversal(text)
    val id = textTraversal.values("id").by(unfold()).next()
    
  }


  ///////////////
  // ASSOCIATION UPDATE
  ///////////////

  /*
   * take a given text vertex and update references to all books, chapters, and verses
   *
   */ 
  def updateReferenceVertices (text : TextVertex) : Unit = {
    // 1) fetch all currently existing reference vertices (book, ch, v)

    // 2) delete all old vertices

    // 3) create vertices that don't exist yet
    // for each book that does not currently exist, create a new edge
    
    // for each chapter that does not currently exist, create a new edge
    
    // for each verse that does not currently exist, create a new edge
  }

  ///////////////////////////////////////////////////////////
  // METADATA HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * for a given textVertex, return java model instances of all book, ch, and verses that this text intersects with
   * - could create a helper to get graphModels instead, but not bothering with that for now
   */ 
  def getReferenceMetadata (text : TextVertex) = {
    // extract out all books for this text
    val startingBook = BookVertex.getBookByName(text.startingBook)
    val startingChapter = ChapterVertex.getChapterByNum(startingBook, text.startingChapter)
    val startingVerse = VerseVertex.getVerseByNum(startingBook, startingChapter, text.startingVerse)

    val endingBook = BookVertex.getBookByName(text.endingBook)
    val endingChapter = ChapterVertex.getChapterByNum(endingBook, text.endingChapter)
    val endingVerse = VerseVertex.getVerseByNum(endingBook, endingChapter, text.endingVerse)

    val books = BookVertex.getBooksBetween(startingBook, endingBook)
    
    // extract out all chapters for this text
    val chapters = ChapterVertex.getChaptersBetween(startingChapter, endingChapter)
    
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
  def buildVertexTraversal (text : TextVertex) : GraphTraversal[Vertex, Vertex] = {
    buildVertexTraversalFromPK(List(text.startingBook, text.id))
  }

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
  // PRIVATE METHODS
  ///////////////////////////////////////////////////////////


  /*
   * creates edge record between text and book
   * - assumes that book already exists (which always should, if we imported the seed that already)
   */
  private def createEdgeToBook (textVertex : TextVertex, bookName : String) = {
    val textVertexTraversal = TextVertex.buildVertexTraversal(textVertex)
    val bookVertexTraversal = BookVertex.buildVertexTraversalFromPK(List(bookName))

    textVertexTraversal.addE("from_book").from(bookVertexTraversal)
  }

  /*
   * creates edge record between text and chapter
   * - assumes that book and chapter already exists (which always should, if we imported the seed that already)
   *
   */
  private def createEdgeToChapter (textVertex : TextVertex, bookName : String, chapterNumber : Int) = {
    val textVertexTraversal = TextVertex.buildVertexTraversal(textVertex)
    val chapterVertexTraversal = ChapterVertex.buildVertexTraversalFromPK(List(bookName, chapterNumber))

    textVertexTraversal.addE("from_chapter").from(chapterVertexTraversal)
  }
  /*
   * creates edge record between text and verse
   * - assumes that book and chapter and verse already exists (which always should, if we imported the seed that already)
   *
   */
  private def createEdgeToVerse (text : TextVertex, bookName : String, chapterNumber : Int, verseNumber : Int) = {
    val textTraversal = TextVertex.buildVertexTraversal(text)
    val verseTraversal = VerseVertex.buildVertexTraversalFromPK(List(bookName, chapterNumber, verseNumber))

    textTraversal.addE("from_verse").from(verseTraversal)
  }
}
