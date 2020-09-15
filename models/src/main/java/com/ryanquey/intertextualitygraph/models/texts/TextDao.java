package com.ryanquey.intertextualitygraph.models.texts;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.entity.saving.NullSavingStrategy;
import com.datastax.oss.driver.api.mapper.annotations.DefaultNullSavingStrategy;
import java.util.UUID;
import java.util.List;

import com.ryanquey.datautils.models.BaseDao;

// NOTE this means that in order to erase a field, cannot set it to null

@Dao 
@DefaultNullSavingStrategy(NullSavingStrategy.DO_NOT_SET) 
public interface TextDao extends BaseDao<TextRecord> {

  // https://community.datastax.com/questions/8352/support-for-solr-query-in-java-driver-mapper.html
  // https://github.com/msmygit/dse-titbits/blob/master/java-driver-4.x/src/main/java/com/madhavan/demos/mapper/ExampleDao.java#L21
  @Query("SELECT * FROM intertextuality_graph.texts where solr_query = :solr_query")
  TextRecord findOneByQuery(String solr_query); 

  /** Simple selection by full primary key. */
  @Select
  TextRecord findOne(String startingBook, UUID id);
}
