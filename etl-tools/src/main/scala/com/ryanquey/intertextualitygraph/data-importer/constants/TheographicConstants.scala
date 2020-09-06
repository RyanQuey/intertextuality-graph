package com.ryanquey.intertextualitygraph.dataimporter.constants
import scala.collection.immutable.Map

object TheographicConstants {
  /*
   * writers > author. BUt will only take the first one
   */
  val booksFieldsMapping = Map(
    ("bookOrder", "book_order"),
    ("osisName", "osis_abbreviation"),
    ("slug", "slug"),
    ("bookName", "name"), 
    ("shortName", "theographic_short_name"),
    ("testament", "testament"),
    ("yearWritten", "year_written"),
    ("chapterCount", "chapter_count"),
    ("writers", (writers : Array[String]) => writers(0)),
    ("verseCount", "verse_count")
  )
  // TODO
  val chaptersFieldsMapping = Map(
  )
  val versesFieldsMapping = Map(
  )
} 
