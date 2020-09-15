package com.ryanquey.intertextualitygraph.dataimporter

// consider just using this: https://raw.githubusercontent.com/scrollmapper/bible_databases/master/cross_reference-mysql.sql
// "cross_references-mysql.sql (MySQL) is the cross-reference table. It has been separated to become an optional feature. This is converted from the project at http://www.openbible.info/labs/cross-references/."

import scala.collection.immutable.Map
import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers._
import com.ryanquey.intertextualitygraph.initializers.Initialize
import com.ryanquey.intertextualitygraph.modelhelpers._

object SearchSolr {

  def main (args: Array[String]): Unit = {
    new Initialize()
    // get the data files
    val result = TextHelpers.testSolrQuery()
    println(result)

  }
}
