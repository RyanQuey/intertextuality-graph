package com.ryanquey.intertextualitygraph.reference
import org.crosswire.jsword.passage.VerseRange
import org.crosswire.jsword.passage.{Verse => JswordVerse}
import com.ryanquey.intertextualitygraph.graphmodels.VerseVertex
import com.ryanquey.intertextualitygraph.utils.JswordUtil._


/*
 * represents a single reference to a specific verse, e.g., Rom 1.1
 * maybe will make a trait that BookVertex can inhereit from ??
 */ 
case class VerseReference(
  book : String, // TODO maybe make this a BookReference? or just make a helper that does that. 
  chapter : Int, 
  number : Int, 
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

    def getChapterReference () = {ChapterReference(book, chapter)}
    def getBookReference () = {BookReference(book)}
    def getLastVerseOfChapter () : VerseReference = {getChapterReference.getLastVerse}
    def getLastVerseOfBook () : VerseReference = {getBookReference.getLastVerse}

    /*
     * - could use jsword and osis for this as well, TODO...?
     */ 
    def isSameAs (otherVerse : VerseReference) : Boolean = {
      otherVerse.number == number && 
      otherVerse.chapter == chapter && 
      otherVerse.book == book
    } 

    /*
     * - could use jsword and osis for this as well, TODO...?
     */ 
    def isSameChapterAs (otherVerse : VerseReference) : Boolean = {
      otherVerse.chapter == chapter && 
      otherVerse.book == book
    } 

    def isStartOfBook () : Boolean = {chapter == 1 && number == 1}
  }

object VerseReference {
  def apply(refRange : ReferenceRange) = {
    new VerseReference(
      refRange.getStartingBookName,
      refRange.getStartingChapterNumber,
      refRange.getStartingVerseNumber
      )

  }
  def apply(verse : JswordVerse) = {
    new VerseReference(
      osisToStartingBookName(verse.getOsisRef),
      verse.getChapter,
      verse.getVerse
      )

  }
}
