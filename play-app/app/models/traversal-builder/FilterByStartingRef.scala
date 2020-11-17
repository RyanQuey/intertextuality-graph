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


// import models.Connection._
import constants.DatasetMetadata._

/*
 * Functions for adding filters to a graph traversal that filter by starting reference only
 *  - Has less functionality and power than filtering by reference ranges, but should be much more performant and less expensive
 *
 */ 
object FilterByStartingRef {

  /*
   * - traversal should be on a step that returns text vertices
   *
   * - Do I want to phase this one out and only use addTextFilterByRefSteps? Maybe not, this one is a lot less expensive and more performant.
   * - if verses not passed in, not filtering by verses. If passed in, will filter to only return texts who have those verses.
   * - same is true for books and chapters.
   *
   */ 
  def addFilterTextByRefSteps (initialTraversal : GraphTraversal[Vertex, Vertex], book : String, chapter : Option[Int], verse : Option[Int])  : GraphTraversal[Vertex, Vertex]= {
    println(s"adding filter steps to traversal $initialTraversal");

    val traversal = chapter match {
      case Some(ch) if verse.isDefined => filterTextByStartingVerse(initialTraversal, book, ch, verse.get)
      case Some(ch) => filterTextByStartingChapter(initialTraversal, book, ch)
      case None  => filterTextByStartingBook(initialTraversal, book)
    }

    traversal
  }

  /*
   * take a traversal and add steps to filter by starting Verse
   * - initialTraversal should be on text vertices 
   * - returns traversal, doesn't actually hit the db yet until something is called on it
   */
  def filterTextByStartingVerse (initialTraversal : GraphTraversal[Vertex, Vertex], book : String, chapter : Int, verse : Int)  : GraphTraversal[Vertex, Vertex]= {
    println(s"getting by starting verse: $book $chapter:$verse");
    val texts : GraphTraversal[Vertex, Vertex] = initialTraversal
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
  def filterTextByStartingChapter (initialTraversal : GraphTraversal[Vertex, Vertex], book : String, chapter : Int)  : GraphTraversal[Vertex, Vertex] = {
    println(s"getting by starting chapter: $book $chapter");
    val texts : GraphTraversal[Vertex, Vertex] = initialTraversal
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

  def filterTextByStartingBook (initialTraversal : GraphTraversal[Vertex, Vertex], book : String)  : GraphTraversal[Vertex, Vertex] = {
    println(s"getting by starting book: $book");
    val texts : GraphTraversal[Vertex, Vertex] = initialTraversal
      .has("starting_book", book)
      .order()
        .by("starting_chapter", asc)
        // send default verse in case it doesn't have one, to prevent breaking
        // put refs with no verse in front 
        // https://groups.google.com/g/gremlin-users/c/FKbxWKG-YxA/m/MdUlPnRqCgAJ
        .by(coalesce(org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values("starting_verse"), constant(0)), asc)

    texts
  }
  

}
