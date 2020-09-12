package com.ryanquey.intertextualitygraph.models.texts;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.entity.saving.NullSavingStrategy;
import com.datastax.oss.driver.api.mapper.annotations.DefaultNullSavingStrategy;
import java.util.UUID;
import java.util.List;

import com.ryanquey.datautils.models.BaseDao;

// NOTE this means that in order to erase a field, cannot set it to null

@Dao 
@DefaultNullSavingStrategy(NullSavingStrategy.DO_NOT_SET) 
public interface TextDao extends BaseDao<TextRecord> {

  /** Simple selection by full primary key. */
  @Select
  TextRecord findOne(String startingBook, UUID id);

    
  // this is hideous
  @Select(customWhereClause = "solr_query='created_by::createdBy AND ending_book::endingBook AND ending_chapter::endingChapter AND ending_verse::endingVerse AND starting_book::startingBook AND starting_chapter::startingChapter AND starting_verse::startingVerse'")
  TextRecord findByRef(String createdBy, String endingBook, Integer endingChapter, Integer endingVerse, String startingBook, Integer startingChapter, Integer startingVerse); 
}
