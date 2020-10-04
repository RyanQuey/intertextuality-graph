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
import java.util.{UUID, Collection};
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
 * TODO I hate how I did all of this, once I can successfully switch over to new route, just throw away all the old ones, just getting too messy. Then rename helpers, either always by Gremlin terminology, or from a high level what I'm doing, but make it clear
 */
class TextsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {


  // only get one
  // By "source" I'm referring to a source text, since the assumption is that a given inter-textual connection is from an alluding text to a source text. 
  // Unfortunately, this is kind of confusing since it means that relative to graph terminology, the alluding text is the source node, and the source text is the target node.
  def findSourcesForRef(book : String, chapter : Option[Int], verse : Option[Int], hopsCount : Int) = Action { implicit request: Request[AnyContent] =>
    val sourceTexts = _findAllSourcesForRef(book, chapter, verse, hopsCount)

    Ok(_outputAllValuesForTraversal(sourceTexts))
  }


  def findSourcesForRefWithAlludingTexts(book : String, chapter : Option[Int], verse : Option[Int], hopsCount : Int) = Action { implicit request: Request[AnyContent] =>

    val textsWithValues = _getTextTraversal(book, chapter, verse)
      .valueMap() 
      .toList()

    // be sure not to continue if size is 0. just return an empty array as our dummy json. Note that passing in null as vertices in g.V(null) will mean get all...
    if (textsWithValues.size == 0) {
      Ok("[]")

    } else {
      val connectionsWithFields = _findAllSourcesForRef(book, chapter, verse, hopsCount)
        .valueMap() 
        .toList()

      println(s"Returning sources with alluding texts - source texts: (${textsWithValues.size}); alluding texts: (${connectionsWithFields.size})");
      // combine the two java lists
      textsWithValues.addAll(connectionsWithFields)
      val outputJson = json_mapper.writeValueAsString(textsWithValues)

      Ok(outputJson)
    }
  }


  // for now requiring a separate API call, but maybe later we'll just merge these into the target vertices as well
  def findTextsByStartingRef(book : String, chapter : Option[Int], verse : Option[Int]) = Action { implicit request: Request[AnyContent] =>
    val texts = _getTextTraversal(book, chapter, verse)

    Ok(_outputAllValuesForTraversal(texts))
  }

  // gets paths for edges
  // returns only "id", "split_passages", "starting_book" (as specified by _findPathsForTraversal)
  def findPathsForSourcesForRef(book : String, chapter : Option[Int], verse : Option[Int], hopsCount : Int) = Action { implicit request: Request[AnyContent] =>
    val texts = _getTextTraversal(book, chapter, verse)
      .toList()

    if (texts.size == 0) {
      Ok("[]")
    } else {
      val connectionsWithFields = _findAllSourcesForRef(book, chapter, verse, hopsCount)

      val valuesToReturn = Seq("id", "split_passages", "starting_book")
      val paths = _findPathsForTraversal(connectionsWithFields, valuesToReturn).toList
      // turn into json
      val output = json_mapper.writeValueAsString(paths)

      Ok(output)
    }
  }

  // gets all values for all vertices along path
  def findAllValuesAlongPathForRef(book : String, chapter : Option[Int], verse : Option[Int], hopsCount : Int) = Action { implicit request: Request[AnyContent] =>
    // TODO I think this is redundant, since _findAllSourcesForRef checks also
    val texts = _getTextTraversal(book, chapter, verse)
      .toList()

    if (texts.size == 0) {
      Ok("[]")
    } else {

      val connectionsWithFields = _findAllSourcesForRef(book, chapter, verse, hopsCount)

      // passing in no args for 
      val pathsWithValues = _findPathsForTraversal(connectionsWithFields, Seq()).toList

      // now we have gremlin output, that is roughly a list of lists of maps, and each map is a vertex with all values attached. 
      // we want to return this as two data items for use with our chart, one for nodes, one for edges
      // TODO convert stuff using scala; for now just sending to frontend and converting using js
      // possibly use gremlin-scala?
      pathsWithValues.asScala.foreach{ pathWithValues => {
        println(pathWithValues.getClass)
      }}

      val output = json_mapper.writeValueAsString(pathsWithValues)

      Ok(output)
    }
  }


  //////////////////////////////////////////////////
  // graph traversals

  /**
   * http://www.doanduyhai.com/blog/?p=13301
   *
   * so far just making this for a single reference, but eventually will probably make another one for if there is a starting and ending reference
   * TODO might move the gremlin queries themselves to the etl-tools model-helpers
   *
   * https://docs.datastax.com/en/developer/java-driver/4.9/manual/core/dse/graph/
   *
   * @param hopsCount how many times to go out on the intertextual_connection edge
   */

  def _findAllSourcesForRef (book : String, chapter : Option[Int], verse : Option[Int], hopsCount : Int)  = {
    val g : GraphTraversalSource = CassandraDb.graph
    val texts = _getTextTraversal(book, chapter, verse).toList()

    // be sure not to continue if size is 0. just return an empty traversal if empty. Since doing g.V([]) will return all vertices...
    if (texts.size == 0) {
      // this is hacky, but just a sure fire way to return a traversal (for the sake of type casting) with no results, so what we return matches type that this method is expected to return TODO better implementation
      _getEmptyTraversal()

    } else {
      val connectionsWithFields = g.V(texts).                
        repeat(out("intertextual_connection")).times(hopsCount)

      connectionsWithFields
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
  def _getTextTraversal (book : String, chapter : Option[Int], verse : Option[Int]) = {
    println("getting texts");
    val traversal = chapter match {
      case Some(c) if verse.isDefined => _fetchTextByStartingVerse(book, c, verse.get)
      case Some(c) => _fetchTextByStartingChapter(book, c)
      case None => _fetchTextByStartingBook(book)
    }

    traversal
  }

  // NOTE returns traversal, doesn't actually hit the db yet until something is called on it
  def _fetchTextByStartingVerse (book : String, chapter : Int, verse : Int)  = {
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
  // output helpers

  // takes Java list of graph traversals or other graph class instances and converts to json
  val json_mapper = GraphSONMapper.
                build().
                version(GraphSONVersion.V1_0).
                create().
                createMapper()


  /*
   * Note that you don't always want to use this, often you'll want to specify what values you want
   * This returns all the values
   *
   *
   * Note that `Any` is probably too broad. So far, Vertex or Path
   */ 

  def _outputAllValuesForTraversal (traversal : GraphTraversal[_, _]) : String = {
    val hitsList = traversal
      .valueMap() 
      .toList()

    val outputJson = json_mapper.writeValueAsString(hitsList)

    outputJson
  }

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
    _getTextTraversal("not-a-book", Some(99), Some(99))
  }
}
