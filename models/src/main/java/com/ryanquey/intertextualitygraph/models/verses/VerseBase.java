package com.ryanquey.intertextualitygraph.models.verses;

import java.time.Instant;

import com.ryanquey.datautils.models.ModelBase;
// needs to be java bean, so record can be compatible with Cassandra java driver
// set all db fields in here. So BookRecord can work with it. But will inherit by Book class also, so DRY up Book class
// will not use this class directly, but will be parent class to other classes
// - make sure to keep fields in sync with com.ryanquey.intertextualitygraph.graphmodels.VerseVertex

public class VerseBase extends ModelBase {
  private Integer number;  // INT
  private Integer yearWritten;  // INT 
  private String author;  // TEXT
  private String osisRef;  // TEXT 
  private String scrollmapperId;  // TEXT 
  private Boolean canonical;  // BOOLEAN 
  private String canonicalText;  // TEXT
  private String kjvText;  // TEXT
  private String mtText;  // TEXT
  private String rahlfsLxxText;  // TEXT
  private String sblGntText;  // TEXT
  private String byzGntText;  // TEXT
  private Integer chapter;  // INT 
  private String book;  // TEXT 
  private String bookSeries;  // TEXT 
  private String testament;  // TEXT 
  private String comments;  // TEXT
  private Instant updatedAt; // TIMESTAMP, 

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
  public String getOsisRef() {
    return osisRef;
  }
  public void setOsisRef(String osisRef) {
    this.osisRef = osisRef;
  }
  public String getScrollmapperId() {
    return scrollmapperId;
  }
  public void setScrollmapperId(String scrollmapperId) {
    this.scrollmapperId = scrollmapperId;
  }
  public Boolean getCanonical() {
    return canonical;
  }
  public void setCanonical(Boolean canonical) {
    this.canonical = canonical;
  }
  public String getCanonicalText() {
    return canonicalText;
  }
  public void setCanonicalText(String canonicalText) {
    this.canonicalText = canonicalText;
  }
  public String getKjvText() {
    return kjvText;
  }
  public void setKjvText(String kjvText) {
    this.kjvText = kjvText;
  }
  public Integer getChapter() {
    return chapter;
  }
  public void setChapter(Integer chapter) {
    this.chapter = chapter;
  }
  public String getBook() {
    return book;
  }
  public void setBook(String book) {
    this.book = book;
  }
  public String getBookSeries() {
    return bookSeries;
  }
  public void setBookSeries(String bookSeries) {
    this.bookSeries = bookSeries;
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
  public String getMtText() {
    return mtText;
  }
  public void setMtText(String mtText) {
    this.mtText = mtText;
  }
  public String getRahlfsLxxText() {
    return rahlfsLxxText;
  }
  public void setRahlfsLxxText(String rahlfsLxxText) {
    this.rahlfsLxxText = rahlfsLxxText;
  }
  public String getSblGntText() {
    return sblGntText;
  }
  public void setSblGntText(String sblGntText) {
    this.sblGntText = sblGntText;
  }
  public String getByzGntText() {
    return byzGntText;
  }
  public void setByzGntText(String byzGntText) {
    this.byzGntText = byzGntText;
  }


}

