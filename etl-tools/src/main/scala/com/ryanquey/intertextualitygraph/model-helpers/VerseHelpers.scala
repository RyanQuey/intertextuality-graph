package com.ryanquey.intertextualitygraph.modelhelpers

import scala.collection.mutable.ListBuffer

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text

import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers.TheographicDataFile


/*
 * (DEPRECATED)
 * for helpers that interact with the java model Class; 
 * - for anything that interacts with the scala case class use VerseVertex companion object
 * - And, in general, don't add to this much, so it's easy to migrate over from the java models in the future. Instead of writing methods here, convert the java class > case class, and then use method from VerseVertex companion object
 */

object VerseHelpers {

  ///////////////////////////////////////////////////////
  // File IO
  ///////////////////////////////////////////////////////
  /*
   * set var (verses) that represents all the verse metadata
   * - once per time this class is instantiated, get all verse data
   * - no reason to pull from the database, just load from csv one time and be done
   * - NOTE at least for now, there's not a strong reason to convert this over into the case class. 
   *   * It's hacky, but it works well, and provides and easy way to access basic information about our verses. 
   *   * Also does not pollute our VerseVertex companion object with all this stuff that mostly just interacts with constants
   */
  val _dataFile = new TheographicDataFile("verses", "verses-completed_records.csv");
  val verses : Iterable[Verse] = _dataFile.getModelInstances().asInstanceOf[Iterable[Verse]]

  ///////////////////////////////////////////////////////
  // Get verses by filter
  ///////////////////////////////////////////////////////
  def getVerseByNum (book : Book, chapter : Chapter, verseNum : Int) : Verse = {
    getVerseByRefData(book.getName, chapter.getNumber, verseNum) 
  }

  /*
   * grab data for a verse from the Theographic data File
   * - assumes English Canon 
   * - Does not touch the db
   * - only reads from file one time, when class is loaded
   * - provides an easy way to get verse metadata quickly
   */ 
  def getVerseByRefData (bookName : String, chapterNum : Int, verseNum : Int) : Verse = {
    verses.find((v) => v.getBook == bookName && v.getChapter == chapterNum && v.getNumber() == verseNum).get
  }
  
  /*
   * retrieve verse java model instance using for ALL chapters between the two provided
   * - uses the same name that we use as db primary keys
   *   TODO try not to use this one, use VerseVertex implementation instead, though the below is working as of 11/20
   */ 
  // def getVersesBetween (startingVerse : Verse, endingVerse : Verse) : Iterable[Verse] = {
  //   val endingChapterNumber = endingVerse.getChapter
  //   val startingChapter = ChapterHelpers.getChapterForVerse(startingVerse)
  //   val endingChapter = ChapterHelpers.getChapterForVerse(endingVerse)
  //   val chapters = ChapterHelpers.getChaptersBetween(startingChapter, endingChapter)

  //   // https://alvinalexander.com/scala/how-to-create-mutable-list-in-scala-listbuffer-cookbook/
  //   val verses = new ListBuffer[Verse]()

  //   // iterate over the chapters
  //   // - TODO probably could do this using verses.filter, and probably better performance?? Except, would have to iterate over all verses in bible a few times.
  //   chapters.foreach(chapter => {
  //     // note that endingChapter could also be first chapter, if there' only one chapter
  //     val isFirstChapter = chapter.getNumber == startingChapter.getNumber
  //     val isFinalChapter = chapter.getNumber == endingChapter.getNumber

  //     // if this is NOT the first chapter, then start at beginning of the chapter
  //     val initialVerseNum : Int = if (isFirstChapter) startingVerse.getNumber else 1
  //     val finalVerseNum : Int = if (isFinalChapter) endingVerse.getNumber else chapter.getVerseCount

  //     val verseRange = initialVerseNum to finalVerseNum
  //     for (verseNum <- verseRange) {
  //       verses += getVerseByRefData(chapter.getBook, chapter.getNumber, verseNum)
  //     }

  //   }) 

  //   verses
  // }
}
