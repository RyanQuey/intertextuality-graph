package com.ryanquey.intertextualitygraph.dataimporter

// consider just using this: https://raw.githubusercontent.com/scrollmapper/bible_databases/master/cross_reference-mysql.sql
// "cross_references-mysql.sql (MySQL) is the cross-reference table. It has been separated to become an optional feature. This is converted from the project at http://www.openbible.info/labs/cross-references/."

import scala.collection.immutable.Map
import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers._
import com.ryanquey.intertextualitygraph.initializers.Initialize

object ImportTreasuryOfScriptureKnowledge {

  // csv file name to the table 
  val dataSourceFiles = Map(
    //("intertextual_connections", "open-bible-cross-references.txt"),
    ("intertextual_connections", "tsk-cli-formatted.csv"),
  )


  def main (args: Array[String]) = {
    new Initialize()
    // get the data files
    for ((tablename, filename) <- dataSourceFiles) {  
      println(s"now importing file $filename into table $tablename")
      val dataFile = new TSKDataFile(tablename, filename);

      dataFile.parseFile()
    }

    println(s"\n\nFinished.")
    // currently throws unhandled error TODO
    // System.exit(0)
  }
}
