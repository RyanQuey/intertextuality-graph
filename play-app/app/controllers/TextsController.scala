package controllers

// https://www.playframework.com/documentation/2.8.x/ScalaJson
import play.api.libs.json._
import play.api.libs.functional.syntax._

import javax.inject._
import play.api._
import play.api.mvc._
import constants.DatasetMetadata._

import scala.jdk.CollectionConverters._


// import gremlin.scala._

import com.datastax.dse.driver.api.core.graph.DseGraph.g._;

import models.TextAPIModel._
import models.HopParamsSet

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 *
 */
class TextsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  // implicit converter from JsValue to HopParamsSet
  // TODO maybe try putting this in the model class file?
  /*
   * this is not working, something with the apply._ thing
   *
   *    identifier expected but '_' found.
   *
   * but don't need it, just use default converter 
   *
  implicit val hopParamsSetReads : Reads[HopParamsSet] = (
    (JsPath \ "referenceOsis").read[String] and 
    (JsPath \ "allusionDirection").read[String] and
    (JsPath \ "dataSet").read[String])(HopParamsSet.apply._)
  */

 // see comment of https://stackoverflow.com/a/25194037/6952495, works if keys line up with case class fields
  implicit val hopParamsSetReads : Reads[HopParamsSet] = Json.reads[HopParamsSet]


  /* 
   * take an array of json objects, each object representing a "hop". Each hop has its own filters
   * - In reality, the first top is not a hop, but the starting text. But should still be sent in the same format as the hops, since it is the same in what kind of parameters it will have
   * - https://www.playframework.com/documentation/2.8.x/ScalaJsonHttp#Creating-a-new-entity-instance-in-JSON
  */ 
  def tracePathsFilteredByHop = Action(parse.json) { request =>

    // expecting an array here
    val hopParamSetsJSON : JsArray = (request.body \ "hopParamsSets").get.as[JsArray]

    // typecast to Seq
    // https://stackoverflow.com/a/25194037/6952495
    val hopParamSets = hopParamSetsJSON.as[Seq[HopParamsSet]]

    val traversal = hopParamSetsToTraversal(hopParamSets)

    // TODO
    //if (sourceTexts.size == 0) {
    if (false) {
      Ok("[]")
      //Ok(request.body)
    } else {

      // maybe can skip
      // passing in empty Seq for getting ALL fields
      val pathsWithValues = findPathsForTraversal(traversal, Seq()).toList

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

    val sourceTexts = getTextTraversal(dataSet, book, chapter, verse)
      .toList()

    if (sourceTexts.size == 0) {
      Ok("[]")
    } else {

      // find what texts allude to the texts we found
      val alludingTexts = textsAlludedToBy(sourceTexts, hopsCount)

      // passing in no args for getting ALL fields
      val pathsWithValues = findPathsForTraversal(alludingTexts, Seq()).toList

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
    
    
    val alludingTexts = getTextTraversal(dataSet, book, chapter, verse)
      .toList()

    if (alludingTexts.size == 0) {
      Ok("[]")
    } else {

      // TODO NOTE I'm actually not sure if this finds the source text or alluding text...
      val sourceTexts = textsAlludeTo(alludingTexts, hopsCount)

      // passing in no args for getting ALL fields
      val pathsWithValues = findPathsForTraversal(sourceTexts, Seq()).toList

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
   * (not really using this right now)
  // for now requiring a separate API call, but maybe later we'll just merge these into the target vertices as well
  */
  def findTextsByStartingRef(dataSet : String, book : String, chapter : Option[Int], verse : Option[Int]) = Action { implicit request: Request[AnyContent] =>
    val texts = getTextTraversal(dataSet, book, chapter, verse)
      .valueMap() 
      .toList()

    val outputJson = json_mapper.writeValueAsString(texts)

    Ok(outputJson)
  }

}
