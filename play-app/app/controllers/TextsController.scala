package controllers

// https://www.playframework.com/documentation/2.8.x/ScalaJson
import play.api.libs.json._
import play.api.libs.functional.syntax._

import javax.inject._
import play.api._
import play.api.mvc._
import constants.DatasetMetadata._
// import models.Connection._
import java.time.Instant;
import java.util.{UUID, Collection, List};
// they don't like after 2.12
// import collection.JavaConverters._
import scala.jdk.CollectionConverters._
import com.google.common.collect.{ImmutableList, Lists}


// import gremlin.scala._

import com.datastax.dse.driver.api.core.graph.DseGraph.g._;

import com.ryanquey.datautils.cassandraHelpers.CassandraDb

// way overkill, but just trying to find what works for method "out"
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Operator._;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Order._;
import org.apache.tinkerpop.gremlin.process.traversal.P._;
import org.apache.tinkerpop.gremlin.process.traversal.Pop._;
import org.apache.tinkerpop.gremlin.process.traversal.SackFunctions._;
import org.apache.tinkerpop.gremlin.process.traversal.Scope._;
import org.apache.tinkerpop.gremlin.process.traversal.TextP._;
import org.apache.tinkerpop.gremlin.structure.Column._;
import org.apache.tinkerpop.gremlin.structure.T._;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__._;

// end over-importing tinkerpop classes

import org.apache.tinkerpop.gremlin.structure.Vertex
import gremlin.scala._
import org.apache.tinkerpop.gremlin.structure.io.graphson.{GraphSONMapper, GraphSONVersion}

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.books.BookRecord
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 *
 */
class TextsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {


  // for now requiring a separate API call, but maybe later we'll just merge these into the target vertices as well
  def findTextsByStartingRef(dataSet : String, book : String, chapter : Option[Int], verse : Option[Int]) = Action { implicit request: Request[AnyContent] =>
    val texts = _getTextTraversal(dataSet, book, chapter, verse)
      .valueMap() 
      .toList()

    val outputJson = json_mapper.writeValueAsString(texts)

    Ok(outputJson)
  }

  /* 
   * Passed in Reference is alluding Text. Get all source texts alluded to by the passed in ref. Return paths for those texts. 
   * - go back hopsCount hops
   * - gets all values for all vertices along path
   * - For now, get all that start with this ref
   * - TODO eventually, will get all texts that include this verse, rather than all texts that start with this verse
  */ 
  def getPathsForTextsRefAlludesTo(dataSet : String, book : String, chapter : Option[Int], verse : Option[Int], hopsCount : Int) = Action { implicit request: Request[AnyContent] =>

    if (hopsCount > 4) {
      throw new Exception("Hops count can't be more than four...and that's already pushing it")
    }


    val sourceTexts = _getTextTraversal(dataSet, book, chapter, verse)
      .toList()

    if (sourceTexts.size == 0) {
      Ok("[]")
    } else {

      // find what texts allude to the texts we found
      val alludingTexts = _textsAlludedToBy(sourceTexts, hopsCount)

      // passing in no args for getting ALL fields
      val pathsWithValues = _findPathsForTraversal(alludingTexts, Seq()).toList

      // now we have gremlin output, that is roughly a list of lists of maps, and each map is a vertex with all values attached. 
      // we want to return this as two data items for use with our chart, one for nodes, one for edges
      // TODO convert stuff using scala; for now just sending to frontend and converting using js
      // possibly use gremlin-scala?
      pathsWithValues.asScala.foreach{ pathWithValues => {
        // println(pathWithValues.getClass)
      }}

      val output = json_mapper.writeValueAsString(pathsWithValues)

      Ok(output)
    }
  }
  
