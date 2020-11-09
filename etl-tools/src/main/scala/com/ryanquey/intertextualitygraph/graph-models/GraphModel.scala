package com.ryanquey.intertextualitygraph.graphmodels

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import scala.collection.JavaConverters._ 
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.ryanquey.intertextualitygraph.helpers.Reflection.{fromMap}

import java.util.UUID;
import java.time.Instant;
import com.datastax.oss.driver.api.core.cql._;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.Element

import com.ryanquey.datautils.helpers.StringHelpers._;

import scala.reflect.runtime.universe._


/*
 * Represents all graph models
 * - keep the type of the case class on hand, to refer to it in methods
 * - Product is what case classes extend. Maybe there is some way to extend case classes directly? Required though, in order to make use of productElementNames
 */
trait GraphModel[A <: GraphModel[A]] extends Product {
  /*
   * add a reference to companion object on each model trait, so can refer to companion object dynamically by these parent traits
   * - https://stackoverflow.com/a/50590202/6952495
   * - this will require defining companionObject method on each child case class
   */ 
  def companionObject : GraphModelCompanion[A]

  /*
   * return all fields as a list
   * (note that this works, but being a method in a case class, only works from a case class instance)
   */ 
  def modelFields () : List[String] = {
    this.productElementNames.toList
  }

  /*
   * return all db columns for this case class as a list (does by converting all fields to snake case)
   */ 
  def dbColumns () : List[String] = {
    this.productElementNames.toList.map(camelToSnake(_))
  }
}

/*
 * Companion object for the case class 
 * - keep the type of the case class on hand, to refer to it in methods 
 *   * we won't need to refer to the companion object as a type...since it's never used as a type!
 *   * to refer to the companion object, use GraphModelSubClass.companionObject (but instead of GraphModelSubClass, put the name of the subclass)
 */ 
trait GraphModelCompanion[A <: GraphModel[A]] {
  // might not need
  //def modelFields () : List[String]

  // https://stackoverflow.com/a/16079804/6952495
  def classAccessors[T: TypeTag]: List[String] = typeOf[T].members.sorted.collect {
    case m: MethodSymbol if m.isCaseAccessor => m.name.toString
  }.toList


  /*
  // better than doing crazy reflection stuff
  // leave as method though if we use reflection in future though
   * NOTE WARNING: Prone to runtime errors if this is off, even by one. Need to unit test all of these
   */ 
  def getOptionalFields() : Set[String]
}
