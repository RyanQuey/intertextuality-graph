package com.ryanquey.intertextualitygraph.modelhelpers

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text

import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers.TheographicDataFile

/*
 * (DEPRECATED)
 * for helpers that interact with the java model Class; 
 * - for anything that interacts with the scala case class use BookVertex companion object
 * - And, in general, don't add to this much, so it's easy to migrate over from the java models in the future. Instead of writing methods here, convert the java class > case class, and then use method from BookVertex companion object
 */

object BookHelpers {

  ///////////////////////////////////////////////////////
  // File IO
  ///////////////////////////////////////////////////////
  /*
   * set var (books) that represents all the book metadata
   * - once per time this class is instantiated, get all book data
   * - no reason to pull from the database, just load from csv one time and be done
   * - NOTE at least for now, there's not a strong reason to convert this over into the case class. 
   *   * It's hacky, but it works well, and provides and easy way to access basic information about our books. 
   *   * Also does not pollute our BookVertex companion object with all this stuff that mostly just interacts with constants
   */
  val _dataFile = new TheographicDataFile("books", "books-Grid view.csv");
  val allBooksFromFile : Iterable[Book] = _dataFile.getModelInstances().asInstanceOf[Iterable[Book]]

  ///////////////////////////////////////////////////////
  // Get books by filter
  ///////////////////////////////////////////////////////
  /*
   * grab data for a book from the Theographic data File
   * - assumes English Canon 
   * - Does not touch the db
   * - only reads from file one time, when class is loaded
   * - provides an easy way to get book metadata quickly
   */ 
  def getBookByNum (bookNum : Int) : Book = {
    allBooksFromFile.find((b) => b.getBookOrder() == bookNum).get
  }

  def getBookByOsis (osisName : String ) : Book = {
    allBooksFromFile.find((b) => b.getOsisAbbreviation() == osisName).get
  }

  /*
   * retrieve book java model instance using standard name
   * - uses the same name that we use as db primary keys
   */ 
  def getBookByName (bookName : String) : Book = {
    allBooksFromFile.find((b) => b.getName() == bookName).get
  }

  /*
   * retrieve book java model instances using for ALL books between the two provided, inclusive
   * - assumes English Bible book ordering
   * - TODO this works, but don't want to use, since we are switching over to using case classes almost exclusively
   */ 
   // def getBooksBetween (startingBook : Book, endingBook : Book) : Iterable[Book] = {
   //   allBooksFromFile.filter((b) => b.getBookOrder >= startingBook.getBookOrder && b.getBookOrder <= endingBook.getBookOrder)
   // }

  /*
   * taking a model instance of Chapter, returning book model instance
   *
   */
  def getBookForChapter (chapter : Chapter) : Book = {
    allBooksFromFile.find((b) => b.getName() == chapter.getBook).get
  }



  ///////////////////////////////////////
  // get book field value(s) using a book unique identifier (name, order in English canon, etc)
  ///////////////////////////////////////

  def bookNumToOsisName (bookNum : Int) : String = {
    BookHelpers.getBookByNum(bookNum).getOsisAbbreviation()
  }

  def osisNameToName (osisName : String ) : String = {
    BookHelpers.getBookByOsis(osisName).getName()
  }

  def bookNumToName (bookNum : Int) : String = {
    // e.g., 1 > Genesis
    BookHelpers.getBookByNum(bookNum).getName()
  }
}
