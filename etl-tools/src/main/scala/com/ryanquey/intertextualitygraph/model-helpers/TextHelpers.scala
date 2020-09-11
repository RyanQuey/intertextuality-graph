package com.ryanquey.intertextualitygraph.modelhelpers

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text

// TODO make this, I think it's helpful. Can have better helpers
// case class Reference()

object TextHelpers {
  // NOTE could be a range, but osisRange could also be a single ref. You won't know until you parse
  // main use case for this one though is building a text model instance
  def populateFieldsfromOsis (osisRange : String, text : Text) { 
    println(s"osisRange is: $osisRange")
    val refs = osisRange.split("-")

    val startingRef = refs(0)
    // NOTE might be the same as the starting ref, if there was no - 
    val endingRef = refs.last

    // parse startingRef
    val startingRefData = startingRef.split("\\.")
    println(s"starting ref is: $startingRef")
    val startingBookOsis = startingRefData(0)
    val startingBookName = BookHelpers.osisNameToName(startingBookOsis)
    val startingChapter = startingRefData(1).toInt
    val startingVerse = startingRefData(2).toInt

    text.setStartingBook(startingBookName)
    text.setStartingChapter(startingChapter)
    text.setStartingVerse(startingVerse)

    // parse endingRef
    val endingRefData = endingRef.split("\\.")
    val endingBookOsis = endingRefData(0)
    val endingBookName = BookHelpers.osisNameToName(endingBookOsis)
    val endingChapter = endingRefData(1).toInt
    val endingVerse = endingRefData(2).toInt

    text.setEndingBook(endingBookName)
    text.setEndingChapter(endingChapter)
    text.setEndingVerse(endingVerse)

  }
}
