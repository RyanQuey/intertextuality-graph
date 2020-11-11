package models

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
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;
import org.apache.tinkerpop.gremlin.structure.Column._;
import org.apache.tinkerpop.gremlin.structure.T._;
// 
import org.apache.tinkerpop.gremlin.process.traversal.P.{within}
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__._;
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.structure.io.graphson.{GraphSONMapper, GraphSONVersion}
import org.apache.tinkerpop.gremlin.process.traversal.Order.{asc}

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


case class HopParamsSet (
  // can be a single text, or a range, even range of books
  referenceOsis : String,
  // for now only allowing alludes-to or alluded-to-by. Maybe later also allowing "both"
  allusionDirection : String,
  // implement later, when we start allowing extra biblical texts
  // canonical : Option[Boolean],  // BOOLEAN 
  // default to "all"
  dataSet : Option[String],
  // TODO allow filtering by author. eg, all the writings of Paul, or David. Could also just implement this on the frontend though, have js figure out how to request those books/those chapters
  // author : Option[String],  // TEXT
) 

/**
 */
object TextAPIModel {
  //////////////////////////////////////////////////
  // graph traversal builders

  def hopParamSetsToTraversal (hopParamSets : Seq[HopParamsSet]) = {
    val g : GraphTraversalSource = CassandraDb.graph

		val initialTraversal : GraphTraversal[Vertex, Vertex] = g.V()

    val traversal = traverseHopsAccumulator(hopParamSets, initialTraversal)

    traversal
  }

  /*
   * recursively iterate over hopParamSets (a sequence of param sets, each set representing a single hop) to build out a graph traversal.
   *
   */ 
  @tailrec
  def traverseHopsAccumulator (hopParamSets : Seq[HopParamsSet], traversal : GraphTraversal[Vertex, Vertex]) : GraphTraversal[Vertex, Vertex] = {
		hopParamSets match {
		  case Nil => traversal
        // pulls the 
		  case firstRemainingSet :: tail => traverseHopsAccumulator(tail, addStepsForHop(traversal, firstRemainingSet))
	  }
  }

  /*
   * take a single hopParamsSet and apply the steps associated with these parameters onto the graph traversal
   */ 
	def addStepsForHop (traversal : GraphTraversal[Vertex, Vertex], hopParamsSet : HopParamsSet) = {
	  // TODO add osis parsing to get ref
	  val allusionDirection = hopParamsSet.allusionDirection
	  val dataSet = hopParamsSet.dataSet.getOrElse("all")
	  val referenceOsis : String = hopParamsSet.referenceOsis
	  val book = osisToStartingBook(referenceOsis)
	  // TODO how things are currently implemented, this will actually return a chapter and filter by chapter when none is specified
	  val chapter = osisToStartingChapter(referenceOsis)
	  val verse = osisToStartingVerse(referenceOsis)
    val hi = getPrimaryKeyFields()

	  addTextFilterSteps(traversal.hasLabel("text"), dataSet, "Genesis", Some(1), None)
  }

  /*
   * return text traversal depending on how many args are passed in
   * Does not allow verse without chapter
   *
   */
  def getTextTraversal (dataSet : String, book : String, chapter : Option[Int], verse : Option[Int]) : GraphTraversal[Vertex, Vertex] = {
    println("getting texts");
    // TODO make this immutable!!
    val g : GraphTraversalSource = CassandraDb.graph
    val initialTraversal : GraphTraversal[Vertex, Vertex] = g.V().hasLabel("text")

    val textTraversal = addTextFilterSteps(initialTraversal, dataSet, book, chapter, verse)

    textTraversal
  }

  def addTextFilterSteps (initialTraversal : GraphTraversal[Vertex, Vertex], dataSet : String, book : String, chapter : Option[Int], verse : Option[Int])  : GraphTraversal[Vertex, Vertex]= {

    var traversal = chapter match {
      case Some(c) if verse.isDefined => fetchTextByStartingVerse(initialTraversal, book, c, verse.get)
      case Some(c) => fetchTextByStartingChapter(initialTraversal, book, c)
      case None => fetchTextByStartingBook(initialTraversal, book)
    }

    // filter by dataSet (which is currently just filtering by created_by)
    println(s"returning dataset: $dataSet")

    if (dataSet == "all") {
      println("returning  all!")

    } else if (dataSet == "treasury-of-scripture-knowledge") {
      // this is for now just tsk
      traversal = traversal.has("updated_by", "treasury-of-scripture-knowledge")

    } else if (dataSet == "user") {
      val userTypes = java.util.Arrays.asList("user-upload", "api-endpoint")
      traversal = traversal.has("updated_by", within(userTypes))
    }
    traversal
  }


