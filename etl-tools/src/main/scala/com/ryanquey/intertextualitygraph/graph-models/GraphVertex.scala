package com.ryanquey.intertextualitygraph.graphmodels

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import com.ryanquey.intertextualitygraph.helpers.shapeless.{CaseClassFromMap}
//import scala.collection.JavaConverters._ 
import scala.jdk.CollectionConverters._
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.ryanquey.intertextualitygraph.helpers.Reflection.{fromMap, getFieldsOfTypeForClass}
import com.ryanquey.intertextualitygraph.graphmodels.BookVertex

import java.util.{UUID};
import java.time.Instant;
import com.datastax.oss.driver.api.core.cql._;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.Element

import scala.collection.convert.JavaCollectionWrappers$JListWrapper

// for TypeTag and ClassTag, and I think that's it
import scala.reflect._
import scala.reflect.runtime.universe._

import com.ryanquey.datautils.helpers.StringHelpers._;

/*
 * Represents all graph models that represent tinkerpop vertices
 * - <: is upper bounds, requiring that A is an instance of GraphVertex or child class
 */ 
trait GraphVertex[A <: GraphVertex[A]] extends GraphModel[A] {

  /*
   * - It looks like you can't pass in arbitrary keys to get values from case classes, or if you can, it's not recommended. see: 
   *    *  comments under: https://stackoverflow.com/a/45548605/6952495
   *    *  comments under https://stackoverflow.com/q/47940532/6952495
   */ 
  def getPrimaryKey() : List[Any];

  /*
   * return type: gremlin vertex 
   *
   */
  def findOneGremlinVertex() : Vertex = {
    this.companionObject.findOneGremlinVertexByPK(this.getPrimaryKey)
  }

  def buildTraversal() : GraphTraversal[Vertex, Vertex] = {
    this.companionObject.buildVertexTraversalFromPK(this.getPrimaryKey)
  }

  override
  def companionObject : GraphVertexCompanion[A]
}

trait GraphVertexCompanion[A <: GraphVertex[A]] extends GraphModelCompanion[A] {

  /*
   * - sacrificing type safety for convenience. 
   *   * The sacrifice: using type List means we don't know how many get past in, and Any means any type will compile
   *   * Convenience: can define a single helper on this trait to perform various common graph traversals. Means our code is easier to develop, since all of this logic is in one file, here
   *   TODO automate this, by using recursive function on the g.V() GraphTraversal
   */
  def buildVertexTraversalFromPK(pk: List[Any] ): GraphTraversal[Vertex, Vertex]

  /*
   * return type: gremlin vertex 
   *
   */
  // alias, to call if need to refer to GraphModel. 
  // - for now, just typecast the result. Better than defining this method out, and typecasting this to get findOneGremlinVertexByPK, since then we would have to also define a buildElementTraversalFromPK method 
  def findOneGremlinElementByPK(pk: List[Any]) : Element = findOneGremlinVertexByPK(pk).asInstanceOf[Element]

  /*
   * not sure if I want to use this one, but if I do, it might not be really working, I think need to use the mpollmeier lib to make sure that type checking works
   * TODO tested this out
   */ 
  def findOneGremlinVertexByPK(pk: List[Any]) : Vertex = {
    val traversal = this.buildVertexTraversalFromPK(pk)
    //val vertex : Vertex = traversal.values().next()
    val vertex : Vertex = traversal.next()

    vertex
  }

  /*
   * get a value map from a single record, specified by the primary key
   *
   * - could use on its own, but also necessary for generating case classes from graph vertices
   * - return type: 
   * e.g., 
   *    java.util.Map[Object,Any] = {updated_at=2020-11-07T05:21:06.855Z, split_passages=[Phlm.1.12], starting_book=Philemon, ending_chapter=1, ending_verse=12, ending_book=Philemon, starting_verse=12, updated_by=treasury-of-scripture-knowledge, starting_chapter=1, id=7e5e5965-207f-11eb-ac8c-5bc5497614cf, created_by=treasury-of-scripture-knowledge}
   */
  def findOneByPKValueMap(pk: List[Any]) : java.util.Map[String, Any]  = {
    val traversal = this.buildVertexTraversalFromPK(pk)

    // unfold, so all the values are not nested within a java ArrayList
    // if don't set as java.util.Map[Object,Any], will be java.util.Map[Object,Nothing]
    // currently we are just assuming that all keys are strings, so typecasting like that, so at least it's a little bit more specific
    val valueMap = traversal.valueMap().by(unfold()).next().asInstanceOf[java.util.Map[String, Any]]

    valueMap
  }

