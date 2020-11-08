package com.ryanquey.intertextualitygraph.graphmodels

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import com.ryanquey.intertextualitygraph.helpers.shapeless.{CaseClassFromMap}
import scala.collection.JavaConverters._ 
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

import java.util.{UUID};
import java.time.Instant;
import com.datastax.oss.driver.api.core.cql._;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold
import org.apache.tinkerpop.gremlin.structure.Vertex


import com.ryanquey.datautils.helpers.StringHelpers._;

/*
 * Represents all graph models that represent tinkerpop vertices
 * - <: is upper bounds, requiring that A is an instance of GraphVertex or child class
 */ 
trait GraphVertex[A <: GraphVertex[A]] extends GraphModel[A] {
}

trait GraphVertexCompanion[A <: GraphVertex[A]] extends GraphModelCompanion[A] {
  //def findOne() : B


  /*
   * Takes valueMap with all values (ie, what is returned from gremlin traversal) and returns case class instance
   * - especially needed so that I can create a simple findOne for all vertices (findOne)
   * - Alternatively, could try this one: https://github.com/mpollmeier/gremlin-scala#mapping-vertices-fromto-case-classes
   *   * downside is that it looks pretty complex, and would require specific implementation of case classes, so might be hard to get out of. Might not even work with DSE, who knows. I had a ahrd time figuring out how to instantiate ScalaGraph by calling "asScala" on ...well I'm not sure what I should have called it on. They call it on return value of tinkergraph.open() 
   *
   */ 
  // this didn't work, works when a specific case class is used, but not when using generics (ie A). 
  // TODO I'm sure there's a way to get it working with generics, but not wasting any more time on this
  // def valueMapToCaseClass(valueMap: java.util.Map[String, Any]) : A = {
  //    val typecastedMap = valueMap.asScala.toMap

  //    CaseClassFromMap[A](typecastedMap)
  // }
  def valueMapToCaseClass(valueMap: java.util.Map[String, Any]) : A;

  



  /*
   * - sacrificing type safety for convenience. 
   *   * The sacrifice: using type List means we don't know how many get past in, and Any means any type will compile
   *   * Convenience: can define a single helper on this trait to perform various common graph traversals. Means our code is easier to develop, since all of this logic is in one file, here
   */
  def buildVertexTraversalFromPK(pk: List[Any] ): GraphTraversal[Vertex, Vertex]


  /*
   * return type: gremlin vertex 
   *
   */
  def findOneGremlinVertex(pk: List[Any]) : Vertex = {
    val traversal = this.buildVertexTraversalFromPK(pk)
    val vertex : Vertex = traversal.values().next()

    vertex
  }

  /*
   * return type: 
   * e.g., 
   *    java.util.Map[Object,Any] = {updated_at=2020-11-07T05:21:06.855Z, split_passages=[Phlm.1.12], starting_book=Philemon, ending_chapter=1, ending_verse=12, ending_book=Philemon, starting_verse=12, updated_by=treasury-of-scripture-knowledge, starting_chapter=1, id=7e5e5965-207f-11eb-ac8c-5bc5497614cf, created_by=treasury-of-scripture-knowledge}
   */
  def findOneValueMap(pk: List[Any]) : java.util.Map[String, Any]  = {
    val traversal = this.buildVertexTraversalFromPK(pk)

    // unfold, so all the values are not nested within a java ArrayList
    // if don't set as java.util.Map[Object,Any], will be java.util.Map[Object,Nothing]
    // currently we are just assuming that all keys are strings, so typecasting like that, so at least it's a little bit more specific
    val valueMap = traversal.valueMap().by(unfold()).next().asInstanceOf[java.util.Map[String, Any]]

    valueMap
  }


  /*
   * returns an instance of one of our graph models
   */ 
  def findOne(pk: List[Any]) : A = {
    val valueMap = findOneValueMap(pk)
    valueMapToCaseClass(valueMap)
  }
}
