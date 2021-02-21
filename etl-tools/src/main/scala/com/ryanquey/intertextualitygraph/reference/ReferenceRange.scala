package com.ryanquey.intertextualitygraph.reference
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
    def getStartingBookName : String = osisToStartingBookName(jswordVerseRange.getOsisRef)
    def getStartingBook : BookReference = BookReference(getStartingBookName)

    def getStartingVerseReference : VerseReference = osisToStartingVerseReference(jswordVerseRange.getOsisRef)
    def getStartingVerseNumber : Int = osisToStartingVerseNumber(jswordVerseRange.getOsisRef)
    def getStartingChapterReference : ChapterReference = osisToStartingChapterReference(jswordVerseRange.getOsisRef)
    def getStartingChapterNumber : Int = osisToStartingChapterNumber(jswordVerseRange.getOsisRef)

    def getEndingBookName : String = osisToEndingBookName(jswordVerseRange.getOsisRef)
    def getEndingVerseReference : VerseReference = osisToEndingVerseReference(jswordVerseRange.getOsisRef)
    def getEndingVerseNumber : Int = osisToEndingVerseNumber(jswordVerseRange.getOsisRef)
    def getStartingBookOrdinal : Int = osisToStartingBookOrdinal(jswordVerseRange.getOsisRef)
    def getEndingBookOrdinal : Int = osisToEndingBookOrdinal(jswordVerseRange.getOsisRef)

    def getOverlappingBookNames : List[String] = getBookNamesBetween(jswordVerseRange)
    def getOverlappingBookOsis : List[String] = getBookOsisBetween(jswordVerseRange)

    def getTotalVerseCount : Int = jswordVerseRange.getCardinality

    def overlapsWith(otherRefRange : ReferenceRange) : Boolean = jswordVerseRange.overlaps(otherRefRange.jswordVerseRange)

    // check if book  fully contains this ref range
    def contains(otherRefRange : ReferenceRange) : Boolean = jswordVerseRange.contains(otherRefRange.jswordVerseRange)


    def adjacentTo(otherRefRange : ReferenceRange) : Boolean = jswordVerseRange.adjacentTo(otherRefRange.jswordVerseRange)

    /**
     *
     *
     * @return names of all books this range completely contains. E.g., Gen 3-Num 3 would return [Exodus, Leviticus]
     */
    def getWholeBooks () : List[BookReference] = {

      // get overlapping books as reference range, so can send to this.contains (which expects a reference range)
      val overlappingBooksAsRanges : List[ReferenceRange] = this.getOverlappingBookOsis.map(bookOsis => {
        val jswordVerseRange = parseOsisRange(bookOsis)

        ReferenceRange(jswordVerseRange)
      })

      // filter out books that this ref range doesn't contain
      val containedBooks : List[ReferenceRange] = overlappingBooksAsRanges.filter(refRange => {
        this.contains(refRange)
      })

      // now convert to List of BookReferences. Since each containedBook has only one book in its range, can getStartingBook or getEndingBook, should not make a difference. 
      containedBooks.map(_.getStartingBook)
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
