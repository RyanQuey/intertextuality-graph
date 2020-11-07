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

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

import com.ryanquey.intertextualitygraph.graphmodels.BookVertex
import com.ryanquey.intertextualitygraph.graphmodels.BookVertex._

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
)

object TextVertex {
  /*
   * overloading the apply method so we can instantiate the case class using the corresponding Java model class
   * - https://stackoverflow.com/a/2401153/6952495
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

  ///////////////////////////////////////////////////////////
  // ASSOCIATION CRUD
  ///////////////////////////////////////////////////////////

  ///////////////
  // ASSOCIATION CREATE
  ///////////////

  /*
   * take a given text vertex and create references to all books, chapters, and verses
   *
   */ 
  def createReferenceVertices (text : TextVertex) = {

  }

  ///////////////
  // ASSOCIATION UPDATE
  ///////////////

  /*
   * take a given text vertex and update references to all books, chapters, and verses
   *
   */ 
  def updateReferenceVertices (text : TextVertex) = {
    // 1) fetch all currently existing reference vertices (book, ch, v)

    // 2) delete all old vertices

    // 3) create vertices that don't exist yet
    // for each book that does not currently exist, create a new edge
    
    // for each chapter that does not currently exist, create a new edge
    
    // for each verse that does not currently exist, create a new edge
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
    val g : GraphTraversalSource = CassandraDb.graph
    val textVertex = g.V().hasLabel("text")
      .has("starting_book", text.startingBook)
      .has("id", text.id)

    textVertex
  }

  ///////////////////////////////////////////////////////////
  // PRIVATE METHODS
  ///////////////////////////////////////////////////////////


  /*
   * creates edge record between text and book
   *
   */
  private def createEdgeToBook (textVertex : TextVertex, bookName : String) = {
    val textVertexTraversal = TextVertex.buildVertexTraversal(textVertex)
    val bookVertexTraversal = BookVertex.buildVertexTraversalFromPK(bookName)

    textVertexTraversal.addE("from_book").from(bookVertexTraversal)
  }
}