  // NOTE returns traversal, doesn't actually hit the db yet until something is called on it
  def fetchTextByStartingVerse (initialTraversal : GraphTraversal[Vertex, Vertex], book : String, chapter : Int, verse : Int)  : GraphTraversal[Vertex, Vertex]= {
    println(s"getting by starting chapter: $book $chapter:$verse");
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
  def fetchTextByStartingChapter (initialTraversal : GraphTraversal[Vertex, Vertex], book : String, chapter : Int)  : GraphTraversal[Vertex, Vertex] = {
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

  def fetchTextByStartingBook (initialTraversal : GraphTraversal[Vertex, Vertex], book : String)  : GraphTraversal[Vertex, Vertex] = {
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
  
  
  //////////////////////////////////////////////////
  // graph traversals

  /**
   * For alluding texts, return what texts they allude to
  * - mostly doing order by for now for the sake of downloading as csv, makes it (almost) sorted in the csv by the alluding text chapter and verse
   * 
   * 
   * http://www.doanduyhai.com/blog/?p=13301
   *
   * so far just making this for a single reference, but eventually will probably make another one for if there is a starting and ending reference
   * TODO might move the gremlin queries themselves to the etl-tools model-helpers
   * TODO I wonder if happening two separate gremlin traversals, one to get all vertices, and then another to do all edges, and returning both separately, would be more performant??? Not sure either way
   *
   * https://docs.datastax.com/en/developer/java-driver/4.9/manual/core/dse/graph/
   *
      // TODO NOTE I'm actually not sure if this finds the source text or alluding text...
   * @param hopsCount how many times to go out on the alludes_to edge
   *
   */

  
  def textsAlludeTo(alludingTexts : List[Vertex], hopsCount : Int)  = {
    val g : GraphTraversalSource = CassandraDb.graph
    // This returns an entry about the edge between each vertex
    val sourceTexts = g.V(alludingTexts)
      .order()
        .by("starting_chapter", asc)
        // send default verse in case it doesn't have one, to prevent breaking
        // put refs with no verse in front 
        // https://groups.google.com/g/gremlin-users/c/FKbxWKG-YxA/m/MdUlPnRqCgAJ
        .by(coalesce(org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values("starting_verse"), constant(0)), asc)
      .repeat(outE().hasLabel("alludes_to").inV().hasLabel("text")).times(hopsCount)

    sourceTexts
  }

  /*
  * for sourceTexts, return texts that allude to them
  * - mostly doing order by for now for the sake of downloading as csv, makes it (almost) sorted in the csv by the source text chapter and verse
  */
  def textsAlludedToBy(sourceTexts : List[Vertex], hopsCount : Int)  = {
    val g : GraphTraversalSource = CassandraDb.graph
    // This returns an entry about the edge between each vertex
    val alludingTexts = g.V(sourceTexts).order()
        .by("starting_chapter", asc)
        .by(coalesce(org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values("starting_verse"), constant(0)), asc).                
      repeat(inE().hasLabel("alludes_to").outV().hasLabel("text")).times(hopsCount)

    alludingTexts
  }


  //////////////////////////////////////////////////
  // output helpers

  // takes Java list of graph traversals or other graph class instances and converts to json
  val json_mapper = GraphSONMapper.
                build().
                version(GraphSONVersion.V1_0).
                create().
                createMapper()


  /*
   * NOTE make sure the traversal has a "out" called on it
   * @param valuesToReturn sequence of properties on the vertices to return. Pass in empty Seq() to return all (calls valueMap())
   */
  def findPathsForTraversal (traversal : GraphTraversal[Vertex, Vertex], valuesToReturn : Seq[String]) = {
    // get values from source and target vertices
    // destructure the arg to valueMap, will be like e.g., valueMap("id", "split_passages", "starting_book")
    // https://stackoverflow.com/a/1832288/6952495
    traversal.path().by(valueMap(valuesToReturn:_*))
  }

  ///////////////////////////////
  // temp test helpers
  def getEmptyTraversal() = {
    getTextTraversal("non-dataset", "not-a-book", Some(99), Some(99))
  }

}
