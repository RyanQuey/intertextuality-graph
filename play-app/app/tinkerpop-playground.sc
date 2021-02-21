import scala.annotation.tailrec
// they don't like after 2.12
// import collection.JavaConverters._
import scala.jdk.CollectionConverters._
import javax.inject._
import java.time.Instant;
import java.util.{UUID};

import com.google.common.collect.{ImmutableList, Lists}
import com.fasterxml.jackson.databind.ObjectMapper;

import play.api.mvc._

import com.datastax.dse.driver.api.core.graph.DseGraph.g._;

// way overkill, but just trying to find what works for method "out"
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.process.traversal.{Path}
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Order.{asc}
import org.apache.tinkerpop.gremlin.process.traversal.P.{within, gte, lte}

import org.apache.tinkerpop.gremlin.structure.Column.{values};
//import org.apache.tinkerpop.gremlin.structure.T._;
//
import org.apache.tinkerpop.gremlin.structure.io.graphson.{GraphSONMapper, GraphSONVersion}

// I think you can do only one of the next two
import gremlin.scala._
//import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__._;
// end over-importing tinkerpop classes


// use to get the global session
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.datastax.dse.driver.api.core.graph.DseGraph.g._;
import com.ryanquey.intertextualitygraph.models.texts.{Text => TextClass}
import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.books.BookRecord
import com.ryanquey.intertextualitygraph.modelhelpers.TextHelpers

import com.ryanquey.intertextualitygraph.graphmodels.TextVertex
import com.ryanquey.intertextualitygraph.utils.JswordUtil.{
  osisToStartingChapterReference,
  osisToStartingVerseReference,
}
import com.ryanquey.intertextualitygraph.reference.{BookReference, ChapterReference, VerseReference}
import com.ryanquey.intertextualitygraph.reference.{
  VerseRangeWithinChapter,
  ChapterRangeWithinBook,
}

// import models.Connection._
import constants.DatasetMetadata._



val g : GraphTraversalSource = CassandraDb.graph
g.V().hasLabel("verse")
  .has("book", verseRange.book.name)
  .has("chapter", verseRange.chapter.number)
  .where(
    values("number").is(
      // TODO check the parentheses on this one, I think it might be wrong
      gte(verseRange.startingVerse.number)
        .and(lte(verseRange.endingVerse.number))
    )
  ).toList