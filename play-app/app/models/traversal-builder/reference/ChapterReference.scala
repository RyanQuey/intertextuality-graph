package models.traversalbuilder.reference
import org.crosswire.jsword.passage.VerseRange


import com.ryanquey.intertextualitygraph.utils.JswordUtil._


/*
 * represents a single reference to a specific chapter, e.g., Rom 1
 * maybe will make a trait that BookVertex can inhereit from ??
 */ 
case class ChapterReference(
  number : Int, 
  book : String, // TEXT 
  // TODO can add heplers that set these later
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
      val lastVerseNumber = ChapterVertex.getChapterByRefData(book, number).verseCount
      VerseReference(book, number, lastVerseNumber)
    }

  }
