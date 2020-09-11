package com.ryanquey.intertextualitygraph.models.texts;

import java.util.Map;
import java.util.HashMap;

import com.ryanquey.datautils.models.Model;

public class Text extends TextBase implements Model {

  public Map<String, String> schema = new HashMap<String, String>();

  // constructors
  public Text() {
    schema.put("yearWritten", "String"); // INT 
    schema.put("author", "String"); // TEXT
    schema.put("id", "String"); // UUID 
    schema.put("canonical", "String"); // BOOLEAN 
    schema.put("canonicalText", "String"); // TEXT
    schema.put("startingBookId", "String"); // UUID
    schema.put("startingChapterId", "String"); // UUID
    schema.put("startingVerseId", "String"); // UUID
    schema.put("endingBookId", "String"); // UUID
    schema.put("endingChapterId", "String"); // UUID
    schema.put("endingVerseId", "String"); // UUID
    schema.put("splitPassages", "String"); // LIST<TEXT>
    schema.put("testament", "String"); // TEXT 
    schema.put("greekTranslation", "String"); // TEXT 
    schema.put("englishTranslation", "String"); // TEXT 
    schema.put("comments", "String"); // TEXT
    schema.put("updatedAt", "Instant"); // TIMESTAMP 
  };

  public Text(TextRecord textRecord) {
    // TODO 
  };


  public void persist () {
    System.out.println("persisting text");
    TextRecord e =  new TextRecord(this);

    TextDao dao = e.getDao();

    dao.save(e);
  };
}
