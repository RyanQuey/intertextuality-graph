package com.ryanquey.intertextualitygraph.utils

import org.crosswire.jsword.passage.OsisParser;
import org.crosswire.jsword.passage.VerseRange;
import org.crosswire.jsword.versification.Versification;
import org.crosswire.jsword.versification.system.Versifications;
import org.crosswire.jsword.passage.{Verse => JswordVerse}
import org.crosswire.jsword.book.Books;
import org.crosswire.jsword.versification.BibleNames
import org.crosswire.jsword.versification.BibleBook

import com.ryanquey.intertextualitygraph.graphmodels.BookVertex
import com.ryanquey.intertextualitygraph.reference.{BookReference, ChapterReference, VerseReference, ReferenceRange}

/*
 * Thin wrapper around jsword stuff
 * - we can say that one goal is to keep all jsword imports out of other files...or almost all
 * - that way, all direct interation with jsword goes here, but other classes can and should import this and use it
 */ 
object JswordUtil {
  ///////////////////////////////////////////////////////////
  // class members
  ///////////////////////////////////////////////////////////

  // probably english?
  val defaultV11n : Versification = Versifications.instance().getVersification(Versifications.DEFAULT_V11N);
  val osisParser : OsisParser = new OsisParser();
  val bibleNames = BibleNames.instance()

  ///////////////////////////////////////////////////////////
  // OBJ HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * takes a single VerseRange (which represents a contiguous passage range) and finds all books that overlap
   *  - TODO this should not have to rely on instantiating all those static files...but works so is ok temporarily
   *  TODO rename to something involving "overlapping"
   *  - note that osis starts at 2 with Genesis, so exod is 3, lev is 4 etc
   */ 
  def getBooksBetween (jswordVerseRange : VerseRange) : List[BookVertex] = {
    // use ordinals fetch book names
    val startingName : String = osisToStartingBookName(jswordVerseRange.getOsisRef)
    val endingName : String = osisToEndingBookName(jswordVerseRange.getOsisRef)

    // TODO get off of this probably, just use jsword. Will probably get better performance
    val books : Iterable[BookVertex] = BookVertex.getBooksBetween(startingName, endingName)

    books.toList
  }

  def getBookNamesBetween (jswordVerseRange : VerseRange): List[String] = {
    val books : List[BookVertex] = getBooksBetween(jswordVerseRange)

    books.map(_.name)
  }

  def getBookOsisBetween (jswordVerseRange : VerseRange): List[String] = {
    val books : List[BookVertex] = getBooksBetween(jswordVerseRange)

    books.map(_.osisAbbreviation.get)
  }


  ///////////////////////////////////////////////////////////
  // STRING HELPERS
  ///////////////////////////////////////////////////////////

  /*
   * for splitting ranges into a list
   * e.g., Gen.1.1,Exod-Lev > two item list
   * e.g., Gen.1.1 > one item list
   * - can be continuous or discrete ranges. Will split by comma  (ie, assumes, separated ranges or just a single contiguous range)
   * - TODO add versification options
   */ 
  def parseOsisRanges (osis : String) : List[VerseRange] = {
    osis.split(",").map(parseOsisRange).toList
  }

  /*
   * For parsing when there might or might not be a hyphen (e.g., Gen.1.1-Exod OR Gen.1.1)
   * - only work for continuous ranges. 
   * - TODO add versification options (e.g., lxx vs mt vs vulgate etc)
   * - 
   */ 
  def parseOsisRange (osis : String) : VerseRange = {
    println(s"attempting to parse osis $osis");
    try {
      osisParser.parseOsisRef(defaultV11n, osis)
    } catch {
      case e: Throwable => {
        println(s"Failed to parse osis $osis")
        throw e
      }
    }
  }
  /*
   * For parsing a single verse (e.g., Gen.1.1)
   * - does not work with e.g., Gen.1. This osis needs a verse (though if you do getOsisID on a jsword VerseRange that is a whole chapter, will return eg "Gen.1")
   * - TODO add versification options
   */ 
  def parseOsisID (osis : String) : JswordVerse = {
    osisParser.parseOsisID(defaultV11n, osis)
  }

