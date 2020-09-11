package com.ryanquey.intertextualitygraph.models.texts;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import com.ryanquey.datautils.models.Model;

public class Text extends TextBase implements Model {

  public Map<String, String> schema = new HashMap<String, String>();
  static {
    
  }

  // constructors
  public Text() {
    // initialize schema 
    schema.put("id", "String"); // UUID 
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
    schema.put("updatedAt", "Instant"); // TIMESTAMP 

    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
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

  /////////////////////////////////
  // validation
  ////////////////////////////////
}
