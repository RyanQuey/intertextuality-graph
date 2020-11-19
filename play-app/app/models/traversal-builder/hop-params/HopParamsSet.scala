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
import gremlin.scala._
import com.datastax.dse.driver.api.core.graph.DseGraph.g._;


// use to get the global session
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.ryanquey.intertextualitygraph.models.texts.{Text => TextClass}
import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.books.BookRecord
import com.ryanquey.intertextualitygraph.modelhelpers.TextHelpers

import com.ryanquey.intertextualitygraph.utils.JswordUtil.{
  osisToStartingBook,
  osisToStartingChapterNum,
  osisToStartingVerse,
}

import models.traversalbuilder.{FilterByRefRanges, TraversalBuilder}
import models.traversalbuilder.reference.{ReferenceRange}

// import models.Connection._
import constants.DatasetMetadata._




case class HopParamsSet (
  // filter results for this hop to only include texts that overlap with these references
  // can be a single verse (Gen.1.1), or a range, even range of books (Gen-Exod) OR EVEN multiple discrete ranges (comma separated) (Gen-Exod,Lev.1.1-Lev.1.5,Rev.1-Rev.5)
  // note that jsword will want ranges to be a contiguous range, so would have to split the string to get to that osis style.
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

  // TODO implement this correctly, so returns all books for params set
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

  // TODO implement this correctly, so returns all verses for params set
  def getVerses() : Set[Int] = {Set(osisToStartingVerse(referenceOsis))}

  // TODO implement using jsword helpers
  def isFullBooks() : Boolean = {false}
  // TODO implement using jsword helpers
  def isFullChapters() : Boolean = {false}

  def getRefRanges() : List[ReferenceRange] = {
    parseOsisRanges(referenceOsis).map(ReferenceRange(_))
  }

} 

object HopParamsSet {

  /*
   * take a single hopParamsSet and apply the steps associated with these parameters onto the graph traversal
   *  - TODO can move to instance method
   */ 
	def addStepsForHop (traversal : GraphTraversal[Vertex, Vertex], hopParamsSet : HopParamsSet) : GraphTraversal[Vertex, Vertex] = {
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
    // TODO (or, perhaps expand first, then filter by ref filters? which way is better??)
    val traversalWithRefFilters = addTextFilterByRefSteps(traversal.hasLabel("text"), hopParamsSet)

    // finally, filter by dataset
    val traversalWithDatasetFilters = TraversalBuilder.addTextFilterByDatasetSteps(traversalWithRefFilters, dataSet)

    traversalWithDatasetFilters
  }

  /*
   * traverse through the reference vertices and get texts from there. 
   * - if have to, traverse all verses related to this hop.
   * - if is full chapter, can traverse through chapters instead, for better performance
   * - if is full book, can traverse through books instead, for even better performance
   */ 
  def addTextFilterByRefSteps (initialTraversal : GraphTraversal[Vertex, Vertex], hopParamsSet : HopParamsSet) : GraphTraversal[Vertex, Vertex] = {

    // TODO make helper to get range of verses or chapters...or books for osis. Will pass in ranges to the addTextFilterByRefSteps method instead
    val refRanges : List[ReferenceRange] = hopParamsSet.getRefRanges

    FilterByRefRanges.addTextFilterSteps(breakdownRefRanges(range))

    // // iterate over ref ranges and for each range, add whatever filters
    // for (range <- refRanges) {

    //   if (range.isWholeBooks) {
    //     val bookNames = range.getBookNames
    //     FilterByRefRanges.addTextFilterByBooksSteps(books)

    //   } else if (range.isWholeChapters) {
    //     val chapters = range.getChapters
    //     FilterByRefRanges.addTextFilterByChaptersSteps(chapters)

    //   } else {
    //     val verses = range.getVerses
    //     FilterByRefRanges.addTextFilterByVersesSteps(verses)
    //   }
    // }
  }

