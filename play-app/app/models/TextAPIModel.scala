package models

import scala.annotation.tailrec
// they don't like after 2.12
// import collection.JavaConverters._
import scala.jdk.CollectionConverters._
import javax.inject._
import java.time.Instant;
import java.util.{UUID, Collection, List};

import com.google.common.collect.{ImmutableList, Lists}
import com.fasterxml.jackson.databind.ObjectMapper;

import play.api.mvc._
import gremlin.scala._

import com.datastax.dse.driver.api.core.graph.DseGraph.g._;

// way overkill, but just trying to find what works for method "out"
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.{Path}
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Order.{asc}
import org.apache.tinkerpop.gremlin.process.traversal.P.{within}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__._;
import org.apache.tinkerpop.gremlin.structure.Column._;
import org.apache.tinkerpop.gremlin.structure.T._;
// 
import org.apache.tinkerpop.gremlin.structure.{Vertex}
import org.apache.tinkerpop.gremlin.structure.io.graphson.{GraphSONMapper, GraphSONVersion}

// end over-importing tinkerpop classes


// use to get the global session
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.datastax.dse.driver.api.core.graph.DseGraph.g._;
import com.ryanquey.intertextualitygraph.models.texts.{Text => TextClass}
import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.books.BookRecord
import com.ryanquey.intertextualitygraph.modelhelpers.TextHelpers

import models.traversalbuilder.{FilterByRefRanges, FilterByStartingRef, TraversalBuilder}
import models.traversalbuilder.hopparams.{HopParamsSets}


// import models.Connection._
import constants.DatasetMetadata._

/**
 * I'm not sure how this particular class functions relative to classes defined in traversalbuilder...TODO
 */
object TextAPIModel {
  //////////////////////////////////////////////////
  // graph traversal builders


  /*
   * return text traversal depending on how many args are passed in
   * - Does not allow verse without chapter
   * - only filters by starting ref for now
   *
   */
  def getTextTraversal (dataSet : String, book : String, chapter : Option[Int], verse : Option[Int]) : GraphTraversal[Vertex, Vertex] = {
    println("getting texts");
    // TODO make this immutable!!
    val g : GraphTraversalSource = CassandraDb.graph
    val initialTraversal : GraphTraversal[Vertex, Vertex] = g.V().hasLabel("text")

    val traversalWithRefFilters = FilterByStartingRef.addFilterTextByRefSteps(initialTraversal, book, chapter, verse)
    val traversalWithDatasetFilters = TraversalBuilder.addTextFilterByDatasetSteps(traversalWithRefFilters, dataSet)

    traversalWithDatasetFilters
  }


  ///////////////////////////////////////////////
  // builder helpers
  ///////////////////////////////////////////////


  
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
  def textsAlludedToBy(sourceTexts : List[Vertex], hopsCount : Int) : GraphTraversal[Vertex, Vertex] = {
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



  ///////////////////////////////
  // temp test helpers
  def getEmptyTraversal() = {
    getTextTraversal("non-dataset", "not-a-book", Some(99), Some(99))
  }

}
