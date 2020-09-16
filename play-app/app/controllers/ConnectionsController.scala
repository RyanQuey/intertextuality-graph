package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import constants.DatasetMetadata._
// import models.Connection._
import java.util.Collection

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class ConnectionsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

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
  def _findAllSourcesRecursivelyForRef (book : String, chapter : Int, verse : Int) : Iterator[Collection[String]]  = {
    // get the text
    val texts = g.V().has("text", "starting_book", book)
      .has("starting_chapter", chapter)
      .has("starting_verse",  verse)
      .next()
      //.valueMap(true) // can you also use this and get the properties even when you're going to traverse its edges later?
      //.limit(1)

    val connections = g.V(texts).                   // Iterator<Vertex>
			repeat(out("intertextual_connection")).times(4). // 4th degree of connections. TODO consider using map or flatmap in there, so gets their connectinos??
			dedup().                       // Remove duplicates
			//where(neq("u861")).            // Exclude u861   
			values("split_passages").              // Iterator<String>
			fold()                         // Iterator<Collection<String>>

    // TODO convert to json for sending to frontend
    connections
  }
}