  def prepareValueMapForCaseClass (valueMap: java.util.Map[String, Any]) : Map[String, Any] = {
    val typecastedMap = valueMap.asScala.toMap

    val fixedKeysMap = typecastedMap.map { 
      case (key, value) => {
        // from db col => case class field
        val camelKey = snakeToCamel(key)
        camelKey -> value
      }
    }

    // convert java lists to scala lists
    // - watch out, can fail on runtime, fairly easily, would be good to have unit tests for this
    // TODO this does not yet handle Option[List[]] I don't think
    val listFields = getFieldsOfType[List[Any]]()

    println(s"listFields: $listFields")
    val listVals : List[(String, List[Any])] = listFields.map((field) => {
      // anyMap.get will return an option (either Some(val) or None). so can just use that
      // note that this will throw error if key represented by var "field" does not exist, but getFieldsOfType didn't specify a fieldwith type option, so that's fine
      println(s"getting original val of field: $field")
      val originalVal : Any = fixedKeysMap(field)
      println(s" original val : $originalVal")
      // val convertedVal : java.util.List[Any]= originalVal.asInstanceOf[java.util.List[Any]]
      // val convertedVal : Iterable[Any]= originalVal.asInstanceOf[Iterable[Any]]
      // I don't know why, but had trouble converting directly to java.util.List, need to typecast to java ArrayList first
      val convertedVal : java.util.ArrayList[Any] = originalVal.asInstanceOf[java.util.ArrayList[Any]]
      val somethingeElse = convertedVal.asInstanceOf[java.util.List[Any]].asScala.toList
      println(s"what is this?: $somethingeElse")
 
      field -> somethingeElse
    })
    println(s"listVals: $listFields")

    val typecastedListsMap = fixedKeysMap ++ listVals
    println(s"typecastedListsMap: $typecastedListsMap")



    // add keys for all fields, so if optional
    // not sure if necessary for the reflection we're doing now? but necessary for using shapeless like before
    // TODO this does not yet handle Option[List[]] I don't think
    // so will return e.g., List(("key1" -> None), ("key2" -> Some(4)))
    val optionVals : Set[(String, Option[Any])] = getOptionalFields.map {
      // anyMap.get will return an option (either Some(val) or None). so can just use that
      case (field) => field -> fixedKeysMap.get(field)
    }

    println(s"optionVals: ${optionVals}")

    // add/update optionVals to existing map, so all fields in the case class have corresponding key
    // https://stackoverflow.com/a/29008426/6952495
    val preparedMap = typecastedListsMap ++ optionVals



    // TODO this does not yet handle Option[List[]] I don't think
    // or any other collection for that matter


    preparedMap
  }

  /*
   * Takes valueMap with all values (ie, what is returned from gremlin traversal) and returns case class instance
   * - especially needed so that I can create a simple findOne for all vertices (findOne)
   * - Alternatively, could try this one: https://github.com/mpollmeier/gremlin-scala#mapping-vertices-fromto-case-classes
   *   * downside is that it looks pretty complex, and would require specific implementation of case classes, so might be hard to get out of. Might not even work with DSE, who knows. I had a ahrd time figuring out how to instantiate ScalaGraph by calling "asScala" on ...well I'm not sure what I should have called it on. They call it on return value of tinkergraph.open() 
   *
   */ 
  // this didn't work, works when a specific case class is used, but not when using generics (ie A). 
  // TODO I'm sure there's a way to get it working with generics, but not wasting any more time on this
  // def preparedValueMapToCaseClass(valueMap: java.util.Map[String, Any]) : A = {
  //    val typecastedMap = valueMap.asScala.toMap

  //    CaseClassFromMap[A](typecastedMap)
  // }
  def preparedValueMapToCaseClass(valueMap: Map[String, Any]) : A;


  /*
   * returns an instance of one of our graph models
   */ 
  def findOneByPK(pk: List[Any]) : A = {
    val valueMap = findOneByPKValueMap(pk)
    val preparedValueMap = prepareValueMapForCaseClass(valueMap)

    preparedValueMapToCaseClass(preparedValueMap)
  }

  /*
   * deletes an instance of one of our graph models
   * - can easily add a wrapper to delete by case class instance
   */ 
  def deleteByPrimaryKey(pk: List[Any]) : Unit = {
    val traversal = this.buildVertexTraversalFromPK(pk)

    val valueMap = traversal.drop()
  }

  def getFieldsOfType[T: TypeTag: ClassTag] () : List[String];

  /*
   * - needs to maintain order, since we will pass in primary key in order sometimes (C* generally requires knowing the primary key in order). So use a list, not set
   */
  def getPrimaryKeyFields () : List[String];
}


