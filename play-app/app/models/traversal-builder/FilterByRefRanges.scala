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
 * Functions for adding filters to a graph traversal that filter by ranges of references (e.g., Gen.1-Gen.3 or Gen-Exod or Gen.1.1-Gen.1.3)
 *
 *
 */ 
object FilterByRefRanges {
  /*
   * take a traversal and add steps to filter by books
   * - initialTraversal should be on text vertices 
   *
   */ 
  def addTextFilterByBooksSteps (initialTraversal : GraphTraversal[Vertex, Vertex], books : Set[String])  : GraphTraversal[Vertex, Vertex]= {
    println(s"adding filter steps to traversal $initialTraversal");

    // find vertices for books
    // https://kelvinlawrence.net/book/Gremlin-Graph-Guide.html#bool
    // 
    // but add each "or" clause recursively, iterating over books list
    // (the gist is something like this, but will be different, .e.g., how we use initialTraversal)
    // g.V(initialTraversal).hasLabel("book").where(values('name').is(eq(books(0).or(eq(books(...)))).


    // dummy code to let it still compile until we implement this
    val traversal = initialTraversal

    traversal
  }
  
  /*
   * take a traversal and add steps to filter by chapters
   * - initialTraversal should be on text vertices 
   * - Potentially going to be very expensive. Need to implement different approaches to make this cheaper
    // (the gist is something like this, but will be different, .e.g., how we use initialTraversal)
   *
   */ 
  // TODO chapters probably should not be int, but instead use our Verse model/graph model or jsword's classes. 
  // That way can easily extract book data out too and pass it around
  def addTextFilterByChaptersSteps (initialTraversal : GraphTraversal[Vertex, Vertex], chapters : Set[Int])  : GraphTraversal[Vertex, Vertex]= {
    println(s"adding filter steps to traversal $initialTraversal");

    // find vertices for chapters
    // https://kelvinlawrence.net/book/Gremlin-Graph-Guide.html#bool
    //
    // iterate over each book, and get chapters for that book using gt/lt ?? 
    // (the gist is something like this, but will be different, .e.g., how we use initialTraversal)
    // g.V(initialTraversal).hasLabel("chapter").where(values('book').is(eq((0).or(eq(name(...)))).


    // dummy code to let it still compile until we implement this
    val traversal = initialTraversal

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
  def addTextFilterByVersesSteps (initialTraversal : GraphTraversal[Vertex, Vertex], verses : Set[Int])  : GraphTraversal[Vertex, Vertex]= {
    println(s"adding filter steps to traversal $initialTraversal");

    // find vertices for chapters
    // https://kelvinlawrence.net/book/Gremlin-Graph-Guide.html#bool
    //
    // iterate over each book, and get chapters for that book using gt/lt ?? And then same for verses?
    // g.V(initialTraversal).hasLabel("chapter").where(values('book').is(eq((0).or(eq(name(...)))).


    // dummy code to let it still compile until we implement this
    val traversal = initialTraversal

    traversal
  }

}
