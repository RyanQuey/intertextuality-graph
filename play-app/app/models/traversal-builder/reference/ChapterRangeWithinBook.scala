package models.traversalbuilder.reference
import org.crosswire.jsword.passage.VerseRange


import com.ryanquey.intertextualitygraph.utils.JswordUtil._


/*
 * represents a range of chapters that all belong to a single book
 * - important for building query ...though I'm sure there's a better way of going about this
 */ 
case class ChapterRangeWithinBook(
  book : BookReference, // TEXT 
  startingChapter : ChapterReference,
  endingChapter : ChapterReference,
  )
