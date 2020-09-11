package com.ryanquey.intertextualitygraph.models.chapters;

import java.util.Date;
import java.util.UUID;
import java.time.Instant;

// needs to be java bean, so record can be compatible with Cassandra java driver
// set all db fields in here. So BookRecord can work with it. But will inherit by Book class also, so DRY up Book class
// will not use this class directly, but will be parent class to other classes

public class ChapterBase {
  private Integer number;  // INT
  private Integer yearWritten;  // INT 
  private String author;  // TEXT
  private String scrollmapper_id;  // TEXT 
  private String osisRef;  // TEXT 
  private Boolean canonical;  // BOOLEAN 
  private String book;  // TEXT 
  private String bookSeriesId;  // TEXT 
  private Integer verseCount;  // INT
  private String testament;  // TEXT 
  private String comments;  // TEXT
  private Instant updatedAt;  // TIMESTAMP 

  public Integer getNumber() {
    return number;
  }
  public void setNumber(Integer number) {
    this.number = number;
  }
  public Integer getYearWritten() {
    return yearWritten;
  }
  public void setYearWritten(Integer yearWritten) {
    this.yearWritten = yearWritten;
  }
  public String getAuthor() {
    return author;
  }
  public void setAuthor(String author) {
    this.author = author;
  }
  public String getScrollmapper_id() {
    return scrollmapper_id;
  }
  public void setScrollmapper_id(String scrollmapper_id) {
    this.scrollmapper_id = scrollmapper_id;
  }
  public String getOsisRef() {
    return osisRef;
  }
  public void setOsisRef(String osisRef) {
    this.osisRef = osisRef;
  }
  public Boolean getCanonical() {
    return canonical;
  }
  public void setCanonical(Boolean canonical) {
    this.canonical = canonical;
  }
  public String getBook() {
    return book;
  }
  public void setBook(String book) {
    this.book = book;
  }
  public String getBookSeriesId() {
    return bookSeriesId;
  }
  public void setBookSeriesId(String bookSeriesId) {
    this.bookSeriesId = bookSeriesId;
  }
  public Integer getVerseCount() {
    return verseCount;
  }
  public void setVerseCount(Integer verseCount) {
    this.verseCount = verseCount;
  }
  public String getTestament() {
    return testament;
  }
  public void setTestament(String testament) {
    this.testament = testament;
  }
  public String getComments() {
    return comments;
  }
  public void setComments(String comments) {
    this.comments = comments;
  }
  public Instant getUpdatedAt() {
    return updatedAt;
  }
  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

}
