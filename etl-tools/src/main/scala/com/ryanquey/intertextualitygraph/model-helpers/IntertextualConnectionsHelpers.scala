package com.ryanquey.intertextualitygraph.modelhelpers

import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse
import com.ryanquey.intertextualitygraph.models.texts.Text

import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
// dse recommends using this for fluent API over strings, since strings can be unwieldy
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph


object IntertextualConnectionsHelpers {
  def connectTexts (srcText : Text, alludingText : Text) = {
  }
}
