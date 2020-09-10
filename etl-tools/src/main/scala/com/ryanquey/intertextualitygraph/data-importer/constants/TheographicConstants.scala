package com.ryanquey.intertextualitygraph.dataimporter.constants
import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers.Helpers
import scala.collection.immutable.Map

object TheographicConstants {
  /*
   * writers > author. BUt will only take the first one
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
    ("writers", Map(
      ("db_col", "author"), 
    )),
    ("verseCount", Map(("db_col", "verse_count")))
  )
  // TODO
  val chaptersFieldsMapping : Map[String, Map[String, String]] = Map(
    ("yearWritten", Map(("db_col", "year_written")))
  )
  val versesFieldsMapping : Map[String, Map[String, String]] = Map(
    ("yearWritten", Map(("db_col", "year_written")))
  )
} 
