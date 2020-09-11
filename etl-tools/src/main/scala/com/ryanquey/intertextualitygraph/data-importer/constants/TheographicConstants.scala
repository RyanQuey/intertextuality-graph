package com.ryanquey.intertextualitygraph.dataimporter.constants
import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers.Helpers
import scala.collection.immutable.Map

object TheographicConstants {
  /*
   * NOTE next time would probably make this three match case pattern matching funcs instead TODO. Fits scala better
   *
   * osisName│bookOrder│bookName│bookDiv│testament│shortName│slug│yearWritten│placeWritten│chapters│chapterCount│verseCount│writers│peopleCount│placeCount│modified
   */
  val booksFieldsMapping : Map[String, Map[String, String]] = Map(
    ("bookOrder", Map(("db_col", "book_order"))),
    ("osisName", Map(("db_col", "osis_abbreviation"))),
    ("slug", Map(("db_col", "slug"))),
    ("bookName", Map(("db_col", "name"))), 
    ("shortName", Map(("db_col", "theographic_short_name"))),
    ("testament", Map(("db_col", "testament"))),
    ("yearWritten", Map(("db_col", "year_written"))),
    ("chapterCount", Map(("db_col", "chapter_count"))),
    // TODO implement
    // writers > author. BUt will only take the first one
    ("writers", Map(
      ("db_col", "author"), 
    )),
    ("verseCount", Map(("db_col", "verse_count")))
  )
  // osisRef│book│chapterNum│writer│verses│slug│peopleCount│placesCount│modified│writer count
  val chaptersFieldsMapping : Map[String, Map[String, String]] = Map(
    ("osisRef", Map(("db_col", "osis_ref"))),
    ("book", Map(("db_col", "book"))), 
    ("chapterNum", Map(("db_col", "number"))),
    ("writer", Map(("db_col", "author"))),
    // TODO 
    // verses, just get the last one and extract the verse number
    ("verses", Map(("db_col", "verse_count")))
  )

  // osisRef│status│book│chapter│verseNum│verseText│mdText│people│peopleCount│places│placesCount│yearNum│quotesFrom│peopleGroups│eventsDescribed
  val versesFieldsMapping : Map[String, Map[String, String]] = Map(
    ("osisRef", Map(("db_col", "osis_ref"))),
    ("book", Map(("db_col", "book"))), 
    // note that they return a string e.g., Gen.1
    ("chapter", Map(("db_col", "chapter"))),
    ("verseNum", Map(("db_col", "number"))),
    ("verseText", Map(("db_col", "kjv_text"))),
    ("yearNum", Map(("db_col", "year_written")))
  )
} 
