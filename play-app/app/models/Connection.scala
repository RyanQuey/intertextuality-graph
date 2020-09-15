package models

import play.api.mvc._
// use to get the global session
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.datastax.dse.driver.api.core.graph.DseGraph.g._;


/**
 * TODO add case class for intertextual_connections
 * maybe import from etl-tools case class, (in data-importer helpers dir), or maybe just make a new one, it might have a different contractor anyway
 */
@Singleton
class Connection {
  /**
   * http://www.doanduyhai.com/blog/?p=13301
   *
   */
  def findAllSourcesRecursively : Iterator<Collection<String>> () = {
    g.V().                   // Iterator<Vertex>
			has("text", "userId", "u861"). // Iterator<User>
			as("u861").                    // Label u861    
			repeat(out("knows")).times(2). // 2nd degree friends
			dedup().                       // Remove duplicates
			where(neq("u861")).            // Exclude u861   
			values("userId").              // Iterator<String>
			fold()                         // Iterator<Collection<String>>
  }

}
