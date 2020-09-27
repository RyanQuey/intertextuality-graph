package com.ryanquey.intertextualitygraph.models.verses;

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
@CqlName("verses") 
public class VerseRecord extends VerseBase implements Record {
  
  @PartitionKey(0) 
  private String book;

  @ClusteringColumn(0)
  private Integer chapter;

  @ClusteringColumn(1)
  private Integer number;

  VerseRecord(Verse verse)  throws Exception {
    DataClassesHelpers.copyMatchingFields(verse, this);
  }

  // keeping empty constructor for use with Dao
  VerseRecord() {};

  // no sure why there was every nonstatic version
  // TODO DEPRECATED
  VerseDao getDao () {
    return InventoryMapperObj
      .inventoryMapper
      .verseDao("verses");
  }

  public static VerseDao dao () {
    return InventoryMapperObj
      .inventoryMapper
      .verseDao("verses");
  }

};


