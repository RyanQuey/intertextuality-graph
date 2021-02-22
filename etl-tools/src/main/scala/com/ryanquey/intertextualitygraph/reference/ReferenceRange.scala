package com.ryanquey.intertextualitygraph.reference
import com.ryanquey.intertextualitygraph.graphmodels.{BookVertex, ChapterVertex, TextVertex}
import org.crosswire.jsword.passage.VerseRange
import com.ryanquey.intertextualitygraph.utils.JswordUtil._

import scala.collection.mutable


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
    def getEndingChapterNumber : Int = osisToEndingChapterNumber(jswordVerseRange.getOsisRef)
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
   * return 2 item tuple, one as startingRefIndex one as endingRefIndex
   *
   * TODO make sure I can do Some(this.getStartingVerseNumber)) and not have to check if
   */
  def asRefIndices () = {
    val startingRefIndex : Int = TextVertex.getIndexForStartingRef(
      this.getStartingBookName,
      this.getStartingChapterNumber,
      Some(this.getStartingVerseNumber))

    val endingRefIndex : Int = TextVertex.getIndexForEndingRef(
      this.getEndingBookName,
      this.getEndingChapterNumber,
      Some(this.getEndingVerseNumber))

    (startingRefIndex, endingRefIndex)
  }

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


  /**
   *  takes ReferenceRange set and converts each one into either a set of book references, a ChapterRangeWithinBook, or a VerseRangeWithinChapter
   *  maybe DEPRECATED? originally made  for filterByConnectedRefEdges which we don't use, though maybe will use for something else in the future
   *
   * - the idea is to break it down into the greatest possible segments, since these will be used
   * - books don't need to/can't use ranges, since they use strings rather than integers. Probably could use integers if we wanted, but then would need to assign integers for extra biblical books as well... Which is also possible, but just do it with strings for now. There probably should not be that many, so should be fine.
   *
   */
  def breakdownRefRanges (referenceRanges : Set[ReferenceRange]) : GroupedRangeSets = {
    val bookReferences : mutable.Set[BookReference] = mutable.Set.empty
    val chapterRanges : mutable.Set[ChapterRangeWithinBook] = mutable.Set.empty
    val verseRanges : mutable.Set[VerseRangeWithinChapter] = mutable.Set.empty
    for (referenceRange <- referenceRanges) {
      // 1) get out whatever whole books that are contained by this ref range that we can
      // NOTE this should work, but might just use recursive function below to pull out everything we need
      val wholeBooks = referenceRange.getWholeBooks
      if (!wholeBooks.isEmpty) {
        bookReferences ++= wholeBooks.asInstanceOf[mutable.Set[BookReference]]
      }

      // 2) with remainder, get out whatever will chapters we can
      // 3) with remainder, set the rest to verseRanges. Should be at most two - one at the start, one at the end. Everything in the middle should be a whole chapter/book
      // - since all whole books are gone, we can just look at the start and end book
      val startingVerse : VerseReference = referenceRange.getStartingVerseReference
      val endingVerse : VerseReference = referenceRange.getEndingVerseReference


      // start with startingVerse and recursively iterate over chapters until we get to ending Verse
      def breakdownRefRange (startingVerse : VerseReference, endingVerse : VerseReference) : Unit = {
        val endOfBook : VerseReference = startingVerse.getLastVerseOfBook
        val endOfChapter : VerseReference = startingVerse.getLastVerseOfChapter

        // is whole book, and we've pulled those out already. We're done, no need to continue
        val rangeIsWholeBook = startingVerse.book == endingVerse.book && endingVerse.isSameAs(endOfBook) && startingVerse.isStartOfBook
        // we've made it to the end. Add this verse and we're done
        // actually, this probably means they only identified one verse they wanted to filter by

        if (rangeIsWholeBook) {
          return
        }

        // temporarily setting here, but might overwrite
        var newStartingVerse : VerseReference = startingVerse.getLastVerseOfChapter

        // 1) CHECK IF WHOLE BOOK
        // - at this point, only possible if there are multiple books left in this range
        val hasWholeBookToAdd = startingVerse.isStartOfBook && startingVerse.book != endingVerse.book
        if (hasWholeBookToAdd) {
          // passing for now, already added
          // move on to next book

          // in this case, do get, since there should always be a next one unless our flow control didn't work...I think...
          val nextBook : BookVertex = BookVertex.getBookAfter(endOfBook.book).get
          newStartingVerse = VerseReference(nextBook.name, 1, 1)
        }

        // 2) CHECK IF WHOLE CHAPTER
        // - true as long as we don't end before end of chapter (ie endingVerse is before end of ch) AND starting verse is start of chapter.
        // - checking by: 1) if endingVerse is a different chapter, it must be a later chapter, so this whole chapter is safe to add or 2) if endingVerse is the end of chapter, we can add this chapter also
        val hasWholeChapterToAdd = startingVerse.isStartOfChapter && (!endingVerse.isSameChapterAs(startingVerse) || endingVerse.isSameAs(endOfChapter))
        if (!hasWholeBookToAdd && hasWholeChapterToAdd) {
          println("======= hasWholeChapterToAdd!! ======= ")

          // check following chapters until we get all whole chapters that we can.
          // - We want ranges to include as much as possible, since in the end each range will hit db one more time! (ie., if one range per chapter, that's one db transaction per chapter, vs if all chapters are in one range, that's only one db transaction)
          val chapterRange = startingVerse.getFullChaptersWithinBookUntil(endingVerse)
          val nextChapter : ChapterVertex = ChapterVertex.getChapterAfter(chapterRange.endingChapter.book, chapterRange.endingChapter.number).get

          // add this range to the chapterRanges set
          chapterRanges += chapterRange

          newStartingVerse = VerseReference(nextChapter.book, nextChapter.number, 1)
        }

        // 3) ADD ALL VERSES OTHERWISE
        if (!hasWholeBookToAdd && !hasWholeChapterToAdd) {
          println("======= has only verses.. ======= ")

          // make sure to not go past endVerse
          // get earliest verse between these
          println(s"checking if $endOfChapter is after $endingVerse")
          val terminalVerseForIteration : VerseReference = if (endOfChapter.isAfter(endingVerse)) endingVerse else endOfChapter
          println(s"terminal verse for this iteration is: $terminalVerseForIteration")

          // add a final verse range that includes the rest of the verses
          verseRanges += VerseRangeWithinChapter(startingVerse, terminalVerseForIteration)

          // we're at the end
          newStartingVerse = endingVerse
        }

        // 4) CHECK IF FINISHED SO KNOW IF SHOULD CONTINUE ITERATING
        val finished = newStartingVerse.isAfter(endingVerse) || newStartingVerse.isSameAs(endingVerse) || rangeIsWholeBook

        if (!finished) {
          // keep iterating
          breakdownRefRange(newStartingVerse, endingVerse)
        }
      }

      breakdownRefRange(startingVerse, endingVerse)

    }

    // now our sets are built out. Return as tuple
    GroupedRangeSets(bookReferences.toSet, chapterRanges.toSet, verseRanges.toSet)
  }
}
