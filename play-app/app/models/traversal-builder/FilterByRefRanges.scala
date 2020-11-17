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
 * Functions for adding filters to a graph traversal that filter by ranges of references
 *
 *
 */ 
object FilterByRefRanges {
  /*
   *
   * - if verses not passed in, not filtering by verses. If passed in, will filter to only return texts who have those verses.
   * - same is true for books and chapters.
   *
   */ 
  def addTextFilterByRefSteps (initialTraversal : GraphTraversal[Vertex, Vertex], book : String, chapter : Option[Int], verses : Option[Set[Int]])  : GraphTraversal[Vertex, Vertex]= {
    println(s"adding filter steps to traversal $initialTraversal");

    val traversal = chapter match {
      /*
      case Some(ch) if verses.isDefined => fetchTextByStartingVerse(initialTraversal, book, ch, verses.get)
      case Some(ch) => fetchTextByStartingChapter(initialTraversal, book, ch)
      */
     // just to get it to compile, will be totally different in the second
      case None  => initialTraversal
      case _  => initialTraversal
    }

    traversal
  }

}
