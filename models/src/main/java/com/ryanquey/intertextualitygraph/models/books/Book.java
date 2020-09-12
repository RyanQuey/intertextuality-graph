package com.ryanquey.intertextualitygraph.models.books;

import java.util.Map;
import java.util.HashMap;

import com.ryanquey.datautils.models.Model;

public class Book extends BookBase implements Model {

  public Map<String, String> schema = new HashMap<String, String>();

  // constructors
  public Book() {
    schema.put("name", "String");// TEXT 
    schema.put("yearWritten", "Integer");// INT
    schema.put("placeWritten", "String");// TEXT 
    schema.put("author", "String");// TEXT
    schema.put("bookOrder", "Integer");// INT
    schema.put("tyndaleAbbreviation", "String");// TEXT
    schema.put("osisAbbreviation", "String");// TEXT
    schema.put("slug", "String");// TEXT
    schema.put("theographicShortName", "String");// TEXT
    schema.put("scrollmapperId", "String");// TEXT 
    schema.put("canonical", "Boolean");// BOOLEAN 
    schema.put("bookSeries", "String");// TEXT 
    schema.put("testament", "String");// TEXT 
    schema.put("chapterCount", "Integer");// INT
    schema.put("verseCount", "Integer");// INT
    schema.put("comments", "String");// TEXT
    schema.put("updatedAt", "Instant");// TIMESTAMP, 
  };

  public Book(BookRecord bookRecord) {
    // TODO 
  };


  public void persist () throws Exception {
    System.out.println("persisting book");
    BookRecord e =  new BookRecord(this);

    BookDao dao = e.getDao();

    dao.save(e);
  };
}
