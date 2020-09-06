package com.ryanquey.intertextualitygraph.models.books

import java.util.Date;
import java.time.Instant;

import scala.beans.BeanProperty;

// needs to be java bean, so record can be compatible with Cassandra java driver
// set all db fields in here. So BookRecord can work with it. But will inherit by Book class also, so DRY up Book class
// will not use this class directly, but will be parent class to other classes
class BookBase() {
  @BeanProperty var name : String // TEXT 
  @BeanProperty var yearWritten : Int // INT
  @BeanProperty var placeWritten : String // TEXT 
  @BeanProperty var author : String // TEXT
  @BeanProperty var bookOrder : Int // INT
  @BeanProperty var tyndaleAbbreviation : String // TEXT
  @BeanProperty var osisAbbreviation : String // TEXT
  @BeanProperty var slug : String // TEXT
  @BeanProperty var theographicShortName : String // TEXT
  @BeanProperty var scrollmapperId : String // TEXT 
  @BeanProperty var canonical : Boolean // BOOLEAN 
  @BeanProperty var bookSeries : String // TEXT 
  @BeanProperty var testament : String // TEXT 
  @BeanProperty var chapterCount : Int // INT
  @BeanProperty var verseCount : Int // INT
  @BeanProperty var userId : String // UUID 
  @BeanProperty var comments : String // TEXT
  @BeanProperty var updatedAt : Instant // TIMESTAMP, 
}
