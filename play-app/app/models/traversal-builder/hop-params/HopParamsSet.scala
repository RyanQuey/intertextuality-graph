package models.traversalbuilder.hopparams

import scala.annotation.tailrec
// they don't like after 2.12
// import collection.JavaConverters._
import scala.jdk.CollectionConverters._
import scala.collection.mutable
import javax.inject._
import java.time.Instant;
import java.util.{UUID};

import com.google.common.collect.{ImmutableList, Lists}
import com.fasterxml.jackson.databind.ObjectMapper;

import play.api.mvc._

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
// I think you can do only one of the next two
import gremlin.scala._
//import com.datastax.dse.driver.api.core.graph.DseGraph.g._;


// use to get the global session
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.ryanquey.intertextualitygraph.models.texts.{Text => TextClass}
import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.books.BookRecord
import com.ryanquey.intertextualitygraph.modelhelpers.TextHelpers
import com.ryanquey.intertextualitygraph.graphmodels.{BookVertex, ChapterVertex, VerseVertex}

import com.ryanquey.intertextualitygraph.utils.JswordUtil.{
  osisToStartingBookName,
  osisToStartingVerseReference,
  osisToStartingBookReference,
  osisToStartingChapterReference,
  parseOsisRanges
}

import models.traversalbuilder.{FilterByRefRanges, TraversalBuilder}

import com.ryanquey.intertextualitygraph.reference.{ReferenceRange, BookReference, ChapterReference, VerseReference,
  ChapterRangeWithinBook,
  VerseRangeWithinChapter,
  GroupedRangeSets
}

// import models.Connection._
import constants.DatasetMetadata._


