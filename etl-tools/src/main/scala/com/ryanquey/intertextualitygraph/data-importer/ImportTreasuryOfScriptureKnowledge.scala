package com.ryanquey.intertextualitygraph.dataimporter

// consider just using this: https://raw.githubusercontent.com/scrollmapper/bible_databases/master/cross_reference-mysql.sql
// "cross_references-mysql.sql (MySQL) is the cross-reference table. It has been separated to become an optional feature. This is converted from the project at http://www.openbible.info/labs/cross-references/."

import scala.collection.immutable.Map
import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers._

object ImportTreasuryOfScriptureKnowledgeData {
  /*
   * Docs for datasource:
   * https://www.notion.so/Using-the-GraphQL-API-3c0f5f2614974ace8af7b872fc13ee01
   * TODO eventually will probably extract other data from this dataset
   */

  // csv file name to the table 
  val dataSourceFiles = Map(
    ("books", "books-Grid view.csv"),
    // ("books", "books-Grid view.csv"),
    // ("chapters", "chapters-Grid view.csv"),
    // ("verses", "verses-Grid view.csv"),
  )


  def main (args: Array[String]) = {
    // get the data files
    for ((tablename, filename) <- dataSourceFiles) {  
      println(s"now importing file $filename into table $tablename")
      val dataFile = new TheographicDataFile(tablename, filename);

      dataFile.parseFile()
    }
  }
}
