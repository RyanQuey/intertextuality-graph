package models.traversalbuilder.hopparams

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

import com.ryanquey.intertextualitygraph.graphmodels.TextVertex.{
  getPrimaryKeyFields, 
  osisToStartingBook,
  osisToStartingChapter,
  osisToStartingVerse,
}


import models.traversalbuilder.{FilterByRefRanges, TraversalBuilder}

// import models.Connection._
import constants.DatasetMetadata._




case class HopParamsSet (
  // can be a single text, or a range, even range of books
  referenceOsis : String,

  /*
  // for now only allowing alludes-to or alluded-to-by. Maybe later also allowing "both"
  allusionDirection : String,

  
  // if true, that means that this really is not a hop, and is just the params for the starting point from where we will do traversals
  // - does not allow expanding to ch/book/verses, since that should be specified within the referenceOsis already
  // - probably more differences as well ...?
  isInitialSet : Boolean = false,

    println("got path");
  // if true, means that there will be no hops after this
  // - 
  isFinalHop : Boolean = false,
  

  // implement later, when we start allowing extra biblical texts
  // canonical : Option[Boolean],  // BOOLEAN 
  // default to "all"
  dataSet : String = "all",
  // TODO allow filtering by author. eg, all the writings of Paul, or David. Could also just implement this on the frontend though, have js figure out how to request those books/those chapters. Also allow "Unknown", or multiple ( probably if multiple, will mostly be one known author, and also Unknown) or something like that
  // authors : Option[Set[String]],  // TEXT

  // TODO
  // - not sure if I want to add this or not, but the idea would be tracing all connections but only when a medial/final hop has a certain theme
  // hasThemes : Option[Set[String]],  // TEXT

  
  // result expansions
  // - only for when this is a middle hop. The initial steps should specify the texts they want directly; and there is no reason to expand if it is the final hop anyways
  // - use case is for when for example, maybe someone wasn't very specific ( or maybe was too specific!) when creating the data set, which could result in not including enough connections. E.g., if I'm searching for texts alluded to by Hebrews 2:6, maybe it gets text for Psalm 8:2-3 for whatever reason, but accidentally in the dataset, Our connection to Gen 1 from Psalm 8 is only on from ps 8:6. If this were the case, our traversal would never get results for Genesis 1. If they expand to the chapter, or even just by 3 verses. It would solve that problem.
  // Basically the idea is to allow "fuzzy" searches to get more results.

  // - NOTE might not want to allow this
  // - definitely not allowing expanding my multiple books
  expandToBook : Boolean = false,

  // expand results to include all texts for the chapter(s) that this text overlaps with.
  // - If text is already full chapter, can probably just do nothing
  expandToChapter : Boolean = false,
  // expand results to all texts in that chapter before going to next hopt.
  // - 1 means expand to one chapter in both directions, and so on
  // - this param is only is taken into account if expandToChapter is true
  expandByNumChapters : Int = 0,
  // allow users to expand hits for this hop by certain number of verses (like accordance's little slider)
  expandByNumVerses : Int = 0
  */
) {

  // make wrappers for this, since we'll change logic later, to include expanding to book/chapter or using expandByNumVerses, and also handling ranges for the osis (e.g., gen.1.1-gen.1.3). 
  // for now though, keep it simple to get something working, so continue to just use starting book/ch/v only, but return as iterable to prepare for the future

  def getBooks() : Set[String] = {
    println(s"getting starting book for $referenceOsis");
    val book = osisToStartingBook(referenceOsis)

    // want it to be a set in the future, so just set it as a set
    Set(book)
  }

	// TODO how things are currently implemented, this will actually return a chapter and filter by chapter when none is specified
  def getChapters() : Set[Int] = {
    Set(osisToStartingChapter(referenceOsis))
  }
  def getVerses() : Set[Int] = {Set(osisToStartingVerse(referenceOsis))}
} 

object HopParamsSet {

  /*
   * take a single hopParamsSet and apply the steps associated with these parameters onto the graph traversal
   *  - TODO can move to instance method
   */ 
	def addStepsForHop (traversal : GraphTraversal[Vertex, Vertex], hopParamsSet : HopParamsSet) : GraphTraversal[Vertex, Vertex] = {
	  // TODO add osis parsing to get ref
	  val referenceOsis : String = hopParamsSet.referenceOsis
	  //val allusionDirection : String = hopParamsSet.allusionDirection
	  //val dataSet : String = hopParamsSet.dataSet.getOrElse("all")
	  val dataSet = "all"

	  val book = hopParamsSet.getBooks
	  val chapter = hopParamsSet.getChapters
	  val verse = hopParamsSet.getVerses

    // fields that have primary keys for text, which will be needed to do graph traversal
    val textPks = getPrimaryKeyFields()

    // TODO make helper to get range of verses or chapters...or books. 

    val traversalWithRefFilters = FilterByRefRanges.addTextFilterByRefSteps(traversal.hasLabel("text"), "Genesis", Some(1), Some(Set(1)))
    val traversalWithDatasetFilters = TraversalBuilder.addTextFilterByDatasetSteps(traversalWithRefFilters, dataSet)

    traversalWithDatasetFilters
  }
}

