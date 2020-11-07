package com.ryanquey.intertextualitygraph.graphmodels

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import scala.collection.JavaConverters._ 
import com.ryanquey.datautils.cassandraHelpers.CassandraDb

import java.util.UUID;
import java.time.Instant;
import com.datastax.oss.driver.api.core.cql._;

/*
 * - I put the CQL data type to the right of the field definition
 * - NOTE make sure to keep fields in sync with com.ryanquey.intertextualitygraph.models.chapters.VerseBase
 */
case class VerseVertex(
  number : Integer,  // INT
  yearWritten : Integer,  // INT 
  author : String,  // TEXT
  osisRef : String,  // TEXT 
  scrollmapperId : String,  // TEXT 
  canonical : Boolean,  // BOOLEAN 
  canonicalText : String,  // TEXT
  kjvText : String,  // TEXT
  mtText : String,  // TEXT
  rahlfsLxxText : String,  // TEXT
  sblGntText : String,  // TEXT
  byzGntText : String,  // TEXT
  chapter : Integer,  // INT 
  book : String,  // TEXT 
  bookSeries : String,  // TEXT 
  testament : String,  // TEXT 
  comments : String,  // TEXT
  updatedAt : Instant // TIMESTAMP, 
  )

object VerseVertex {
  /*
   * overloading the apply method so we can instantiate the case class using the corresponding Java model class
   * - https://stackoverflow.com/a/2401153/6952495
   */
  def apply(javabean : Verse) = {
    new VerseVertex(
      javabean.getNumber(),
      javabean.getYearWritten(),
      javabean.getAuthor(),
      javabean.getOsisRef(),
      javabean.getScrollmapperId(),
      javabean.getCanonical(),
      javabean.getCanonicalText(),
      javabean.getKjvText(),
      javabean.getMtText(),
      javabean.getRahlfsLxxText(),
      javabean.getSblGntText(),
      javabean.getByzGntText(),
      javabean.getChapter(),
      javabean.getBook(),
      javabean.getBookSeries(),
      javabean.getTestament(),
      javabean.getComments(),
      javabean.getUpdatedAt(),
      )
  }

}