case class HopParamsSet (
  // filter results for this hop to only include texts that overlap with these references
  // can be a single verse (Gen.1.1), or a range, even range of books (Gen-Exod) OR EVEN multiple discrete ranges (comma separated) (Gen-Exod,Lev.1.1-Lev.1.5,Rev.1-Rev.5)
  // note that jsword will want ranges to be a contiguous range, so would have to split the string to get to that osis style.
  referenceOsis : String,

  // for now only allowing alludes-to or alluded-to-by. Maybe later also allowing "both" (?)
  allusionDirection : String = "alludes-to",


  // if true, that means that this really is not a hop, and is just the params for the starting point from where we will do traversals
  // - does not allow expanding to ch/book/verses, since that should be specified within the referenceOsis already
  // - probably more differences as well ...?
//  isInitialSet : Boolean = false,

  // if true, means that there will be no more hops after this
  // -
//  isFinalHop : Boolean = false,

  /*

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

  // TODO implement this correctly, so returns all books for params set
  def getBooks() : Set[BookReference] = {
    println(s"getting starting book for $referenceOsis");
    val book : BookReference = osisToStartingBookReference(referenceOsis)

    // want it to be a set in the future, so just set it as a set
    Set(book)
  }

	// TODO how things are currently implemented, this will actually return a chapter and filter by chapter when none is specified
  def getChapters() : Set[ChapterReference] = {
    Set(osisToStartingChapterReference(referenceOsis))
  }

  // TODO implement this correctly, so returns all verses for params set
  def getVerses() : Set[VerseReference] = {Set(osisToStartingVerseReference(referenceOsis))}

  // TODO implement using jsword helpers
  def isFullBooks() : Boolean = {false}
  // TODO implement using jsword helpers
  def isFullChapters() : Boolean = {false}

  def getRefRanges() : List[ReferenceRange] = {
    parseOsisRanges(referenceOsis).map(ReferenceRange(_))
  }

} 

object HopParamsSet {

  // TODO I think most of these class methods can really be instance methods, and then just put the GraphTraversal as a field on the  HopParamsSet.



  /**
   * take a single hopParamsSet and apply the steps associated with these parameters onto the graph traversal
   *  - TODO can move to instance method
   *
   *  @return the original traversal, but now with all the steps needed to filter on this hop
   */ 
	def addStepsForHop (traversal : GraphTraversal[Vertex, Vertex], hopParamsSet : HopParamsSet, isInitialRun : Boolean, isFinalHop : Boolean) : GraphTraversal[Vertex, Vertex] = {
	  // TODO add osis parsing to get ref
	  val referenceOsis : String = hopParamsSet.referenceOsis
	  //TODO uncomment when ready
	  //val allusionDirection : String = hopParamsSet.allusionDirection
	  //TODO uncomment when ready
	  //val dataSet : String = hopParamsSet.dataSet
	  val dataSet = "all"


    // TODO (backlog, not urgent)
    // next, expand texts returned by previous out step to book/ch if requested
    // if (hopParamsSet.expandToBook) {
    // - probably go out to book, then back in to all texts connected to that book
    // ...
    // } else if (hopParamsSet.expandToChapter) {
    // - probably go out to chapter, then back in to all texts connected to that chapter
    // - maybe for this, I'll want to connect adjacent chapters to each other so they know each other and traverse easily to each other...I'm not sure if this actually creates a performance gain however
    // ...
    // } else if (hopParamsSet.expandByNumVerses) {
    // ...
    // }


    // next, filter those expanded text vertices by specified ref ranges
    // TODO (or, perhaps expand after, before filter by ref filters? which way is better??)
    val traversalWithRefFilters = addTextFilterByRefSteps(traversal.hasLabel("text"), hopParamsSet)

    // then, filter by dataset (as of 02/21 only sending in dataset "all" though, so shouldn't do anything)
    val traversalWithDatasetFilters = TraversalBuilder.addTextFilterByDatasetSteps(traversalWithRefFilters, dataSet)

    // finally, unless this is the last hop, go out the next edge (ie get intertextual connections) before filtering with next hop params
    val finalTraversalForHop = if (isFinalHop) traversalWithDatasetFilters else addOutEdgeStep(traversalWithDatasetFilters, hopParamsSet)

    finalTraversalForHop
  }

  /**
   * add steps to traversal to filter out the current texts by the reference filters that are provided as part of this HopParamsSet.
   * - different implementations exist that perform this, so the current implementation is specified within this function.
   *
   * @return traversal with steps added to filter based on reference queries
   */ 
  def addTextFilterByRefSteps (initialTraversal : GraphTraversal[Vertex, Vertex], hopParamsSet : HopParamsSet) : GraphTraversal[Vertex, Vertex] = {
    // now trying to filter references using ref index numbers
    val refRanges = hopParamsSet.getRefRanges

    val traversalWithFilter = FilterByRefRanges.addFilterByRefIndexSteps(initialTraversal, refRanges)

    traversalWithFilter
  }

  /**
   * add step to go out the edge
   * - assumes this is not the final hop params set of the hop params sets
   *
   * @return traversal with step added to filter based on reference queries
   */
  def addOutEdgeStep (initialTraversal : GraphTraversal[Vertex, Vertex], hopParamsSet : HopParamsSet) : GraphTraversal[Vertex, Vertex] = {
    // now trying to filter references using ref index numbers

    if (hopParamsSet.allusionDirection == "alludes-to") {
      return initialTraversal.out("alludes_to")
    } else if (hopParamsSet.allusionDirection == "alluded-to-by") {
      return initialTraversal.inE().hasLabel("alludes_to").outV().hasLabel("text")
    } else {
      throw new Exception("need to set allusion direction!")
    }
  }





  /**
   * filter each step using an Or-step that checks connected reference edges.
   * DEPRECATED working (ish) but really slow
   * - traverses through the reference vertices and get texts from there.
   * - if have to, traverse all verses related to this hop.
   * - if is full chapter, can traverse through chapters instead, for better performance
   * - if is full book, can traverse through books instead, for even better performance
   *
   * @param initialTraversal
   * @param hopParamsSet
   * @return traversal with steps added to filter based on reference queries
   */
  def filterByConnectedRefEdges (initialTraversal : GraphTraversal[Vertex, Vertex], hopParamsSet : HopParamsSet) : GraphTraversal[Vertex, Vertex] = {

    // TODO make helper to get range of verses or chapters...or books for osis. Will pass in ranges to the addTextFilterByRefSteps method instead

    val groupedRangeSets : GroupedRangeSets = ReferenceRange.breakdownRefRanges(hopParamsSet.getRefRanges.toSet)
    FilterByRefRanges.addFilterByConnectedRefEdgesSteps(initialTraversal, groupedRangeSets)
  }
}

