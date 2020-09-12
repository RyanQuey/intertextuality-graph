package com.ryanquey.intertextualitygraph.models.texts;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import com.ryanquey.datautils.models.Model;

// TODO consider making these models in scala, since they don't interact with java driver directly
// FOr now, just put everything in a helpers file...better to not use oop in scala to learn fp. For now
public class Text extends TextBase implements Model {

  public Map<String, String> schema = new HashMap<String, String>();
  static {
    
  }

  // constructors
  public Text() {
    // initialize schema 
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

}
