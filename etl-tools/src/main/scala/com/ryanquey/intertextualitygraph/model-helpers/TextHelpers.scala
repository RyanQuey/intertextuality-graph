package com.ryanquey.intertextualitygraph.modelhelpers

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text
import scala.collection.JavaConverters._ 


// TODO make this, I think it's helpful. Can have better helpers
// case class Reference()

// avoiding 

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
    text.setStartingBook(startingBookName)
    val startingChapter = startingRefData(1).toInt
    text.setStartingChapter(startingChapter)
    if (startingRefData.length > 2) {
      // has a verse (most do for TSK)
      val startingVerse = startingRefData(2).toInt
      text.setStartingVerse(startingVerse)
    }

    // parse endingRef
    val endingRefData = endingRef.split("\\.")
    val endingBookOsis = endingRefData(0)
    val endingBookName = BookHelpers.osisNameToName(endingBookOsis)
    text.setEndingBook(endingBookName)
    val endingChapter = endingRefData(1).toInt
    text.setEndingChapter(endingChapter)
    
    if (endingRefData.length > 2) {
      // has a verse (most do for TSK)
      val endingVerse = endingRefData(2).toInt
      text.setEndingVerse(endingVerse)
    }

    
    // NOTE for TSK data at least, should not have any semicolon at this point, so will just be a single split passage.
    val splitPassages = osisRange.split(";").toList.asJava
      
    text.setSplitPassages(splitPassages)

  }
  
  def connectTexts (srcText : Text, alludingText : Text) = {
    srcText
    // ...
  }
}
