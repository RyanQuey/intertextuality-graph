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
import com.ryanquey.intertextualitygraph.graphmodels.IntertextualConnectionEdge
import com.ryanquey.intertextualitygraph.modelhelpers.IntertextualConnectionsHelpers
import com.ryanquey.intertextualitygraph.modelhelpers.BookHelpers.{getBookByOsis}
import com.ryanquey.intertextualitygraph.modelhelpers.TextHelpers
import com.ryanquey.intertextualitygraph.models.texts.Text


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


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
class ConnectionsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /*
   * Define implicit conversions from Text / InterTextualConnectionEdge case class to json
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

  implicit val connectionWrites: Writes[IntertextualConnectionEdge] = (JsPath \ "sourceTextStartingBook").write[String].and(
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
    (JsPath \ "sourceLanguage").write[Option[String]])(unlift(IntertextualConnectionEdge.unapply))

   */

  /*
   * create a new connection between two texts
   * - If the texts do not exist, create new texts as well
   */ 
  def create() = Action(parse.json) { request =>
    val connectionData : JsValue = request.body
    println(connectionData.toString)
    // find or create vertices

    // https://www.playframework.com/documentation/2.8.x/ScalaJson#Traversing-a-JsValue-structure
    val sourceTextData : JsValue = (connectionData \ "sourceText").get
    // could be a single ref, or a range (e.g., Gen1.1-3.6)
    val srcOsis = (sourceTextData \ "parsed" \ 0 \ "osis").as[String]
    // is a java bean, not a case class
    println(srcOsis)

    val sourceText = new Text()
    if (srcOsis != "") {
      TextHelpers.populateFieldsfromOsis(srcOsis, sourceText)
    }

    val alludingTextData : JsValue = (connectionData \ "alludingText").get
    // could be a single ref, or a range (e.g., Gen1.1-3.6)
    val alludingOsis = (alludingTextData \ "parsed" \ 0 \ "osis").as[String]
    // is a java bean, not a case class
    println(alludingOsis)

    val alludingText = new Text()
    if (alludingOsis != "") {
      TextHelpers.populateFieldsfromOsis(alludingOsis, alludingText)
    }

    sourceText.setCreatedBy("api-endpoint")
    sourceText.setUpdatedBy("api-endpoint")
    alludingText.setCreatedBy("api-endpoint")
    alludingText.setUpdatedBy("api-endpoint")

    // also filters by createdBy
    TextHelpers.updateOrCreateByRef(alludingText)
    TextHelpers.updateOrCreateByRef(sourceText)

    val connectionConfidenceLevel = connectionData("confidenceLevel").as[Float]

    val connection : IntertextualConnectionEdge = IntertextualConnectionsHelpers.connectTexts(sourceText, alludingText, "user-created", connectionConfidenceLevel)
    // https://www.playframework.com/documentation/2.8.x/ScalaJson#JsValue-to-a-model

    // find or create edge
		Ok(Json.obj("message" -> (" '" + connection + "' saved.")))
  }




  // return payload back
  //
  def test() = Action(parse.json) { request =>
    val connectionData = request.body
    println(connectionData)
		Ok(Json.obj("message" -> ("Play API received '" + connectionData)))
  }

}
