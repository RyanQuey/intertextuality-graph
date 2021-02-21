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


// import models.Connection._
import constants.DatasetMetadata._

/*
 * Functions for building a traversal
 * - these are the more generic functions, not specific to a certain category of traversal methods we're using
 *   TODO probably eventually, all this will go into etl-tools. this whole package and all sub packages
 *
 */ 
object TraversalBuilder {
  
  def addTextFilterByDatasetSteps (initialTraversal : GraphTraversal[Vertex, Vertex], dataSet : String)  : GraphTraversal[Vertex, Vertex]= {
    // filter by dataSet (which is currently just filtering by created_by)
    println(s"returning dataset: $dataSet")

    dataSet match {
      case "all" => initialTraversal
      case "treasury-of-scripture-knowledge" => {
        // this is for now just tsk
        initialTraversal.has("updated_by", "treasury-of-scripture-knowledge")
      }
      case "user" => {
        val userTypes = java.util.Arrays.asList("user-upload", "api-endpoint")
        initialTraversal.has("updated_by", within(userTypes))
      }
    }
  }

  /*
   * NOTE make sure the traversal has a "out" called on it
   * @param valuesToReturn sequence of properties on the vertices to return. Pass in empty Seq() to return all (calls valueMap())
   * - get values from source and target vertices
   * - destructure the arg to valueMap, if e.g., Seq("id", "split_passages", "starting_book") will be like e.g., valueMap("id", "split_passages", "starting_book")
   * - https://stackoverflow.com/a/1832288/6952495
   */
  def findPathsForTraversal (initialTraversal : GraphTraversal[Vertex, Vertex], valuesToReturn : Seq[String]) : GraphTraversal[Vertex, Path] = {
    val pathForTraversal = initialTraversal.path()
    println(s"got path for traversal: $pathForTraversal");

    // destructure the sequence of strings (using _*). 
    val valuesForPaths = pathForTraversal.by(valueMap(valuesToReturn:_*))
    println(s"values for paths: $valuesForPaths");
    valuesForPaths
  }

}