  /*
   *  takes ReferenceRange set and converts each one into either a set of book references, a ChapterRangeWithinBook, or a VerseRangeWithinChapter
   * - the idea is to break it down into the greatest possible segments, since these will be used 
   * - books don't need to/can't use ranges, since they use strings rather than integers. Probably could use integers if we wanted, but then would need to assign integers for extra biblical books as well... Which is also possible, but just do it with strings for now. There probably should not be that many, so should be fine.
   *
   */ 
  def breakdownRefRanges (referenceRanges : Set[ReferenceRange]) : ((Set[BookReference], Set[ChapterRangeWithinBook]], Set[VerseRangeWithinChapter])) = {
    val bookReferences : Set[BookReference] = Set()
    val chapterRanges : Set[ChapterRangeWithinBook] = Set()
    val verseRanges : Set[VerseRangeWithinChapter] = Set()
    for (referenceRange <- referenceRanges) {
      // 1) get out whatever whole books that are contained by this ref range that we can
      // NOTE this should work, but might just use recursive function below to pull out everything we need
      val wholeBooks = referenceRange.getWholeBooks
      bookReferences ++ wholeBooks

      // 2) with remainder, get out whatever will chapters we can
      // 3) with remainder, set the rest to verseRanges. Should be at most two - one at the start, one at the end. Everything in the middle should be a whole chapter/book
      // - since all whole books are gone, we can just look at the start and end book
      val startingVerse : VerseReference = referenceRange.getStartingVerse
      val endingVerse : VerseReference = referenceRange.getEndingVerse



      /*
       * - since doing this in multiple places, make a helper
       */ 
      def addVersesBetween (startingVerse : VerseReference, endingVerse : VerseReference) : Unit = {
        // TODO 

      }


      // start with startingVerse and recursively iterate over chapters until we get to ending Verse
      def breakdownRefRange (startingVerse : VerseReference, endingVerse : VerseReference) : Unit = {
        val endOfBook : VerseReference = startingVerse.getLastVerseOfBook
        val endOfChapter : VerseReference = startingVerse.getLastVerseOfChapter

        // is whole book, and we've pulled those out already. We're done, no need to continue
        val rangeIsWholeBook = startingVerse.book == endingVerse.book && endingVerse.isSameAs(endOfBook) && startingVerse.isStartOfBook
        // we've made it to the end. Add this verse and we're done
        // actually, this probably means they only identified one verse they wanted to filter by

        if (rangeIsWholeBook) {
          return
        }

        // temporarily setting here, but might overwrite
        var newStartingVerse : VerseReference = startingVerse.getLastVerseOfChapter

        // 1) CHECK IF WHOLE BOOK
        // - at this point, only possible if there are multiple books left in this range
        val hasWholeBookToAdd = startingVerse.isStartOfBook && startingVerse.book != endingVerse.book)
        if (hasWholeBookToAdd) { 
          // passing for now, already added
          newStartingVerse = endOfBook + 1 verse // can't do this, but that's what it would be
        }

        // 2) CHECK IF WHOLE CHAPTER
        // - true as long as we don't end before end of chapter and starting verse is start of chapter
        val hasWholeChapterToAdd = startingVerse.isStartOfChapter && (!endingVerse.isSameChapterAs(startingVerse) || endingVerse.isSameAs(endOfChapter))
        if (!hasWholeBookToAdd && hasWholeChapterToAdd) { 
          chapterRanges ++ endingVerse.getChapterReference
          newStartingVerse = endOfChapter + 1 verse // can't do this, but that's what it would be
        }

        // 3) ADD ALL VERSES OTHERWISE
        if (!hasWholeBookToAdd && !hasWholeChapterToAdd) {

          // make sure to not go past endVerse
          val terminalVerseForIteration = earliestVerseBetween(endOfChapter.number, endingVerse.number)

          addVersesBetween(startingVerse, terminalVerseForIteration) 
          newStartingVerse = terminalVerseForIteration + 1 verse // can't do this, but that's what it would be
        }

        // 4) CHECK IF FINISHED SO KNOW IF SHOULD CONTINUE ITERATING
        val finished = newStartingVerse >= endingVerse || rangeIsWholeBook

        if (!done) {
          // keep iterating
          breakdownRefRange(newStartingVerse, endingVerse)
        }
      }

      breakdownRefRange(startingVerse, endingVerse)

      // now our sets are built out. Return as tuple
      (bookReferences, chapterRanges, verseRanges)
    }
  }

}