  /*
   * Takes an osis string (which is potentially a range, but not necessarily) and returns the first reference for it
   * - note that parseOsisID cannot take e.g., Gen.4 and work. Needs a verse. 
   *  - Accordingly, we're getting verse for this osis range, and returning it
   * - specifically the first verse, because this is starting range, so first verse will give starting book, starting chapter, and starting verse
   *   (e.g., Gen.1 > Genesis 1:1, which is correct, that is the first verse of Genesis 1)
   *
   */ 
  def osisToStartingRef (osis : String) : JswordVerse = {
    val parsedRange = parseOsisRange(osis)
    parsedRange.getStart 
    /*
    val allVerses = parsedRange.toVerseArray()

    if (allVerses.length == 0) {
      throw new java.lang.IllegalArgumentException(s"No reference found for osis $osis")
    }

    // first verse will be starting ref
    allVerses(0)
    */
  }

  /*
   *
   * - http://www.crosswire.org/jsword/cobertura/org.crosswire.jsword.examples.APIExamples.html
   */ 
  def osisToStartingBookName (osis : String) : String= {
    // we want full, hopefully these are teh same as what theographic uses!
    //osisToStartingRef(osis).getBook // would be e.g., Gen
    // e.g., Gen.1.1 => Genesis 1:1 => (Genesis,1:1) => Genesis
    val book : BibleBook = osisToStartingRef(osis).getBook

    //Books.installed().getBook(book.toString);
    bibleNames.getLongName(book);
  }
  def osisToStartingBookOrdinal (osis : String) : Int= {
    // we want full, hopefully these are teh same as what theographic uses!
    //osisToStartingRef(osis).getBook // would be e.g., Gen
    // e.g., Gen.1.1 => Genesis 1:1 => (Genesis,1:1) => Genesis
    val book : BibleBook = osisToStartingRef(osis).getBook
    book.ordinal
  }

  // TODO these should be implicits or constructrs and so call as asInstanceOf for more of a scala style conversion
  def osisToStartingBookReference (osis : String) : BookReference = {
    BookReference(osisToStartingBookName(osis))
  }

  def osisToStartingChapterReference (osis : String) : ChapterReference = {
    ChapterReference(osisToStartingBookName(osis), osisToStartingRef(osis).getChapter)
  }

  def osisToStartingChapterNumber (osis : String) : Int = {
    osisToStartingRef(osis).getChapter 
  }

  def osisToStartingVerseReference (osis : String) : VerseReference = {
    val verse : JswordVerse = osisToStartingRef(osis)
    VerseReference(verse)
  }

  def osisToStartingVerseNumber (osis : String) : Int= {
    osisToStartingRef(osis).getVerse
  }

  def osisToEndingRef (osis : String) : JswordVerse = {
    // parseOsisRange(osis).toVerseArray().last 
    parseOsisRange(osis).getEnd

  }

  def osisToEndingBookName (osis : String) : String= {
    // we want full, hopefully these are teh same as what theographic uses!
    //osisToEndingRef(osis).getBook // would be e.g., Gen
    // e.g., Gen.1.1 => Genesis 1:1 => (Genesis,1:1) => Genesis
    val book : BibleBook = osisToEndingRef(osis).getBook
    bibleNames.getLongName(book);
  }
  def osisToEndingBookOrdinal (osis : String) : Int = {
    // we want full, hopefully these are teh same as what theographic uses!
    //osisToEndingRef(osis).getBook // would be e.g., Gen
    // e.g., Gen.1.1 => Genesis 1:1 => (Genesis,1:1) => Genesis
    val book : BibleBook = osisToEndingRef(osis).getBook
    book.ordinal
  }
  def osisToEndingChapterNumber (osis : String) : Int = {
    osisToEndingRef(osis).getChapter 
  }
  def osisToEndingVerseNumber (osis : String) : Int = {
    osisToEndingRef(osis).getVerse
  }

  def osisToEndingVerseReference (osis : String) : VerseReference = {
    val verse : JswordVerse = osisToEndingRef(osis)
    VerseReference(verse)
  }



}
