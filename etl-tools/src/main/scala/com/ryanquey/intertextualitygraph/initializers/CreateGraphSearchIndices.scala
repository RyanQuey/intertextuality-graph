package com.ryanquey.intertextualitygraph.initializers

import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement
import com.ryanquey.datautils.cassandraHelpers.CassandraDb


/**
 * create search indices for our graph using gremlin
 * - I haven't figured out how to do this using fluent API, so just doing script API for now
 */ 
object CreateGraphSearchIndices {
  def createIndices() = {

		val statementForEndingRefIndex : ScriptGraphStatement = ScriptGraphStatement.newInstance(
			"schema.vertexLabel('text').searchIndex().ifNotExists().by('ending_ref_index').create()"
    ).setGraphName("intertextuality_graph");

		val statementForStartingRefIndex : ScriptGraphStatement = ScriptGraphStatement.newInstance(
			"schema.vertexLabel('text').searchIndex().ifNotExists().by('starting_ref_index').create()"
    ).setGraphName("intertextuality_graph");

    CassandraDb.session.execute(statementForStartingRefIndex);
    CassandraDb.session.execute(statementForEndingRefIndex);
  }
}
