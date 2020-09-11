package com.ryanquey.intertextualitygraph.dataimporter.constants
import com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers.Helpers
import scala.collection.immutable.Map

object TSKConstants {
  /*
   * NOTE next time would probably make these two match case pattern matching funcs instead TODO. Fits scala better
   *
   * 
   */

  // From Verse	To Verse	Votes
  val openBibleFieldsMapping : Map[String, Map[String, String]] = Map(
    ("From Verse", Map(("alluding_db_col", ""))),
  )

  val tskCLIMapping : Map[String, Map[String, String]] = Map(
    // book	chapter	verse	sort	words	refs
    ("book", Map(
      ("book", "book_order")
    ))
  )
} 
