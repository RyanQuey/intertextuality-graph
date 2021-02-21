package models.traversalbuilder

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import javax.inject._
import java.time.Instant;
import java.util.{UUID};

import com.google.common.collect.{ImmutableList, Lists}
import com.fasterxml.jackson.databind.ObjectMapper;

import play.api.mvc._

import com.datastax.dse.driver.api.core.graph.DseGraph.g._;

// way overkill, but just trying to find what works for method "out"
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.{Path}
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Order.{asc}
import org.apache.tinkerpop.gremlin.process.traversal.P.{within, gte, lte, between}

import org.apache.tinkerpop.gremlin.structure.Column.{values};
//import org.apache.tinkerpop.gremlin.structure.T._;
// 
import org.apache.tinkerpop.gremlin.structure.{Vertex}
import org.apache.tinkerpop.gremlin.structure.io.graphson.{GraphSONMapper, GraphSONVersion}

// I think you can do only one of the next two
//import gremlin.scala._
//import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__._;
// let's force ourselves to do declare anonymous traversals explicitly
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

// end over-importing tinkerpop classes


// use to get the global session
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.datastax.dse.driver.api.core.graph.DseGraph.g._;
import com.ryanquey.intertextualitygraph.models.texts.{Text => TextClass}
import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.books.BookRecord
import com.ryanquey.intertextualitygraph.modelhelpers.TextHelpers

import com.ryanquey.intertextualitygraph.graphmodels.TextVertex
import com.ryanquey.intertextualitygraph.utils.JswordUtil.{
  osisToStartingChapterReference,
  osisToStartingVerseReference,
}
import com.ryanquey.intertextualitygraph.reference.{BookReference, ChapterReference, VerseReference}
import com.ryanquey.intertextualitygraph.reference.{
  VerseRangeWithinChapter, 
  ChapterRangeWithinBook, 
}

// import models.Connection._
import constants.DatasetMetadata._
import models.traversalbuilder.GroupedRangeSets

/*
 * Functions for adding filters to a graph traversal that filter by ranges of references (e.g., Gen.1-Gen.3 or Gen-Exod or Gen.1.1-Gen.1.3)
 *
 *
 */ 
object FilterByRefRanges {



