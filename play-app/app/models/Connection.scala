package models

import play.api.mvc._
// use to get the global session
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.datastax.dse.driver.api.core.graph.DseGraph.g._;

import java.util.Collection

/**
 * TODO add case class for intertextual_connections
 * maybe import from etl-tools case class, (in data-importer helpers dir), or maybe just make a new one, it might have a different contractor anyway
 */
@Singleton
class Connection {

}
