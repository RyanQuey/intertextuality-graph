package com.ryanquey.intertextualitygraph.graphmodels

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text

import java.time.Instant;
import java.util.UUID;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
// dse recommends using this for fluent API over strings, since strings can be unwieldy
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph
import com.datastax.oss.driver.api.querybuilder.QueryBuilder._;
// import com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

/*
 * - NOTE make sure to keep fields in sync with com.ryanquey.intertextualitygraph.models.chapters.IntertextualConnectionBase (if we ever make one)
 */
case class IntertextualConnectionEdge(
  sourceTextStartingBook : String,
  sourceTextId : UUID,
  alludingTextStartingBook : String,
  alludingTextId : UUID,
  // Required. If you don't have a type, make unknown. Or don't make it!
  // TODO add validation. one of: 
  // "from-generic-list" // ie, there's a list api I grabbed it from, whic does not label connection type
  // "unknown" 
  connectionType : String, 

  // we always want this. 0-100.0 inclusive
  confidenceLevel : Float,
  updatedAt : Instant, 

  // how important this connection is in teh argument of original author. Hays' criteria "volume". 0-100.0 inclusive
  volumeLevel : Option[Float] = None,
  // who created this connection
  userId : Option[UUID] = None,
  bealeCategories : Option[List[String]] = None,
  // e.g., allusion, quote, echo, etc (?). Use list of constants to validate this field
  connectionSignificance : Option[String] = None, 
  comments : Option[String] = None,
            // TODO oops! forgot to put this in the db!
  description : Option[String] = None,
  // not just different languages. E.g., can be Theodotian vs other LXX versions
  sourceVersion : Option[String] = None,
  sourceLanguage : Option[String] = None,
  id : Option[UUID] = None,
  )


object IntertextualConnectionEdge {
  // only allowing bare minimum fields for now
  // probably will make overloaded method for handling more params
  // TODO implement for graph backend, that doesn't have C* support already
  /* TODO move it form helpers to here probably
  def connectTexts (srcText : Text, alludingText : Text, connectionType : String, confidenceLevel : Float) = {

    val connection = IntertextualConnectionEdge(srcText.getStartingBook(), srcText.getId(), alludingText.getStartingBook(), alludingText.getId(), connectionType, confidenceLevel, Instant.now())

    println("connecting...")
    persistConnection(connection)

    // if works, return IntertextualConnectionEdge instance
    connection
  }
  */

  def persistConnection (ic : IntertextualConnectionEdge) = {
  }
}
