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

import scala.jdk.CollectionConverters._

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
  description : Option[String] = None,
  // not just different languages. E.g., can be Theodotian vs other LXX versions
  sourceVersion : Option[String] = None,
  sourceLanguage : Option[String] = None,
  id : Option[UUID] = None,
  )


object IntertextualConnectionEdge {
  /* 
   * take two texts and fields for the connection and 1) make connection and 2) make those texts if not exists
   * only allowing bare minimum fields for now
  * probably will make overloaded method for handling more params
  * TODO test that this works now that I added extra args
  */
  def connectTexts (
    srcText : TextVertex, 
    alludingText : TextVertex, 
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
    val connection = IntertextualConnectionEdge(
      srcText.startingBook, 
      srcText.id, 
      alludingText.startingBook, 
      alludingText.id, 
      connectionType, 
      confidenceLevel, 
      Instant.now(), 
      volumeLevel,
      userId, 
      bealeCategories, 
      connectionSignificance, 
      comments, 
      description,
      sourceVersion)

    println("connecting...")
    persistConnection(connection)

    // if works, return IntertextualConnectionEdge instance
    connection
  }

  def persistConnection (ic : IntertextualConnectionEdge) = {
    /* 
     * upserts this intertextual connection into Cassandra
     * - NOTE this is persisting to Cassandra
    *
    * - note that query builder makes immutable objects, so adding any value creates new obj
    * - TODO this creates a lot of tombstones, since will have a lot of null values
    *   * actually, this might have been fixed already
    * - TODO heading into the future, we want to do inserts using gremlin. This is probably fine for now though, no reason to change
    * - NOTE tsk importer is not idempotent, will create new connectins since no ids are in tsk csv
    *   TODO can make this a case class method
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
    // TODO extract this out into a helper, so can we use it for other places
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
    setField("description", ic.description)
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
