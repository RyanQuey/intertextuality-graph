package com.ryanquey.intertextualitygraph.models.texts;

import java.util.UUID;
import java.util.List;

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
@CqlName("texts") 
public class TextRecord extends TextBase implements Record {
  
  @PartitionKey(0) 
  private String startingBook;

  @ClusteringColumn(0)
  private UUID id;

  TextRecord(Text text)  throws Exception {
    DataClassesHelpers.copyMatchingFields(text, this);
  }

  // keeping empty constructor for use with Dao
  TextRecord() {};
  
  // no sure why there was every nonstatic version
  // TODO DEPRECATED
  TextDao getDao () {
    return InventoryMapperObj
      .inventoryMapper
      .textDao("texts");
  }

  public static TextDao dao () {
    return InventoryMapperObj
      .inventoryMapper
      .textDao("texts");
  }

};


