package models

import play.api.mvc._
// use to get the global session
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.datastax.dse.driver.api.core.graph.DseGraph.g._;

import com.ryanquey.intertextualitygraph.models.texts.{Text => TextClass}
import com.ryanquey.intertextualitygraph.modelhelpers.TextHelpers
import javax.inject._
import constants.DatasetMetadata._
// import models.Connection._
import java.time.Instant;
import java.util.{UUID, Collection, List};
// they don't like after 2.12
// import collection.JavaConverters._
import scala.jdk.CollectionConverters._
import com.google.common.collect.{ImmutableList, Lists}


import com.datastax.dse.driver.api.core.graph.DseGraph.g._;

import com.ryanquey.datautils.cassandraHelpers.CassandraDb

// way overkill, but just trying to find what works for method "out"
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;
import org.apache.tinkerpop.gremlin.structure.Column._;
import org.apache.tinkerpop.gremlin.structure.T._;
// 
import org.apache.tinkerpop.gremlin.process.traversal.P.{within}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__._;
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.io.graphson.{GraphSONMapper, GraphSONVersion}
import org.apache.tinkerpop.gremlin.process.traversal.Order.{asc}

// end over-importing tinkerpop classes

import gremlin.scala._

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.books.BookRecord
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 */
object TextAPIModel {
  //////////////////////////////////////////////////
  // graph traversal builders

  /*
   * return text traversal depending on how many args are passed in
   * Does not allow verse without chapter
   *
   */
  def getTextTraversal (dataSet : String, book : String, chapter : Option[Int], verse : Option[Int]) = {
    println("getting texts");
    var traversal = chapter match {
      case Some(c) if verse.isDefined => fetchTextByStartingVerse(book, c, verse.get)
      case Some(c) => fetchTextByStartingChapter(book, c)
      case None => fetchTextByStartingBook(book)
    }

    // filter by dataSet (which is currently just filtering by created_by)
    println(s"returning dataset: $dataSet")

    if (dataSet == "all") {
      println("returning  all!")

    } else if (dataSet == "treasury-of-scripture-knowledge") {
      // this is for now just tsk
      traversal = traversal.has("updated_by", "treasury-of-scripture-knowledge")

    } else if (dataSet == "user") {
      val userTypes = java.util.Arrays.asList("user-upload", "api-endpoint")
      traversal = traversal.has("updated_by", within(userTypes))
    }
    traversal
  }

  // NOTE returns traversal, doesn't actually hit the db yet until something is called on it
  def fetchTextByStartingVerse (book : String, chapter : Int, verse : Int) = {
    println(s"getting by starting chapter: $book $chapter:$verse");
    val g : GraphTraversalSource = CassandraDb.graph
    val texts : GraphTraversal[Vertex, Vertex] = g.V().hasLabel("text")
      .has("starting_book", book)
      .has("starting_chapter", chapter)
      .has("starting_verse",  verse)
      .order()
        .by("starting_chapter", asc)
        .by("starting_verse", asc)

    texts
  }

  /*
   * not using overloaded functions for now, since I think there might be distinctive enough behavior for these different queries down the road, so just make them separate
   */
  def fetchTextByStartingChapter (book : String, chapter : Int)  = {
    println(s"getting by starting chapter: $book $chapter");
    val g : GraphTraversalSource = CassandraDb.graph
    val texts : GraphTraversal[Vertex, Vertex] = g.V().hasLabel("text")
      .has("starting_book", book)
      .has("starting_chapter", chapter)
      .order()
        .by("starting_chapter", asc)
        // send default verse in case it doesn't have one, to prevent breaking
        // put refs with no verse in front 
        // https://groups.google.com/g/gremlin-users/c/FKbxWKG-YxA/m/MdUlPnRqCgAJ
        .by(coalesce(org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values("starting_verse"), constant(0)), asc)

    texts
  }

