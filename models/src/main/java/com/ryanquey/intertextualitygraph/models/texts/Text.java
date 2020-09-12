package com.ryanquey.intertextualitygraph.models.texts;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import com.ryanquey.datautils.models.Model;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.ryanquey.datautils.cassandraHelpers.CassandraDb;
import com.datastax.oss.driver.api.core.cql.*;


// TODO consider making these models in scala, since they don't interact with java driver directly
// FOr now, just put everything in a helpers file...better to not use oop in scala to learn fp. For now
public class Text extends TextBase implements Model {

  public Map<String, String> schema = new HashMap<String, String>();
  static {
    
  }

  // constructors
  public Text() {
    // initialize schema 
    schema.put("id", "UUID"); // INT 
    schema.put("yearWritten", "String"); // INT 
    schema.put("author", "String"); // TEXT
    schema.put("canonical", "String"); // BOOLEAN 
    schema.put("canonicalText", "String"); // TEXT
    schema.put("startingBook", "String"); // UUID
    schema.put("startingChapter", "Integer"); // UUID
    schema.put("startingVerse", "Integer"); // UUID
    schema.put("endingBook", "String"); // UUID
    schema.put("endingChapter", "Integer"); // UUID
    schema.put("endingVerse", "Integer"); // UUID
    schema.put("splitPassages", "List<String>"); // LIST<TEXT>
    schema.put("testament", "String"); // TEXT 
    schema.put("greekTranslation", "String"); // TEXT 
    schema.put("englishTranslation", "String"); // TEXT 
    schema.put("comments", "String"); // TEXT
    schema.put("createdBy", "String"); // TEXT 
    schema.put("updatedBy", "String"); // TEXT 
    schema.put("updatedAt", "Instant"); // TIMESTAMP 

    if (this.getId() == null) {
      this.setId(Uuids.timeBased());
    }
  };

  public Text(TextRecord textRecord) {
    // TODO 
  };

  public void persist ()  throws Exception {
    TextRecord e =  new TextRecord(this);
    System.out.println("persisting text " + e);

    TextDao dao = e.getDao();

    dao.save(e);
  };

  // returns null if nothing found
  // NOTE TODO currently doesn't work, returns error InvalidQueryException: Undefined column name solr_query
  public Boolean existsByRef () throws Exception {
    System.out.println("persisting text " + this);
    TextDao dao = new TextRecord().getDao();

    String solrQuery = String.format("'created_by:%s" +
      " AND ending_book:%s" +
      " AND ending_chapter:%s" +
      " AND ending_verse:%s" +
      " AND starting_book:%s" +
      " AND starting_chapter:%s" +
      " AND starting_verse:%s'", this.getCreatedBy(), this.getEndingBook(), this.getEndingChapter(), this.getEndingVerse(), this.getStartingBook(), this.getStartingChapter(), this.getStartingVerse());

    
    // not working unfortunately, spend maybe an hour and no luck
    // TextRecord found = dao.findBySolr(solrQuery);
    String query = "SELECT * FROM intertextuality_graph.texts where solr_query = " + solrQuery;
    System.out.print("Running CQL: " + query);
    ResultSet rs = CassandraDb.execute(query);
    Row row = rs.one();


    // often we don't care about the returned record, just want to know whether it exists or not. But good to return a record anyways, just in case
    if (row != null) {
      // TODO this would be better, to return a Text instance
      // return new Text(found);
      return true;
    } else {
      return false;
    }

  }
}
