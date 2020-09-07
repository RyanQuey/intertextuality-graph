package com.ryanquey.intertextualitygraph.models.books;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;

import com.ryanquey.datautils.helpers.DataClassesHelpers;
import com.ryanquey.datautils.cassandraHelpers.CassandraDb;
import com.ryanquey.datautils.models.Record;
import com.ryanquey.intertextualitygraph.models.InventoryMapper;
import com.ryanquey.intertextualitygraph.models.InventoryMapperObj;

@Entity 
@CqlName("books") 
class BookRecord extends BookBase implements Record {
  
  @PartitionKey(0) 
  private String name; // C* TEXT 

  BookRecord(Book book) {
    DataClassesHelpers.copyMatchingFields(book, this);
  }

  // keeping empty constructor for use with Dao
  BookRecord() {};
  BookDao getDao () {
    return InventoryMapperObj.inventoryMapper.bookDao("books");
  }

};


