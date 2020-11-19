package models.traversalbuilder.reference
import org.crosswire.jsword.passage.VerseRange


import com.ryanquey.intertextualitygraph.utils.JswordUtil._


/*
 * represents a single reference that spans a single CONTIGUOUS range 
 * - e.g., Gen 1-3 or Gen1.1 or Gen 1.1-3 NOT Gen 1-2; 4-5;
 * - overlaps conceptually at least some with TextVertex, but at least currently is only used specifically for building traversals
 * - for now, just provides a wrapper for jsword verse range mostly
 *
 *   TODO 
 *   - also consider taking advantage of jsword's Passage classes, ie RangedPassage and DistinctPassage (both seem to do the same thing, but with different performance benefits due to different data type used under the cover)
 *    https://github.com/crosswire/jsword/blob/master/src/main/java/org/crosswire/jsword/passage/Passage.java
 *   - probably could easily extract out some of this into a trait that all "Range" classes in this package inherit from
 */ 
case class ReferenceRange(
  referenceOsis : String,
  jswordVerseRange : VerseRange,
  ) {
    def isWholeChapter : Boolean = jswordVerseRange.isWholeChapter
    def isWholeChapters : Boolean = jswordVerseRange.isWholeChapters
    def isWholeBook : Boolean = jswordVerseRange.isWholeBook
    def isWholeBooks : Boolean = jswordVerseRange.isWholeBooks
    def isMultipleBooks : Boolean = jswordVerseRange.isMultipleBooks
    def getStartingBookName : String = osisToStartingBookName(jswordVerseRange)

    def getStartingVerse : ReferenceVerse = osisToStartingVerseReference(jswordVerseRange)
    def getStartingVerseNumber : ReferenceVerse = osisToStartingVerseNumber(jswordVerseRange)
    def getStartingChapter : ReferenceChapter = osisToStartingChapter(jswordVerseRange)

    def getEndingBookName : String = osisToEndingBookName(jswordVerseRange)
    def getEndingVerse : ReferenceVerse = osisToEndingVerseReference(jswordVerseRange)
    def getStartingBookOrdinal : String = osisToStartingBookOrdinal(jswordVerseRange)
    def getEndingBookOrdinal : String = osisToEndingBookOrdinal(jswrdVerseRange)

    def getOverlappingBookNames : List[String] = getBookNamesBetween(getEndingBook, getStartingBook)
    def getOverlappingBookOsis : List[String] = getBookOsisBetween(getEndingBook, getStartingBook)

    def getTotalVerseCount : Int = jswordVerseRange.getCardinality

    def overlapsWith(otherRefRange : ReferenceRange) : Boolean = jswordVerseRange.overlaps(otherRefRange.jswordVerseRange)
    def contains(otherRefRange : ReferenceRange) : Boolean = jswordVerseRange.contains(otherRefRange.jswordVerseRange)
    def adjacentTo(otherRefRange : ReferenceRange) : Boolean = jswordVerseRange.adjacentTo(otherRefRange.jswordVerseRange)

    // names of all books this range completely contains. E.g., Gen 3-Num 3 would return [Exodus, Leviticus]
    def getWholeBooks () : List[ReferenceRange] = {
      val fullBooksContainedByRef = getOverlappingBookOsis.map(ReferenceRange(_))
      fullBooksContainedByRef.filter(osis => {
        val refRangeForFullBook = 
        this.contains(refRangeForFullBook)
      })
    }


    // TODO add support for changing versification. Jsword can do this
  }

object ReferenceRange {
  // overload constructor for case class
  def apply(range : VerseRange) = {
    new ReferenceRange(
      referenceOsis = range.getOsisRef,
      jswordVerseRange = range
    )
  }

}
