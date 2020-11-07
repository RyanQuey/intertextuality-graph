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
 * - for anything that interacts with the scala case class use ChapterVertex companion object
 * - And, in general, don't add to this much, so it's easy to migrate over from the java models in the future. Instead of writing methods here, convert the java class > case class, and then use method from ChapterVertex companion object
 */

object ChapterHelpers {

  ///////////////////////////////////////////////////////
  // File IO
  ///////////////////////////////////////////////////////
  /*
   * set var (chapters) that represents all the chapter metadata
   * - once per time this class is instantiated, get all chapter data
   * - no reason to pull from the database, just load from csv one time and be done
   * - NOTE at least for now, there's not a strong reason to convert this over into the case class. 
   *   * It's hacky, but it works well, and provides and easy way to access basic information about our chapters. 
   *   * Also does not pollute our ChapterVertex companion object with all this stuff that mostly just interacts with constants
   */
  val _dataFile = new TheographicDataFile("chapters", "chapters-Grid view.csv");
  val allChaptersFromFile : Iterable[Chapter] = _dataFile.getModelInstances().asInstanceOf[Iterable[Chapter]]

  ///////////////////////////////////////////////////////
  // Get chapters by filter
  ///////////////////////////////////////////////////////
  def getChapterByNum (book : Book, chapterNum : Int) : Chapter = {
    getChapterByRefData(book.getName, chapterNum) 
  }

  /*
   * grab data for a chapter from the Theographic data File
   * - assumes English Canon 
   * - Does not touch the db
   * - only reads from file one time, when class is loaded
   * - provides an easy way to get chapter metadata quickly
   */ 
  def getChapterByRefData (bookName : String, chapterNum : Int) : Chapter = {
    allChaptersFromFile.find((c) => c.getBook == bookName && c.getNumber() == chapterNum).get
  }

  def getChapterForVerse (verse : Verse) : Chapter = {
    allChaptersFromFile.find((c) => c.getBook == verse.getBook && c.getNumber() == verse.getChapter).get
  }


  /*
   * retrieve chapter java model instance using for ALL chapters between the two provided
   * - uses the same name that we use as db primary keys
   * - works as of 11/2020 but trying to use case classes instead
   */ 
  // def getChaptersBetween (startingChapter : Chapter, endingChapter : Chapter) : Iterable[Chapter] = {
  //   val endingBookNumber = endingChapter.getBook
  //   val startingBook = BookHelpers.getBookForChapter(startingChapter)
  //   val endingBook = BookHelpers.getBookForChapter(endingChapter)
  //   val books = BookHelpers.getBooksBetween(startingBook, endingBook)

  //   // https://alvinalexander.com/scala/how-to-create-mutable-list-in-scala-listbuffer-cookbook/
  //   val chapters = new ListBuffer[Chapter]()

  //   // iterate over the books
  //   // - TODO probably could do this using chapters.filter, and probably better performance?? Except, would have to iterate over all chapters in bible a few times.
  //   books.foreach(book => {
  //     // note that endingBook could also be first book, if there' only one book
  //     val isFirstBook = book.getName == startingBook.getName
  //     val isFinalBook = book.getName == endingBook.getName

  //     // if this is NOT the first book, then start at beginning of the book
  //     val initialChapterNum : Int = if (isFirstBook) startingChapter.getNumber else 1
  //     val finalChapterNum : Int = if (isFinalBook) endingChapter.getNumber else book.getChapterCount

  //     val chapterRange = initialChapterNum to finalChapterNum
  //     for (chapterNum <- chapterRange) {
  //       chapters += getChapterByNum(book, chapterNum)
  //     }

  //   }) 

  //   chapters
  // }



  ///////////////////////////////////////
  // get chapter field value(s) using a chapter unique identifier (name, order in English canon, etc)
  ///////////////////////////////////////

}
