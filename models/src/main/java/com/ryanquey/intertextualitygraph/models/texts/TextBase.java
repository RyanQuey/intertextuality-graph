package com.ryanquey.intertextualitygraph.models.texts;

import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.time.Instant;

import com.ryanquey.datautils.models.ModelBase;

// needs to be java bean, so record can be compatible with Cassandra java driver
// set all db fields in here. So BookRecord can work with it. But will inherit by Book class also, so DRY up Book class
// will not use this class directly, but will be parent class to other classes

public class TextBase extends ModelBase {
  private String startingBook;  
  private List<String> splitPassages;  // LIST<TEXT>
  private Integer yearWritten;  // INT 
  private String author;  // TEXT
  private Boolean canonical;  // BOOLEAN 
  private String canonicalText;  // TEXT
  private Integer startingChapter;  
  private Integer startingVerse;  
  private String endingBook;  
  private Integer endingChapter; 
  private Integer endingVerse;  
  private String testament;  // TEXT 
  private String greekTranslation;  // TEXT 
  private String englishTranslation;  // TEXT 
  private String comments;  // TEXT
  private Instant updatedAt; // TIMESTAMP, 

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
  public String getStartingBook() {
    return startingBook;
  }
  public void setStartingBook(String startingBook) {
    this.startingBook = startingBook;
  }
  public Integer getStartingChapter() {
    return startingChapter;
  }
  public void setStartingChapter(Integer startingChapter) {
    this.startingChapter = startingChapter;
  }
  public Integer getStartingVerse() {
    return startingVerse;
  }
  public void setStartingVerse(Integer startingVerse) {
    this.startingVerse = startingVerse;
  }
  public String getEndingBook() {
    return endingBook;
  }
  public void setEndingBook(String endingBook) {
    this.endingBook = endingBook;
  }
  public Integer getEndingChapter() {
    return endingChapter;
  }
  public void setEndingChapter(Integer endingChapter) {
    this.endingChapter = endingChapter;
  }
  public Integer getEndingVerse() {
    return endingVerse;
  }
  public void setEndingVerse(Integer endingVerse) {
    this.endingVerse = endingVerse;
  }
  public List<String> getSplitPassages() {
    return splitPassages;
  }
  public void setSplitPassages(List<String> splitPassages) {
    this.splitPassages = splitPassages;
  }
  public String getTestament() {
    return testament;
  }
  public void setTestament(String testament) {
    this.testament = testament;
  }
  public String getGreekTranslation() {
    return greekTranslation;
  }
  public void setGreekTranslation(String greekTranslation) {
    this.greekTranslation = greekTranslation;
  }
  public String getEnglishTranslation() {
    return englishTranslation;
  }
  public void setEnglishTranslation(String englishTranslation) {
    this.englishTranslation = englishTranslation;
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
