package com.ryanquey.intertextualitygraph.graphmodels

//import scala.collection.JavaConverters._ 
import scala.jdk.CollectionConverters._
import java.util.UUID;
import java.time.Instant;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex

import com.datastax.oss.driver.api.core.cql._;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import com.ryanquey.intertextualitygraph.modelhelpers.BookHelpers
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

import com.ryanquey.datautils.models.{Model, Record}
import com.ryanquey.intertextualitygraph.helpers.shapeless.{CaseClassFromMap}
import com.ryanquey.datautils.helpers.StringHelpers._;
import com.ryanquey.intertextualitygraph.helpers.Reflection.{fromMap, getFieldsOfTypeForClass}

import scala.reflect._
import scala.reflect.runtime.universe._

/*
 * - NOTE make sure to keep fields in sync with com.ryanquey.intertextualitygraph.models.chapters.BookBase
 */
case class BookVertex(
  name : String, // TEXT 
  chapterCount : Integer, // INT
  updatedAt : Instant, // TIMESTAMP, 
  testament : String, // TEXT 
  canonical : Boolean, // BOOLEAN 

  bookOrder : Option[Integer] = None, // INT
  bookSeries : Option[String] = None, // TEXT 
  yearWritten : Option[Integer] = None, // INT
  placeWritten : Option[String] = None, // TEXT 
  author : Option[String] = None, // TEXT
  verseCount : Option[Integer] = None, // INT
  tyndaleAbbreviation : Option[String] = None, // TEXT
  osisAbbreviation : Option[String] = None, // TEXT
  slug : Option[String] = None, // TEXT
  theographicShortName : Option[String] = None, // TEXT
  scrollmapperId : Option[String] = None, // TEXT 
  comments : Option[String] = None, // TEXT
) extends GraphReferenceVertex[BookVertex] {
    def companionObject = BookVertex

  /*
   *
   * - needs to maintain order, since we will pass in primary key in order sometimes (C* generally requires knowing the primary key in order). So use a list, not set
   */ 
  def getPrimaryKey() = {
    List(
      this.name,
      )
  }

}

object BookVertex extends GraphReferenceVertexCompanion[BookVertex] {
  /*
   * overloading the apply method so we can instantiate the case class using the corresponding Java model class
   * - https://stackoverflow.com/a/2401153/6952495
   */
  def apply(javabean : Book) = {
    new BookVertex(
      name = javabean.getName,
      chapterCount = javabean.getChapterCount,
      updatedAt = javabean.getUpdatedAt,
      testament = javabean.getTestament,
      canonical = javabean.getCanonical,

      bookOrder = Option(javabean.getBookOrder),
      bookSeries = Option(javabean.getBookSeries),
      yearWritten = Option(javabean.getYearWritten),
      placeWritten = Option(javabean.getPlaceWritten),
      author = Option(javabean.getAuthor),
      verseCount = Option(javabean.getVerseCount),
      tyndaleAbbreviation = Option(javabean.getTyndaleAbbreviation),
      osisAbbreviation = Option(javabean.getOsisAbbreviation),
      slug = Option(javabean.getSlug),
      theographicShortName = Option(javabean.getTheographicShortName),
      scrollmapperId = Option(javabean.getScrollmapperId),
      comments = Option(javabean.getComments),
      )
  }

  def convertJavabeanModelInstances(javabeanModelInstances : Iterable[Model]) : Iterable[BookVertex] = {
    javabeanModelInstances.map((instance) => {
      // BookVertex requires Book type as arg 
      val typecastedInstance : Book = instance.asInstanceOf[Book]

      BookVertex(typecastedInstance)
    })
  }

  def preparedValueMapToCaseClass(preparedValueMap: Map[String, Any]) : BookVertex = {
     //CaseClassFromMap[BookVertex](preparedValueMap)
     fromMap[BookVertex](preparedValueMap) 
  }

  /*
   * NOTE WARNING: Prone to runtime errors if this is off, even by one. Need to unit test all of these
   */ 
  def getOptionalFields() = {
    Set(
      "bookOrder",
      "bookSeries",
      "yearWritten",
      "placeWritten",
      "author",
      "verseCount",
      "tyndaleAbbreviation",
      "osisAbbreviation",
      "slug",
      "theographicShortName",
      "comments",
      "scrollmapperId"
    )
  }

  /*
   *
   * - needs to maintain order, since we will pass in primary key in order sometimes (C* generally requires knowing the primary key in order). So use a list, not set
   */ 
  def getPrimaryKeyFields() = {
    List(
      "name",
      )
  }

  def getFieldsOfType[T: TypeTag: ClassTag] () : List[String] = getFieldsOfTypeForClass[BookVertex, T]

  ///////////////////////////////////////////////////////////
  // METADATA HELPERS
  // TODO move away from these to jsword helpers when possible
  // also move a lot of this to JswordUtil instead, so this kind of metadata finding all belongs in one spot
  // - TODO or better yet, make a trait that is a book, and has some of the same fields as BookVertex, 
  ///////////////////////////////////////////////////////////

  /*
   * for now, just make thin wrapper around what we have already with BookHelpers
   * - a little bit of performance overhead, because has to iterate over everything again to convert into case classes
   * - if it becomes a performance issue, can have TheographicDataFile do something to instantiate as case classes directly instead
   */
  val allBooksFromFile = convertJavabeanModelInstances(BookHelpers.allBooksFromFile)

  /*
   * retrieve book case class instance using standard name
   * - uses the same name that we use as db primary keys
   */ 
  def getBookByName (bookName : String) : BookVertex = {
    allBooksFromFile.find((b) => b.name == bookName).get
  }

  /*
   * taking a model instance of Chapter, returning book model instance
   *
   */
  def getBookForChapter (chapter : ChapterVertex) : BookVertex = {
    getBookByName(chapter.book) 
  }

  def getBookByOsis (osisName : String) : BookVertex = {
    allBooksFromFile.find((b) => b.osisAbbreviation.get == osisName).get
  }

  /*
   * retrieve book java model instances using for ALL books between the two provided, inclusive
   * - assumes English Bible book ordering
   * - assumes books that are in an order. TODO for extra testamental books, will have to add a conditional where if no order, just returns the start and end book (which should be the same book)
   */ 
  def getBooksBetween (startingBook : BookVertex, endingBook : BookVertex) : Iterable[BookVertex] = {
    allBooksFromFile.filter((b) => b.bookOrder.get >= startingBook.bookOrder.get && b.bookOrder.get <= endingBook.bookOrder.get)
  }
  def getBooksBetween (startingBookName : String, endingBookName : String) : Iterable[BookVertex] = {
    getBooksBetween(getBookByName(startingBookName), getBookByName(endingBookName))
  }

  ///////////////////////////////////////////////////////////
  // GRAPH HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * 
   * There should only be one result, but rather than returning a Vertex, just return the traversal, so can continue traversing off of it without hitting the database yet
   * - can call `next()` on the return value to get a Vertex (I think...)
   */
  //def buildVertexTraversalFromPK (bookName : String) : GraphTraversal[Vertex, Vertex] = {
  def buildVertexTraversalFromPK (pk : List[Any]) : GraphTraversal[Vertex, Vertex] = {
    val g : GraphTraversalSource = CassandraDb.graph
    val bookName : String = pk(0).asInstanceOf[String]
    println(s"Get vertex for book ${bookName}");
    val traversal = g.V().hasLabel("book")
      .has("name", bookName)

    traversal
  }

}

