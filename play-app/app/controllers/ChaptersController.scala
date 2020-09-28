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
import com.ryanquey.intertextualitygraph.models.chapters.ChapterRecord
import com.ryanquey.intertextualitygraph.models.verses.VerseRecord

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
class ChaptersController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def findOne(bookName : String, number : Int) = Action { implicit request: Request[AnyContent] =>

    // retrieve one from the database
    // make an option, since sometimes returns null
    val book = Option(BookRecord.dao().findOne(bookName))

    // for now, chapter's 'book' field is actually osis. Probably good to switch from that eventually...
    // but in the meantime, get that osis first (requires typecasting serializable as string)
    val bookOsis = book match {
      // return as json
      case Some(b) => b.getOsisAbbreviation()
      // book was null, findOne did not find a record
      case None => ""
    }

    if (bookOsis == "") {
      InternalServerError(s"Could not find data for book '$bookName' when searching for chapter '$number'")
    }


    val chapter = Option(ChapterRecord.dao().findOne(bookOsis, number))
    val mapper : ObjectMapper = new ObjectMapper();
    // NOTE if null, make sure to run ./etl-tools/scripts/import-theographic-data.sh so that data gets imported into the db

    chapter match {
      // return as json
      case Some(c) => Ok(mapper.writeValueAsString(c))
      // book was null, findOne did not find a record
      case None => InternalServerError(s"Could not find data for book '$bookName' chapter '$number'")
    }
  }


  ///////////////////////////////
  // helpers
}
