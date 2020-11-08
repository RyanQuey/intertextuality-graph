package com.ryanquey.intertextualitygraph.graphmodels

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import scala.collection.JavaConverters._ 
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

import java.util.UUID;
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
   * - Alternatively, could try this one: https://github.com/mpollmeier/gremlin-scala#mapping-vertices-fromto-case-classes
   *   * downside is that it looks pretty complex, and would require specific implementation of case classes, so might be hard to get out of. Might not even work with DSE, who knows
   *
   */ 
  /*def valueMapToCaseClass(valueMap: java.util.Map[Object, Any]) : A = {
    // fields of case class, in order to defind by case class
    val fields : List[String] = modelFields

    // - use the order defined in the case class definition (so we don't have to use named arguments)
    // - in valueMap, fields will be the same as in the database (snake case), so convert that to camel case 
    val valuesInOrder = fields.map((field) => {
      val dbCol : String = snakeToCamel(field)
      val value = valueMap.get(dbCol)

      value
    })


    // whatever the case class is, instantiate it using these values, 
    // - lists provide arg destructuring, so use list (https://alvinalexander.com/scala/how-to-define-methods-variable-arguments-varargs-fields/)
    // - using some lightweight reflection here on the case class to make instance
    val caseClass = classOf[A]
    caseClass.newInstance(valuesInOrder: _*)
  }
  */

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
  def findOneValueMap(pk: List[Any]) : java.util.Map[Object, Any]  = {
    val traversal = this.buildVertexTraversalFromPK(pk)

    // unfold, so all the values are not nested within a java ArrayList
    // if don't set as java.util.Map[Object,Any], will be java.util.Map[Object,Nothing]
    val valueMap : java.util.Map[Object,Any] = traversal.valueMap().by(unfold()).next()

    valueMap
  }


  /*
   * returns an instance of one of our graph models
   */ 
  /*def findOne(pk: List[Any]) : B = {
    val gremlinVertex = findOneGremlinVertex(pk).next()

    // convert gremlin vertex class to graph model
    gremlinVertexToCaseClass(gremlinVertex)
  }*/
}
