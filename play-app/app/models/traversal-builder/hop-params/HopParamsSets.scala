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



// import models.Connection._
import constants.DatasetMetadata._




case class HopParamsSets (
  // can be a single text, or a range, even range of books
  paramsSets : Seq[HopParamsSet],
) {

} 


object HopParamsSets {
  /*
   *
   * - TODO convert to instance method probably
   */ 
  def buildTraversal(hopParamSets : Seq[HopParamsSet]) : GraphTraversal[Vertex, Vertex] = {
    val g : GraphTraversalSource = CassandraDb.graph

		val initialTraversal : GraphTraversal[Vertex, Vertex] = g.V()

    // recursively iterate over param sets to build a traversal with all filters set for each hop
    val traversal : GraphTraversal[Vertex, Vertex] = traverseHopsAccumulator(hopParamSets, initialTraversal)

    println(s"returning traversal: $traversal");
    traversal
  }


  /*
   * recursively iterate over hopParamSets (a sequence of param sets, each set representing a single hop) to build out a graph traversal.
   * - TODO  convert hopParamSets into instance of associated case class
   *
   */ 
  @tailrec
  def traverseHopsAccumulator (hopParamSets : Seq[HopParamsSet], traversal : GraphTraversal[Vertex, Vertex]) : GraphTraversal[Vertex, Vertex] = {
		hopParamSets match {
		  case Nil => traversal
        // pulls the 
		  case firstRemainingSet :: tail => traverseHopsAccumulator(tail, HopParamsSet.addStepsForHop(traversal, firstRemainingSet))
	  }
  }


}
