package com.ryanquey.intertextualitygraph.dataimporter.models.books

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;

import com.ryanquey.datautils.helpers.DataClassesHelpers;
import com.ryanquey.datautils.cassandraHelpers.CassandraDb;


@Entity
@CqlName("books")
class BookRecord extends BookBase (val book : Option[Book]) {
  @PartitionKey(0)
  @BeanProperty var name : String // C* TEXT 
  if (book.isDefined) DataClassesHelpers.copyMatchingFields(book, this);

  def getDao () : BookDao = {
    CassandraDb.inventoryMapper.episodeByPodcastDao("books");
  }

  // keeping empty constructor for use with Dao
  def this () = {
    this(null)
  }
};


