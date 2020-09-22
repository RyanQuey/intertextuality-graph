package com.ryanquey.intertextualitygraph.modelhelpers

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

case class IntertextualConnection(
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
  // not just different languages. E.g., can be Theodotian vs other LXX versions
  sourceVersion : Option[String] = None,
  sourceLanguage : Option[String] = None,
  )


object IntertextualConnectionsHelpers {
  // only allowing bare minimum fields for now
  // probably will make overloaded method for handling more params
  def connectTexts (srcText : Text, alludingText : Text, connectionType : String, confidenceLevel : Float) = {
    // mostly these will be built mostly with gremlin, so not bothering to do the whole model thing for now
    // Make case classes for this at most

    val connection = IntertextualConnection(srcText.getStartingBook(), srcText.getId(), alludingText.getStartingBook(), alludingText.getId(), connectionType, confidenceLevel, Instant.now())

    persistConnection(connection)
  }

  def persistConnection (ic : IntertextualConnection) = {
    // Not making intertextualconnection dao for now, or model, just do it using query builder until a need arises
    // Want to try using case classes and use scala more for this, and especially, using graph instead of C*
    //
    // note that query builder makes immutable objects, so adding any value creates new obj
    // TODO this creates a lot of tombstones, since will have a lot of null values
    var query = insertInto("intertextual_connections")
      .value("confidence_level", literal(ic.confidenceLevel))

  // how important this connection is in teh argument of original author. Hays' criteria "volume"
      .value("source_text_id", literal(ic.sourceTextId))
      .value("source_text_starting_book", literal(ic.sourceTextStartingBook))
      .value("alluding_text_id", literal(ic.alludingTextId))
      .value("alluding_text_starting_book", literal(ic.alludingTextStartingBook))
      .value("connection_type", literal(ic.connectionType)) 
      .value("updated_at", literal(ic.updatedAt)) 

    def setField (col : String, field : Option[Any]) = field match {
      case Some(_) => { query = query.value(col, literal(field.get))}
      case None => 
    }

    setField("connection_significance", ic.connectionSignificance) 
      println("user_id", ic.userId)
    setField("user_id", ic.userId)
      println("beale_categories", ic.bealeCategories)
    setField("beale_categories", ic.bealeCategories)
    setField("comments", ic.comments)
    setField("source_version", ic.sourceVersion)
    setField("source_language", ic.sourceLanguage)

    CassandraDb.execute(query.toString);
  }
}