  /**
   *
   * - The idea is to use BookRanges when stuff can be grouped into books, ChapterRanges when can be grouped into chapters, VerseRanges when can't
   * - I'm hoping this will be the single function that will replace them all
   *
   * @initialTraversal should be traversal with vertices of label "text"
   *
   */ 
  def addTextFilterSteps (
    initialTraversal : GraphTraversal[Vertex, Vertex], 
    groupedRangeSets : GroupedRangeSets
  ) : GraphTraversal[Vertex, Vertex] = {
    // TODO do these need to maintain order? If so, make it Seq not Set
    val bookReferences: Set[BookReference] = groupedRangeSets.bookReferences
    val chapterRanges: Set[ChapterRangeWithinBook] = groupedRangeSets.chapterRanges
    val verseRanges: Set[VerseRangeWithinChapter] = groupedRangeSets.verseRanges

    // https://stackoverflow.com/a/59502635/6952495
    // To begin, just do a simple traversal for each verse range. HOpefully there isn't an absurd amount of these anyways.
    // order doesn't matter, we're not doing range here, but going to check each manually
    // needs to be list, since we have to pull first out to pass into hasId, and then pass the rest, so there has to be order
    val verseIds: List[String] = verseRanges.map(verseIdsFromChapterRange).flatten.toList
    // does a traversal per range...pretty expensive. oh well
    val chapterIds: List[String] = chapterRanges.map(chapterIdsFromChapterRange).flatten.toList

    val bookNamesSet: Set[String] = bookReferences.map(_.name)
    // need it as sequence
    val bookNames: Seq[String] = bookNamesSet.toSeq

    // destructures book names, one name per arg
    var orStatements : scala.collection.mutable.Seq[GraphTraversal[Vertex,Vertex]] = scala.collection.mutable.Seq()

    // filter by booknames if there are some specified
    if (bookNames.length > 0) {
      // check if the text overlaps with one of these books...
      val withinBookStatement = within(bookNames: _*)

      orStatements = orStatements ++ Set(
        __.has("ending_book", withinBookStatement),
        __.has("starting_book", withinBookStatement)
      )
    }

    // if there are chapterIds, allow matches to those too
    if (chapterIds.length > 0) {
//      val chapterFilterTraversal = if (chapterIds.length == 1) {
//        __.out().hasLabel("chapter").hasId(chapterIds(0), chapterIds.drop(1):_*)
//      } else {
//        __.out().hasLabel("chapter").hasId(chapterIds(0)
//      }
      val chapterFilterTraversal = __.out().hasLabel("chapter").hasId(chapterIds(0), chapterIds.drop(1):_*)

      // hasId takes only ids, not vertices. First arg is first id, then can take the deconstructed arg using _*, so passing in the rest after that
      // I'm not sure why +: is not working for this, so making chapterFilterTraversal a Set and using ++ instead
      orStatements = orStatements ++ Set(chapterFilterTraversal)
    }

    // if there are verseIds, allow matches to those too
    if (verseIds.length > 0) {
      // check if the text overlaps with one of these books...
      //      val chapterFilterTraversal = if (chapterIds.length == 1) {
      //        __.out().hasLabel("chapter").hasId(chapterIds(0), chapterIds.drop(1):_*)
      //      } else {
      //        __.out().hasLabel("chapter").hasId(chapterIds(0)
      //      }
      val verseFilterTraversal = __.out().hasLabel("verse").hasId(verseIds(0), verseIds.drop(1):_*)

      // hasId takes only ids, not vertices. First arg is first id, then can take the deconstructed arg using _*, so passing in the rest after that
      orStatements = orStatements ++ Set(verseFilterTraversal)
    }


    // make immutable Seq, so can deconstruct into args
    // TODO could be faster perhaps if did outE and then queried the edge for the chapterId?? Same with verse below. But just get it working first...
    val traversal = initialTraversal.or(orStatements.toSeq:_*)

    traversal
  }



  /////////////////////////////////////////////
  // ARCHIVED / use for testing
  // ///////////////////////////////////////
  /*
   * take a traversal and add steps to filter by books
   * - initialTraversal should be on text vertices 
   * - assumes books are always contiguous for now. TODO Can implement otherwise though by using jsword's getOrdinal concept (e.g., finding by index that is "<5 or 16>x>19" is not too bad. All our books/chs/vv should have ordinal assigned, which will make querying much faster for biblical books. Extra biblical books...maybe assign ordinal to them too?
   *
   */ 
  def addTextFilterByBooksSteps (initialTraversal : GraphTraversal[Vertex, Vertex], books : List[BookReference])  : GraphTraversal[Vertex, Vertex]= {
    println(s"adding filter steps to traversal $initialTraversal");

    // find vertices for books
    // https://kelvinlawrence.net/book/Gremlin-Graph-Guide.html#bool
    // 
    // but add each "or" clause recursively, iterating over books list
    // (the gist is something like this, but will be different, .e.g., how we use initialTraversal)
    // g.V(initialTraversal).hasLabel("book").where(values('name').is(eq(books(0).or(eq(books(...)))).



    // - TODO for now, just filtering text by whether or not its starting book is within the list of books provided. 
    //    * But this is not right. Need to include end book. 
    //    * https://stackoverflow.com/questions/30043747/gremlin-multiple-filter-condition-or
    // - see here for an idea https://stackoverflow.com/questions/59501842/gremlin-find-all-nodes-from-one-set-with-a-connection-to-another. Maybe more necessary for ch or verse than book though
    // - for now, trying with "within" like 
    // kelvin lawrence's example: g.V().hasLabel('airport').has('region',within('US-TX','US-LA','US-AZ','US-OK')).

    val bookNames = books.map(_.name)
    val withinStatement = within(bookNames : _*)

    val traversal = initialTraversal.has("starting_book", withinStatement)

    // maybe what it could look like to also filter with ending_book. TODO test later, get basic working first
    //val traversal = initialTraversal.or(_().has('starting_book', withinStatement), _().has('ending_book', withinStatement))

    traversal
  }
  
