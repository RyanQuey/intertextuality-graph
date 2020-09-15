package models

import play.api.mvc._
// use to get the global session
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.datastax.dse.driver.api.core.graph.DseGraph.g._;

import com.ryanquey.intertextualitygraph.models.texts.{Text => TextClass}
import com.ryanquey.intertextualitygraph.modelhelpers.TextHelpers


/**
 */
@Singleton
class Text {
  /**
   * so far just making this for a single reference, but eventually will probably make another one for if there is a starting and ending reference
   */
  def findByReference : TextClass (book : String, chapter : Int, verse : Int) = {
    // make a TextClass instance
    val text = new TextClass()

    // find it using solr query
    TextHelpers.findMatchByRef(
  }

}