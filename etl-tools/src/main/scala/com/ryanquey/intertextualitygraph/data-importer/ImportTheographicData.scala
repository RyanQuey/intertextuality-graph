package com.ryanquey.intertextualitygraph.dataimporter
import scala.collection.immutable.Map
import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers._

object ImportTheographicData {
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
