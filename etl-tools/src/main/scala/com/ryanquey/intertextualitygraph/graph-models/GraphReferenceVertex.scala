package com.ryanquey.intertextualitygraph.graphmodels

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import scala.collection.JavaConverters._ 
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.ryanquey.datautils.models.{Model, Record}

import java.util.UUID;
import java.time.Instant;
import com.datastax.oss.driver.api.core.cql._;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex

/*
 * Represents GraphModel instances, specifically for vertices representing text references (book, chapter, verse, text)
 * - subclass of GraphModel, 
 * - <: is upper bounds, requiring that A is an instance of GraphReferenceVertex or child class
 */ 
trait GraphReferenceVertex[A <: GraphReferenceVertex[A]] extends GraphVertex[A] {
  def yearWritten : Option[Integer]  // INT 
  def author : Option[String]  // TEXT
  def canonical : Boolean  // BOOLEAN 
  // always should have. If not Old Testament or New Testament, can be Pseudapigrapha, Apocrypha etc.
  def testament : String  // TEXT 
  def comments : Option[String]  // TEXT
  def updatedAt : Instant // TIMESTAMP, 
}

/*
 * A will be the case class
 */
trait GraphReferenceVertexCompanion[A <: GraphReferenceVertex[A]] extends GraphVertexCompanion[A] {
  /* all reference vertex models need a quick way to convert from the javabean class to the scala case class
   * - ideally we could just write a generic function here to cover all GraphReferenceVertexCompanion implementations, but I'm having trouble figuring out, so just do it later TODO
   * - we want this to output as a TextVertex (not generic GraphReferenceVertex) so it is easy to use, so use A rather than GraphReferenceVertex
   */
  def convertJavabeanModelInstances(javabeanModelInstances : Iterable[Model]) : Iterable[A]

}
