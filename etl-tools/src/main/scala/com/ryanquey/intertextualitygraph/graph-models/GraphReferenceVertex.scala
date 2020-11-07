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

trait GraphReferenceVertex extends GraphModel {
  def yearWritten : Integer  // INT 
  def author : String  // TEXT
  def canonical : Boolean  // BOOLEAN 
  def testament : String  // TEXT 
  def comments : String  // TEXT
  def updatedAt : Instant // TIMESTAMP, 
}

/*
 * A will be the case class
 */
trait GraphReferenceVertexCompanion[A] extends GraphVertexCompanion {
  /* all reference vertex models need a quick way to convert from the javabean class to the scala case class
   * - ideally we could just write a generic function here to cover all GraphReferenceVertexCompanion implementations, but I'm having trouble figuring out, so just do it later TODO
   * - we want this to output as a TextVertex (not generic GraphReferenceVertex) so it is easy to use, so use A rather than GraphReferenceVertex
   */
  def convertJavabeanModelInstances(javabeanModelInstances : Iterable[Model]) : Iterable[A]
}
