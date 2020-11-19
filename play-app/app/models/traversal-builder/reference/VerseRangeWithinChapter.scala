package models.traversalbuilder.reference
import org.crosswire.jsword.passage.VerseRange


import com.ryanquey.intertextualitygraph.utils.JswordUtil._


/*
 * represents a range of verses  that all belong to a single chapter
 * - when possible, if verses are a whole chapter, use a different class instead.
 * - important for building query ...though I'm sure there's a better way of going about this
 */ 
case class VerseRangeWithinChapter(
  book : BookReference, // TEXT 
  chapter : ChapterReference,
  startingVerse : VerseReference,
  endingVerse : VerseReference,
  )
