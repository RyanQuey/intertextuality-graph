package com.ryanquey.intertextualitygraph.reference
import org.crosswire.jsword.passage.VerseRange
import org.crosswire.jsword.passage.{Verse => JswordVerse}
import com.ryanquey.intertextualitygraph.graphmodels.{BookVertex, ChapterVertex, VerseVertex}
import com.ryanquey.intertextualitygraph.utils.JswordUtil._


/*
 * represents a single reference to a specific verse, e.g., Rom 1.1
 * maybe will make a trait that BookVertex can inhereit from ??
 */ 
case class VerseReference(
  book : String, // TODO maybe make this a BookReference? or just make a helper that does that. 
  chapter : Int, 
  number : Int, 
  // TODO can add helpers that set these later
  // testament : String, // TEXT 
  // canonical : Boolean, // BOOLEAN 
  // bookOrder : Integer, // INT
  // osisAbbreviation : String, // TEXT

  // TODO can add helpers that set these later
  //chapterCount : Integer, // INT
  //verseCount : Integer, // INT
  //bookSeries : Option[String] = None, // TEXT 
  ) {

    def getChapterReference () = {ChapterReference(book, chapter)}
    def getPreviousChapterReference () = {ChapterReference(book, chapter -1)}
    def getNextChapterReference () = {ChapterReference(book, chapter +1)}
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
    def isSameBookAs (otherVerse : VerseReference) : Boolean = {
      otherVerse.book == book
    } 

    def isAfter (otherVerse : VerseReference) : Boolean = {
      BookVertex.getBookByName(otherVerse.book).bookOrder.get >= BookVertex.getBookByName(this.book).bookOrder.get &&
      otherVerse.chapter >= this.chapter && 
      otherVerse.number > this.chapter
    }

    def isStartOfBook () : Boolean = {chapter == 1 && number == 1}
    def isStartOfChapter () : Boolean = {number == 1}
    def isLastVerseOfChapter () : Boolean = {
      this.isSameAs(this.getLastVerseOfChapter)
    }
    def isLastVerseOfBook () : Boolean = {
      this.isSameAs(this.getLastVerseOfBook)
    }

    
    /*
     * returns chapter range from this verse to 
     * - requires (and confirms) terminalVerse is after this verse
     * - only need to do within same book, since that is what we use in our gremlin queries currently
     */ 
    def getFullChaptersWithinBookUntil (terminalVerse : VerseReference) : ChapterRangeWithinBook = {
      // throw runtime error here, but hopefully it reveals business logic failure that is easy to fix before pushing stuff to prod
      if (isAfter(terminalVerse)) {
        throw new Exception("terminal needs to be after this verse!")
      }
      if (!isStartOfChapter) {
        // TODO can just use this.getNextChapterReference instead of this and not throw an exception here, but for now use case is only to use this if this verse is start of ch
        throw new Exception("verse needs to be start of chapter!")
      }

      if (this.isSameBookAs(terminalVerse)) {
        // get all full chapters to terminalVerse

        if (terminalVerse.isLastVerseOfChapter) {
          // can get all full chapters through the chapter of terminalVerse
          ChapterRangeWithinBook(this.getChapterReference, terminalVerse.getChapterReference)
        } else {
          // can get all full chapters before the chapter of terminalVerse
          ChapterRangeWithinBook(this.getChapterReference, terminalVerse.getPreviousChapterReference)
        }

      } else {
        // get all chapters to end of book
        ChapterRangeWithinBook(this.getChapterReference, this.getLastVerseOfBook.getChapterReference)
      }
    }
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