  def fetchTextByStartingBook (book : String)  = {
    println(s"getting by starting book: $book");
    val g : GraphTraversalSource = CassandraDb.graph

    val texts : GraphTraversal[Vertex, Vertex] = g.V().hasLabel("text")
      .has("starting_book", book)
      .order()
        .by("starting_chapter", asc)
        // send default verse in case it doesn't have one, to prevent breaking
        // put refs with no verse in front 
        // https://groups.google.com/g/gremlin-users/c/FKbxWKG-YxA/m/MdUlPnRqCgAJ
        .by(coalesce(org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values("starting_verse"), constant(0)), asc)

    texts
  }
  
  
  //////////////////////////////////////////////////
  // graph traversals

  /**
   * For alluding texts, return what texts they allude to
  * - mostly doing order by for now for the sake of downloading as csv, makes it (almost) sorted in the csv by the alluding text chapter and verse
   * 
   * 
   * http://www.doanduyhai.com/blog/?p=13301
   *
   * so far just making this for a single reference, but eventually will probably make another one for if there is a starting and ending reference
   * TODO might move the gremlin queries themselves to the etl-tools model-helpers
   * TODO I wonder if happening two separate gremlin traversals, one to get all vertices, and then another to do all edges, and returning both separately, would be more performant??? Not sure either way
   *
   * https://docs.datastax.com/en/developer/java-driver/4.9/manual/core/dse/graph/
   *
      // TODO NOTE I'm actually not sure if this finds the source text or alluding text...
   * @param hopsCount how many times to go out on the alludes_to edge
   *
   */

  
  def textsAlludeTo(alludingTexts : List[Vertex], hopsCount : Int)  = {
    val g : GraphTraversalSource = CassandraDb.graph
    // This returns an entry about the edge between each vertex
    val sourceTexts = g.V(alludingTexts)
      .order()
        .by("starting_chapter", asc)
        // send default verse in case it doesn't have one, to prevent breaking
        // put refs with no verse in front 
        // https://groups.google.com/g/gremlin-users/c/FKbxWKG-YxA/m/MdUlPnRqCgAJ
        .by(coalesce(org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values("starting_verse"), constant(0)), asc)
      .repeat(outE().hasLabel("alludes_to").inV().hasLabel("text")).times(hopsCount)

    sourceTexts
  }

  /*
  * for sourceTexts, return texts that allude to them
  * - mostly doing order by for now for the sake of downloading as csv, makes it (almost) sorted in the csv by the source text chapter and verse
  */
  def textsAlludedToBy(sourceTexts : List[Vertex], hopsCount : Int)  = {
    val g : GraphTraversalSource = CassandraDb.graph
    // This returns an entry about the edge between each vertex
    val alludingTexts = g.V(sourceTexts).order()
        .by("starting_chapter", asc)
        .by(coalesce(org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values("starting_verse"), constant(0)), asc).                
      repeat(inE().hasLabel("alludes_to").outV().hasLabel("text")).times(hopsCount)

    alludingTexts
  }


  //////////////////////////////////////////////////
  // output helpers

  // takes Java list of graph traversals or other graph class instances and converts to json
  val json_mapper = GraphSONMapper.
                build().
                version(GraphSONVersion.V1_0).
                create().
                createMapper()


  /*
   * NOTE make sure the traversal has a "out" called on it
   * @param valuesToReturn sequence of properties on the vertices to return. Pass in empty Seq() to return all (calls valueMap())
   */
  def findPathsForTraversal (traversal : GraphTraversal[Vertex, Vertex], valuesToReturn : Seq[String]) = {
    // get values from source and target vertices
    // destructure the arg to valueMap, will be like e.g., valueMap("id", "split_passages", "starting_book")
    // https://stackoverflow.com/a/1832288/6952495
    traversal.path().by(valueMap(valuesToReturn:_*))
  }

  ///////////////////////////////
  // temp test helpers
  def getEmptyTraversal() = {
    getTextTraversal("non-dataset", "not-a-book", Some(99), Some(99))
  }

}
