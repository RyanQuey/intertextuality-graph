package com.ryanquey.intertextualitygraph.graphmodels

import java.util.UUID;
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

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

import com.ryanquey.intertextualitygraph.graphmodels.GraphModel
import com.ryanquey.intertextualitygraph.graphmodels.GraphReferenceVertex
import com.ryanquey.intertextualitygraph.graphmodels.BookVertex
import com.ryanquey.intertextualitygraph.graphmodels.BookVertex._

import com.ryanquey.datautils.models.{Model, Record}

/*
 * - NOTE make sure to keep fields in sync with com.ryanquey.intertextualitygraph.models.chapters.ChapterBase
 */
case class TextVertex(
  id : UUID,  
  startingBook : String,  
  yearWritten : Integer,  // INT 
  author : String,  // TEXT
  canonical : Boolean,  // BOOLEAN 
  canonicalText : String,  // TEXT
  splitPassages : List[String],  // LIST<TEXT>
  startingChapter : Integer,  
  startingVerse : Integer,  
  endingBook : String,  
  endingChapter : Integer, 
  endingVerse : Integer,  
  testament : String,  // TEXT 
  greekTranslation : String,  // TEXT 
  englishTranslation : String,  // TEXT 
  comments : String,  // TEXT
  createdBy : String,
  updatedBy : String, 
  updatedAt : Instant // TIMESTAMP, 
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
      javabean.getId(),
      javabean.getStartingBook(),
      javabean.getYearWritten(),
      javabean.getAuthor(),
      javabean.getCanonical(),
      javabean.getCanonicalText(),
      javabean.getSplitPassages().asScala.toList,
      javabean.getStartingChapter(),
      javabean.getStartingVerse(),
      javabean.getEndingBook(),
      javabean.getEndingChapter(),
      javabean.getEndingVerse(),
      javabean.getTestament(),
      javabean.getGreekTranslation(),
      javabean.getEnglishTranslation(),
      javabean.getComments(),
      javabean.getCreatedBy(),
      javabean.getUpdatedBy(),
      javabean.getUpdatedAt()
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
