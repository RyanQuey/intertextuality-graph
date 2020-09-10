package com.ryanquey.intertextualitygraph.models.books;

import java.util.Date;
import java.util.UUID;
import java.time.Instant;

// needs to be java bean, so record can be compatible with Cassandra java driver
// set all db fields in here. So BookRecord can work with it. But will inherit by Book class also, so DRY up Book class
// will not use this class directly, but will be parent class to other classes

public class BookBase {
  private String name; // TEXT 
  private Integer yearWritten; // INT
  private String placeWritten; // TEXT 
  private String author; // TEXT
  private Integer bookOrder; // INT
  private String tyndaleAbbreviation; // TEXT
  private String osisAbbreviation; // TEXT
  private String slug; // TEXT
  private String theographicShortName; // TEXT
  private String scrollmapperId; // TEXT 
  private Boolean canonical; // BOOLEAN 
  private String bookSeries; // TEXT 
  private String testament; // TEXT 
  private Integer chapterCount; // INT
  private Integer verseCount; // INT
  private UUID userId; // UUID 
  private String comments; // TEXT
  private Instant updatedAt; // TIMESTAMP, 

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Integer getYearWritten() {
    return yearWritten;
  }
  public void setYearWritten(Integer yearWritten) {
    this.yearWritten = yearWritten;
  }
  public String getPlaceWritten() {
    return placeWritten;
  }
  public void setPlaceWritten(String placeWritten) {
    this.placeWritten = placeWritten;
  }
  public String getAuthor() {
    return author;
  }
  public void setAuthor(String author) {
    this.author = author;
  }
  public Integer getBookOrder() {
    return bookOrder;
  }
  public void setBookOrder(Integer bookOrder) {
    this.bookOrder = bookOrder;
  }
  public String getTyndaleAbbreviation() {
    return tyndaleAbbreviation;
  }
  public void setTyndaleAbbreviation(String tyndaleAbbreviation) {
    this.tyndaleAbbreviation = tyndaleAbbreviation;
  }
  public String getOsisAbbreviation() {
    return osisAbbreviation;
  }
  public void setOsisAbbreviation(String osisAbbreviation) {
    this.osisAbbreviation = osisAbbreviation;
  }
  public String getSlug() {
    return slug;
  }
  public void setSlug(String slug) {
    this.slug = slug;
  }
  public String getTheographicShortName() {
    return theographicShortName;
  }
  public void setTheographicShortName(String theographicShortName) {
    this.theographicShortName = theographicShortName;
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
  public Integer getChapterCount() {
    return chapterCount;
  }
  public void setChapterCount(Integer chapterCount) {
    this.chapterCount = chapterCount;
  }
  public Integer getVerseCount() {
    return verseCount;
  }
  public void setVerseCount(Integer verseCount) {
    this.verseCount = verseCount;
  }
  public UUID getUserId() {
    return userId;
  }
  public void setUserId(UUID userId) {
    this.userId = userId;
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