  /*
   * take a traversal and add steps to filter by chapters
   * - initialTraversal should be on text vertices 
   * - Potentially going to be very expensive. Need to implement different approaches to make this cheaper
   * - assumes chapters are always contiguous for now
    // (the gist is something like this, but will be different, .e.g., how we use initialTraversal)
   *
   */ 
  // TODO chapters probably should not be int, but instead use our Verse model/graph model or jsword's classes. 
  // That way can easily extract book data out too and pass it around
  def addTextFilterByChaptersSteps (initialTraversal : GraphTraversal[Vertex, Vertex], chapterRange : ChapterRangeWithinBook)  : GraphTraversal[Vertex, Vertex]= {
    println(s"adding filter steps to traversal $initialTraversal");

    // find vertices for chapters
    // https://kelvinlawrence.net/book/Gremlin-Graph-Guide.html#bool
    // OR
    // https://stackoverflow.com/a/59502635/6952495

    // iterate over each book, and get chapters for that book using gt/lt ?? 
    // (the gist is something like this, but will be different, .e.g., how we use initialTraversal)
    // g.V(initialTraversal).hasLabel("chapter").where(values('book').is(eq((0).or(eq(name(...)))).
    val chapterIds = chapterIdsFromChapterRange(chapterRange)


    // TODO totally untested
    val traversal = initialTraversal.filter(__.out().hasLabel("chapter").hasId(within(chapterIds)))

    traversal
  }

  /*
   * take a traversal and add steps to filter by verses
   * - initialTraversal should be on text vertices 
   * - Potentially going to be very VERY expensive. Need to implement different approaches to make this cheaper
   *
   */ 

  // TODO verses probably should not be int, but instead use our Verse model/graph model or jsword's classes
  // That way can easily extract book and ch data out too and pass it around
  def addTextFilterByVersesSteps (initialTraversal : GraphTraversal[Vertex, Vertex], verseRange : VerseRangeWithinChapter)  : GraphTraversal[Vertex, Vertex]= {
    println(s"adding filter steps to traversal $initialTraversal");

    // find vertices for chapters
    // https://kelvinlawrence.net/book/Gremlin-Graph-Guide.html#bool
    //
    // iterate over each book, and get chapters for that book using gt/lt ?? And then same for verses?
    val verseIds = verseIdsFromChapterRange(verseRange)


    // TODO totally untested
    val traversal = initialTraversal.filter(__.out().hasLabel("verse").hasId(within(verseIds)))

    traversal
  }



  



  /**
   * get list of verse vertex ids that are within specified chapter and book, between starting verse (inclusive) and ending verse (exclusive)
   * - converts first to Java list, then to scala, then calls toList (now converts to Scala list), then maps to ids (NOTE: this is required. Cannot pass in vertices to .hasId() step
   *
   * @param chapterRange
   * @return
   */
  private def verseIdsFromChapterRange (verseRange : VerseRangeWithinChapter): Set[String]  = {
    val g : GraphTraversalSource = CassandraDb.graph
    g.V().hasLabel("verse")
      .has("book", verseRange.book.name)
      .has("chapter", verseRange.chapter.number)
      .has("number", between(
        verseRange.startingVerse.number,
        verseRange.endingVerse.number + 1)
      ).toList.asScala.toSet.map((v : Vertex) => v.id.toString)
  }

  /**
   *
   * - between is inclusive for starting number, exclusive for ending, so add one
   * - converts first to Java list, then to scala, then calls toList (now converts to Scala list), then maps to ids (NOTE: this is required. Cannot pass in vertices to .hasId() step
   * @param chapterRange
   * @return
   */
  private def chapterIdsFromChapterRange (chapterRange : ChapterRangeWithinBook) : Set[String] = {
    val g : GraphTraversalSource = CassandraDb.graph
    val traversal = g.V().hasLabel("chapter")
      .has("book", chapterRange.book.name)
      .has("number", between(
        chapterRange.startingChapter.number,
        chapterRange.endingChapter.number + 1)
      )

      traversal.toList.asScala.toSet.map((v : Vertex) => v.id.toString)
  }
}
