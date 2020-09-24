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

import com.datastax.dse.driver.api.core.graph.DseGraph.g._;

import com.ryanquey.datautils.cassandraHelpers.CassandraDb
// the case class
import com.ryanquey.intertextualitygraph.modelhelpers.IntertextualConnection


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
import org.apache.tinkerpop.gremlin.structure.Direction._;
import org.apache.tinkerpop.gremlin.structure.T._;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__._;

// end over-importing tinkerpop classes

import org.apache.tinkerpop.gremlin.structure.Vertex
import gremlin.scala._
import org.apache.tinkerpop.gremlin.structure.io.graphson.{GraphSONMapper, GraphSONVersion}


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
class ConnectionsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /*
   * Define implicit conversions from Text / InterTextualConnection case class to json
   *
   * TODO 
   * - find a way to reuse the same implicit conversion throughout the different controllers
   * - will need to try after we get everything set up...for now just using graphson instead
   *
   *   Example from https://www.playframework.com/documentation/2.8.x/ScalaJsonHttp#Serving-a-list-of-entities-in-JSON
  implicit val locationWrites: Writes[Location] =
    (JsPath \ "lat").write[Double].and((JsPath \ "long").write[Double])(unlift(Location.unapply))

  implicit val placeWrites: Writes[Place] =
    (JsPath \ "name").write[String].and((JsPath \ "location").write[Location])(unlift(Place.unapply))

  implicit val connectionWrites: Writes[IntertextualConnection] = (JsPath \ "sourceTextStartingBook").write[String].and(
    (JsPath \ "sourceTextId").write[UUID]).and(
    (JsPath \ "alludingTextStartingBook").write[String]).and(
    (JsPath \ "alludingTextId").write[UUID]).and(
    (JsPath \ "connectionType").write[String]).and(
    (JsPath \ "confidenceLevel").write[Float]).and(
    (JsPath \ "updatedAt").write[Instant]).and(
    (JsPath \ "volumeLevel").write[Option[Float]]).and(
    (JsPath \ "userId").write[Option[UUID]]).and(
    (JsPath \ "bealeCategories").write[Option[List[String]]]).and(
    (JsPath \ "connectionSignificance").write[Option[String]]).and(
    (JsPath \ "comments").write[Option[String]]).and(
    (JsPath \ "sourceVersion").write[Option[String]]).and(
    (JsPath \ "sourceLanguage").write[Option[String]])(unlift(IntertextualConnection.unapply))





   */

  // only get one
  // By "source" I'm referring to a source text, since the assumption is that a given inter-textual connection is from an alluding text to a source text. 
  // Unfortunately, this is kind of confusing since it means that relative to graph terminology, the alluding text is the source node, and the source text is the target node.
  def findSourcesForRef(book : String, chapter : Option[Int], verse : Option[Int]) = Action { implicit request: Request[AnyContent] =>
    val sourceTexts = _findAllSourcesForRef(book, chapter, verse)

    Ok(_outputAllValuesForTraversal(sourceTexts))
  }


  def findSourcesForRefWithAlludingTexts(book : String, chapter : Option[Int], verse : Option[Int]) = Action { implicit request: Request[AnyContent] =>

    val textsWithValues = _getTextTraversal(book, chapter, verse)
      .valueMap() 
      .toList()

    val connectionsWithFields = _findAllSourcesForRef(book, chapter, verse)
      .valueMap() 
      .toList()

    // combine the two java lists
    textsWithValues.addAll(connectionsWithFields)
    val outputJson = json_mapper.writeValueAsString(textsWithValues)

    Ok(outputJson)
  }


  // for now requiring a separate API call, but maybe later we'll just merge these into the target vertices as well
  def findTextsByStartingRef(book : String, chapter : Option[Int], verse : Option[Int]) = Action { implicit request: Request[AnyContent] =>
    val texts = _getTextTraversal(book, chapter, verse)

    Ok(_outputAllValuesForTraversal(texts))
  }



  // gets paths for edges
  def findPathsForSourcesForRef(book : String, chapter : Option[Int], verse : Option[Int]) = Action { implicit request: Request[AnyContent] =>
    val texts = _getTextTraversal(book, chapter, verse)
      .toList()

    val connectionsWithFields = _findAllSourcesForRef(book, chapter, verse)

    val paths = _findPathsForTraversal(connectionsWithFields).toList
    val output = json_mapper.writeValueAsString(paths)

    Ok(output)
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
   */

  def _findAllSourcesForRef (book : String, chapter : Option[Int], verse : Option[Int])  = {
    val texts = _getTextTraversal(book, chapter, verse)
      .toList()

    val g : GraphTraversalSource = CassandraDb.graph
    val connectionsWithFields = g.V(texts).                
			repeat(out("intertextual_connection")).times(1)

    connectionsWithFields
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
    val traversal = chapter match {
      case Some(c) if verse.isDefined => _fetchTextByStartingVerse(book, c, verse.get)
      case Some(c) => _fetchTextByStartingChapter(book, c)
      case None => _fetchTextByStartingBook(book)
    }

    traversal
  }

  // NOTE returns traversal, doesn't actually hit the db yet until something is called on it
  def _fetchTextByStartingVerse (book : String, chapter : Int, verse : Int)  = {
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
    val g : GraphTraversalSource = CassandraDb.graph
    val texts : GraphTraversal[Vertex, Vertex] = g.V().hasLabel("text")
      .has("starting_book", book)
      .has("starting_chapter", chapter)

    texts
  }

  def _fetchTextByStartingBook (book : String)  = {
    val g : GraphTraversalSource = CassandraDb.graph
    val texts : GraphTraversal[Vertex, Vertex] = g.V().hasLabel("text")
      .has("starting_book", book)

    texts
  }



  //////////////////////////////////////////////////
  // output helpers
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
   *
   */
  def _findPathsForTraversal (traversal : GraphTraversal[Vertex, Vertex]) = {
    // get values from source and target vertices
    traversal.path().by(valueMap("id", "split_passages", "starting_book"))
  }


}