  /* 
   * For a given starting reference (book, chapter, verse), and find all texts that the ref alludes to. Return all paths for those texts
   * - go back hopsCount hops
   * - gets all values for all vertices along path
   * - For now, get all that start with this ref
   * - TODO eventually, will get all texts that include this verse, rather than all texts that start with this verse
  */ 
  def getPathsForTextsAlludedToByRef(dataSet : String, book : String, chapter : Option[Int], verse : Option[Int], hopsCount : Int) = Action { implicit request: Request[AnyContent] =>
    
    if (hopsCount > 4) {
      throw new Exception("Hops count can't be more than four...and that's already pushing it")
    }
    
    
    val alludingTexts = _getTextTraversal(dataSet, book, chapter, verse)
      .toList()

    if (alludingTexts.size == 0) {
      Ok("[]")
    } else {

      // TODO NOTE I'm actually not sure if this finds the source text or alluding text...
      val sourceTexts = _textsAlludeTo(alludingTexts, hopsCount)

      // passing in no args for getting ALL fields
      val pathsWithValues = _findPathsForTraversal(sourceTexts, Seq()).toList

      // now we have gremlin output, that is roughly a list of lists of maps, and each map is a vertex with all values attached. 
      // we want to return this as two data items for use with our chart, one for nodes, one for edges
      // TODO convert stuff using scala; for now just sending to frontend and converting using js
      // possibly use gremlin-scala?
      pathsWithValues.asScala.foreach{ pathWithValues => {
        // println(pathWithValues.getClass)
      }}

      val output = json_mapper.writeValueAsString(pathsWithValues)

      Ok(output)
    }
  }

  //////////////////////////////////////////////////
  // TODO Move all of this kind of stuff into the model
  // graph traversal builders

  /*
   * return text traversal depending on how many args are passed in
   * Does not allow verse without chapter
   *
   */
  def _getTextTraversal (dataSet : String, book : String, chapter : Option[Int], verse : Option[Int]) = {
    println("getting texts");
    var traversal = chapter match {
      case Some(c) if verse.isDefined => _fetchTextByStartingVerse(book, c, verse.get)
      case Some(c) => _fetchTextByStartingChapter(book, c)
      case None => _fetchTextByStartingBook(book)
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
  def _fetchTextByStartingVerse (book : String, chapter : Int, verse : Int) = {
    println(s"getting by starting chapter: $book $chapter:$verse");
    val g : GraphTraversalSource = CassandraDb.graph
    val texts : GraphTraversal[Vertex, Vertex] = g.V().hasLabel("text")
      .has("starting_book", book)
      .has("starting_chapter", chapter)
      .has("starting_verse",  verse)

    texts
  }

  /*
   * not using overloaded functions for now, since I think there might be distinctive enough behavior for these different queries down the road, so just make them separate
   */
  def _fetchTextByStartingChapter (book : String, chapter : Int)  = {
    println(s"getting by starting chapter: $book $chapter");
    val g : GraphTraversalSource = CassandraDb.graph
    val texts : GraphTraversal[Vertex, Vertex] = g.V().hasLabel("text")
      .has("starting_book", book)
      .has("starting_chapter", chapter)

    texts
  }

  def _fetchTextByStartingBook (book : String)  = {
    println(s"getting by starting book: $book");
    val g : GraphTraversalSource = CassandraDb.graph

    val texts : GraphTraversal[Vertex, Vertex] = g.V().hasLabel("text")
      .has("starting_book", book)

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

  
  def _textsAlludeTo(alludingTexts : List[Vertex], hopsCount : Int)  = {
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
  def _textsAlludedToBy(sourceTexts : List[Vertex], hopsCount : Int)  = {
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
  def _findPathsForTraversal (traversal : GraphTraversal[Vertex, Vertex], valuesToReturn : Seq[String]) = {
    // get values from source and target vertices
    // destructure the arg to valueMap, will be like e.g., valueMap("id", "split_passages", "starting_book")
    // https://stackoverflow.com/a/1832288/6952495
    traversal.path().by(valueMap(valuesToReturn:_*))
  }

  ///////////////////////////////
  // helpers
  def _getEmptyTraversal() = {
    _getTextTraversal("non-dataset", "not-a-book", Some(99), Some(99))
  }
}
