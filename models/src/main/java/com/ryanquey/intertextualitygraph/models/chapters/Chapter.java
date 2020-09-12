package com.ryanquey.intertextualitygraph.models.chapters;

import java.util.Map;
import java.util.HashMap;

import com.ryanquey.datautils.models.Model;

public class Chapter extends ChapterBase implements Model {

  public Map<String, String> schema = new HashMap<String, String>();

  // constructors
  public Chapter() {
    schema.put("number", "Integer"); // INT
    schema.put("yearWritten", "Integer"); // INT 
    schema.put("author", "String"); // TEXT
    schema.put("scrollmapper_id", "String"); // TEXT 
    schema.put("osisRef", "String"); // TEXT 
    schema.put("canonical", "Boolean"); // BOOLEAN 
    schema.put("book", "String"); // TEXT 
    schema.put("bookSeriesId", "String"); // TEXT 
    schema.put("verseCount", "Integer"); // INT
    schema.put("testament", "String"); // TEXT 
    schema.put("comments", "String"); // TEXT
    schema.put("updatedAt", "Instant"); // TIMESTAMP 
  };

  public Chapter(ChapterRecord chapterRecord) {
    // TODO 
  };


  public void persist ()  throws Exception {
    System.out.println("persisting chapter");
    ChapterRecord e =  new ChapterRecord(this);

    ChapterDao dao = e.getDao();

    dao.save(e);
  };
}
