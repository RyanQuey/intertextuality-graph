package com.ryanquey.intertextualitygraph.modelhelpers

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import com.ryanquey.intertextualitygraph.graphmodels.IntertextualConnectionEdge
import com.ryanquey.intertextualitygraph.graphmodels.TextVertex

import java.time.Instant;
import java.util.UUID;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
// dse recommends using this for fluent API over strings, since strings can be unwieldy
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph
import com.datastax.oss.driver.api.querybuilder.QueryBuilder._;
// import com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal

import scala.jdk.CollectionConverters._

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

/*
 * (DEPRECATED)
 * for helpers that interact with the java model Class; 
 * - for anything that interacts with the scala case class use companion object
 * - And, in general, don't add to this much, so it's easy to migrate over from the java models in the future. Instead of writing methods here, convert the java class > case class, and then use method from companion object
 */

object IntertextualConnectionsHelpers {
  // just a wrapper method for IntertextualConnectionEdge object companion, which takes java classes instead of TextVertex case class instances
  def connectTexts (
    srcText : Text, 
    alludingText : Text, 
    connectionType : String, 
    confidenceLevel : Float, 
    volumeLevel : Option[Float] = None, 
    userId : Option[UUID] = None, 
    bealeCategories : Option[List[String]] = None, 
    connectionSignificance : Option[String] = None, 
    comments : Option[String] = None, 
    description : Option[String] = None, 
    sourceVersion : Option[String] = None
  ) = {
    val srcTextVertex = TextVertex(srcText)
    val alludingTextVertex = TextVertex(alludingText)

    IntertextualConnectionEdge.connectTexts(
      srcTextVertex, 
      alludingTextVertex, 
      connectionType,
      confidenceLevel,
      volumeLevel,
      userId,
      bealeCategories,
      connectionSignificance,
      comments,
      description,
      sourceVersion,
      )
  }
}
