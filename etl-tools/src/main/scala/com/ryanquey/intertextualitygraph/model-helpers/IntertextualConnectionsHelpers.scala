package com.ryanquey.intertextualitygraph.modelhelpers

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import com.ryanquey.intertextualitygraph.graphmodels.IntertextualConnectionEdge

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

object IntertextualConnectionsHelpers {
  /* 
   * take two texts and fields for the connection and 1) make connection and 2) make those texts if not exists
   * only allowing bare minimum fields for now
  * probably will make overloaded method for handling more params
  * TODO test that this works now that I added extra args
  */
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
    sourceVersion : Option[String] = None
  ) = {
    // mostly these will be built mostly with gremlin, so not bothering to do the whole model thing for now
    // Make case classes for this at most

    val connection = IntertextualConnectionEdge(
      srcText.getStartingBook(), 
      srcText.getId(), 
      alludingText.getStartingBook(), 
      alludingText.getId(), 
      connectionType, 
      confidenceLevel, 
      Instant.now(), 
      volumeLevel,
      userId, 
      bealeCategories, 
      connectionSignificance, 
      comments, 
      sourceVersion)

    println("connecting...")
    persistConnection(connection)

    // if works, return IntertextualConnectionEdge instance
    connection
  }

  def persistConnection (ic : IntertextualConnectionEdge) = {
    /* 
     * NOTE this is persisting to Cassandra
     * Not making intertextualconnection dao for now, or model, just do it using query builder until a need arises
    * Want to try using case classes and use scala more for this, and especially, using graph instead of C*
    *
    * note that query builder makes immutable objects, so adding any value creates new obj
    * TODO this creates a lot of tombstones, since will have a lot of null values
    *   - actually, this might have been fixed already
    * NOTE tsk importer is not idempotent, will create new connectins since no ids are in tsk csv
    */


   // first set values we definitely have and have to have
   // in this case using insert as an upsert, so don't have to set primary key anywhere, but if I do set it will update
    var query = insertInto("intertextual_connections")
      .value("confidence_level", literal(ic.confidenceLevel))
      .value("source_text_id", literal(ic.sourceTextId))
      .value("source_text_starting_book", literal(ic.sourceTextStartingBook))
      .value("alluding_text_id", literal(ic.alludingTextId))
      .value("alluding_text_starting_book", literal(ic.alludingTextStartingBook))
      .value("connection_type", literal(ic.connectionType)) 
      .value("updated_at", literal(ic.updatedAt)) 

      // small helper to set more fields on this query builder that we're going to send to C* db
      // TODO move these out of this function
    def setField (col : String, field : Option[Any]) = field match {
      // convert to java list
      // call .get to convert from option to something
      case Some(_) => { query = query.value(col, literal(field.get))}
      case None => 
    }

    // I'm not sure how to do this in setField and only use one case match thing...so making a separate one for fields
    // issue is related to this warning that I see when building: non-variable type argument List[String] in type pattern Some[List[String]] is unchecked since it is eliminated by erasure
    def setListField (col : String, field : Option[List[Any]]) = field match {
      //case list : Some[List[String]] => { query = query.value(col, literal(list.get.asJava))}
      case Some(_) => { query = query.value(col, literal(field.get.asJava))}
      case None => 
    }

    // how important this connection is in teh argument of original author. Hays' criteria "volume"
    setField("volume_level", ic.volumeLevel) 
    setField("connection_significance", ic.connectionSignificance) 
    setField("user_id", ic.userId)
    setListField("beale_categories", ic.bealeCategories)
    // TODO add description once added to the db schema
    setField("comments", ic.comments)
    setField("source_version", ic.sourceVersion)
    setField("source_language", ic.sourceLanguage)

    // make teh wher clause by id if ID is passed in
    ic.id match {
      case Some(_) => query = query.value("id", literal(ic.id))
      case None =>
    }

    println(s"Executing string to create connection: $query")
    val result = CassandraDb.execute(s"$query ;");
    println(s"result from creating connection: $result");
  }
}
