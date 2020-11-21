package models.traversalbuilder.reference
import org.crosswire.jsword.passage.VerseRange
import com.ryanquey.intertextualitygraph.reference.BookReference


import com.ryanquey.intertextualitygraph.utils.JswordUtil._


/*
 * represents a range of contiguous books 
 * might not use
 */ 
case class BookRange(
  startingBook : BookReference,
  endingBook : BookReference,
  )
