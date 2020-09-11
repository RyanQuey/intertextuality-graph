package com.ryanquey.intertextualitygraph.modelhelpers

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text

import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers.TheographicDataFile

object BookHelpers {
  // no reason to pull from the database, just load from csv
  val dataFile = new TheographicDataFile("books", "books-Grid view.csv");
  val books : Iterable[Book] = dataFile.getModelInstances().asInstanceOf[Iterable[Book]]

  def getBookByNum (bookNum : Int) : Book = {
    books.find((b) => b.getBookOrder() == bookNum).get
  }

  def getBookByOsis (osisName : String ) : Book = {
    println(s"looking for osis name $osisName...")
    books.find((b) => b.getOsisAbbreviation() == osisName).get
  }

  def osisNameToName (osisName : String ) : String = {
    BookHelpers.getBookByOsis(osisName).getName()
  }

  def bookNumToName (bookNum : Int) : String = {
    // e.g., 1 > Genesis
    BookHelpers.getBookByNum(bookNum).getName()
  }
}
