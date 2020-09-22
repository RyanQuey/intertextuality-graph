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
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Operator._;
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
   *
   *   Example from https://www.playframework.com/documentation/2.8.x/ScalaJsonHttp#Serving-a-list-of-entities-in-JSON
  implicit val locationWrites: Writes[Location] =
    (JsPath \ "lat").write[Double].and((JsPath \ "long").write[Double])(unlift(Location.unapply))

  implicit val placeWrites: Writes[Place] =
    (JsPath \ "name").write[String].and((JsPath \ "location").write[Location])(unlift(Place.unapply))
   */

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



  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def findAllSourcesRecursivelyForRef() = Action { implicit request: Request[AnyContent] =>
    Ok(_findAllSourcesRecursivelyForRef("Genesis", 1, 1))
  }

  /**
   * http://www.doanduyhai.com/blog/?p=13301
   *
   * so far just making this for a single reference, but eventually will probably make another one for if there is a starting and ending reference
   * TODO might move the gremlin queries themselves to the etl-tools model-helpers
   *
   * https://docs.datastax.com/en/developer/java-driver/4.9/manual/core/dse/graph/
   */
  def _findAllSourcesRecursivelyForRef (book : String, chapter : Int, verse : Int)  = {
    val g : GraphTraversalSource = CassandraDb.g

    // get the text
    val texts = g.V().has("text", "starting_book", book)
      .has("starting_chapter", chapter)
      .has("starting_verse",  verse)
      //.next() // just get first hit
      // .valueMap(true) // can you also use this and get the properties even when you're going to traverse its edges later? Also can't use valueMap with next, it is one or the other
      //.limit(1)
      .toList()



      // I think we will often need to typecast to make sure we get the right format here. maybe scala dsl won't need it
    val connections = g.V(texts).                   // Iterator<Vertex>
			repeat(out("intertextual_connection")).times(4). // 4th degree of connections. TODO consider using map or flatmap in there, so gets their connectinos??
			dedup().                       // Remove duplicates
			//where(neq("u861")).            // Exclude u861   
			values("split_passages"). // starting simple; will use scala dsl later
      // valueMap(true). // can you also use this and get the properties even when you're going to traverse its edges later?
    // currently List[java.util.Map[Object,Nothing]] when use valueMap
			toList().asScala 

    // https://www.baeldung.com/java-convert-iterator-to-list#guava
    // getting the wrong type, having a hard time converting to list

    // convert to json for sending to frontend
    // TODO might need to convert from Iterator<Collection<String>> to something that toJson can handle
    // Cannot write an instance of Iterator[java.util.Collection[String]] to HTTP response. Try to define a Writeable[Iterator[java.util.Collection[String]]]]
    val connectionJson = Json.toJson(connections)
    // val textJson = Json.toJson(text)
    connectionJson
  }
}
