package models.traversalbuilder

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
import com.ryanquey.intertextualitygraph.models.traversalbuilder.{
  VerseReference, 
  ChapterReference, 
  VerseRangeWithinChapter, 
  ChapterRangeWithinBook, 
  BookReference, 
  ReferenceRange
}


// import models.Connection._
import constants.DatasetMetadata._

/*
 * Functions for adding filters to a graph traversal that filter by ranges of references (e.g., Gen.1-Gen.3 or Gen-Exod or Gen.1.1-Gen.1.3)
 *
 *
 */ 
object FilterByRefRanges {
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

    val bookNames = books.map(name)
    val withinStatement = within(bookNames : _*)

    val traversal = initialTraversal.has('starting_book', withinStatement)

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
    val traversal = initialTraversal.filter(__.out().hasLabel("chapter")(__.hasId(within(chapterIds))))

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
    val traversal = initialTraversal.filter(__.out().hasLabel("verse")(__.hasId(within(verseIds))))

    traversal
  }

  /*
   *
   * - The idea is to use BookRanges when stuff can be grouped into books, ChapterRanges when can be grouped into chapters, VerseRanges when can't
   * - I'm hoping this will be the single function that will replace them all
   *
   *
   */ 
  def addTextFilterSteps (initialTraversal : GraphTraversal[Vertex, Vertex], books : Set[BookReference], chapterRanges : Set[ChapterRangeWithinBook]], verseRanges : Set[VerseRangeWithinChapter])  : GraphTraversal[Vertex, Vertex]= {

    // https://stackoverflow.com/a/59502635/6952495
    // To begin, just do a simple traversal for each verse range. HOpefully there isn't an absurd amount of these anyways.
    val verseIds : Set[List[Strings]] = verseRanges.map(verseIdsFromChapterRange)
    val chapterIds : Set[List[Strings]] = verseRanges.map(chapterIdsFromChapterRange)

    val bookNames = books.map(name)
    val withinBookStatement = within(bookNames : _*)
    val traversal = initialTraversal.or(
      // either the text overlaps with one of these books...
      _().has('starting_book', withinBookStatement), 
      _().has('ending_book', withinBookStatement)
      // ... or is connected to one of these chapters
      // TODO could be faster perhaps if did outE and then queried the edge for the chapterId?? Same with verse below. But just get it working first...
      __.out().hasLabel("chapter")(__.hasId(within(chapterIds)))
      // ... or is connected to one of these verses. HOpefully there's not too many...
      __.out().hasLabel("verse")(__.hasId(within(verseIds)))
    )

    traversal
  }


  



  private def verseIdsFromChapterRange (verseRange : VerseRangeWithinChapter) = {
    g.V().hasLabel("verse").has("book", verseRange.book.name).has("chapter", verseRange.chapter.number).where(values('number').is(gt((verseRange.startingverse.number).and(lt(verseRange.endingverse.number))).toList 
  }

  private def chapterIdsFromChapterRange (chapterRange : ChapterRangeWithinBook) = {
    g.V().hasLabel("chapter").has("book", chapterRange.book.name).where(values('number').is(gt((chapterRange.startingChapter.number).and(lt(chapterRange.endingChapter.number))).toList
  }
}
