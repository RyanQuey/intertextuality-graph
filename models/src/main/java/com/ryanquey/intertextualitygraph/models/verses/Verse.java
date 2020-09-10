package com.ryanquey.intertextualitygraph.models.verses;

import java.util.Map;
import java.util.HashMap;

import com.ryanquey.datautils.models.Model;

public class Verse extends VerseBase implements Model {

  public Map<String, String> schema = new HashMap<String, String>();

  // constructors
  public Verse() {
    schema.put("number", "Integer"); // INT
    schema.put("yearWritten", "Integer"); // INT 
    schema.put("author", "String"); // TEXT
    schema.put("osisRef", "String"); // TEXT 
    schema.put("scrollmapperId", "String"); // TEXT 
    schema.put("canonical", "Boolean"); // BOOLEAN 
    schema.put("canonicalText", "String"); // TEXT
    schema.put("chapter", "Integer"); // INT 
    schema.put("book", "String"); // TEXT 
    schema.put("bookSeries", "String"); // TEXT 
    schema.put("testament", "String"); // TEXT 
    schema.put("comments", "String"); // TEXT
    schema.put("updatedAt", "Instant"); // TIMESTAMP 
  };

  public Verse(VerseRecord verseRecord) {
    // TODO 
  };


  public void persist () {
    System.out.println("persisting verse");
    VerseRecord e =  new VerseRecord(this);

    VerseDao dao = e.getDao();

    dao.save(e);
  };
}
