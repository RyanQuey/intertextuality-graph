package com.ryanquey.intertextualitygraph.reference

import org.crosswire.jsword.passage.VerseRange
import com.ryanquey.intertextualitygraph.utils.JswordUtil._
import com.ryanquey.intertextualitygraph.graphmodels.{BookVertex, ChapterVertex}


/*
 * represents a single reference that spans a book
 * maybe will make a trait that BookVertex can inhereit from ??
 */ 
case class BookReference(
  name : String, // TEXT 
  // testament : String, // TEXT 
  // canonical : Boolean, // BOOLEAN 
  // bookOrder : Integer, // INT
  // osisAbbreviation : String, // TEXT

  // TODO can add heplers that set these later
  //chapterCount : Integer, // INT
  //verseCount : Integer, // INT
  //bookSeries : Option[String] = None, // TEXT 
  ) {
    def getLastVerse () = {
      // when we implement VerseCount on ChapterReference  case class, won't have to get it from ChapterVertex
      val lastChapterNumber = BookVertex.getBookByName(name).chapterCount
      val lastVerseNumber = ChapterVertex.getChapterByRefData(name, lastChapterNumber).verseCount

      VerseReference(name, lastChapterNumber, lastVerseNumber)
    }

  }
