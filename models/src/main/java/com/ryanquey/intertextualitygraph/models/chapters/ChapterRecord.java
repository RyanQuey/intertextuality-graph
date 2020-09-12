package com.ryanquey.intertextualitygraph.models.chapters;

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
@CqlName("chapters") 
class ChapterRecord extends ChapterBase implements Record {
  
  @PartitionKey(0) 
  private String book; // C* TEXT 

  @ClusteringColumn(0) 
  private Integer number; // C* INT 

  ChapterRecord(Chapter chapter)  throws Exception {
    DataClassesHelpers.copyMatchingFields(chapter, this);
  }

  // keeping empty constructor for use with Dao
  ChapterRecord() {};
  ChapterDao getDao () {
    return InventoryMapperObj
      .inventoryMapper
      .chapterDao("chapters");
  }

};


