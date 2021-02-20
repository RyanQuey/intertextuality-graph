package com.ryanquey.intertextualitygraph.reference
import org.crosswire.jsword.passage.VerseRange
import com.ryanquey.intertextualitygraph.reference.{BookReference, ChapterReference}


import com.ryanquey.intertextualitygraph.utils.JswordUtil._


/*
 * represents a range of chapters that all belong to a single book
 * - important for building query ...though I'm sure there's a better way of going about this
 *   TODO put this in etl-tools, it is used there now
 */ 
case class ChapterRangeWithinBook (
  book : BookReference, // TEXT 
  startingChapter : ChapterReference,
  endingChapter : ChapterReference,
  )

object ChapterRangeWithinBook {
  def apply (startingChapter : ChapterReference, endingChapter : ChapterReference) : ChapterRangeWithinBook = { 
    val book = startingChapter.book
    ChapterRangeWithinBook(BookReference(book), startingChapter, endingChapter)
  }
}
