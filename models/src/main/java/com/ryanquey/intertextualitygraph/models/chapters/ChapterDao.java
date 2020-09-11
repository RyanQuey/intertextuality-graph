package com.ryanquey.intertextualitygraph.models.chapters;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.entity.saving.NullSavingStrategy;
import com.datastax.oss.driver.api.mapper.annotations.DefaultNullSavingStrategy;
import java.util.UUID;

import com.ryanquey.datautils.models.BaseDao;

// NOTE this means that in order to erase a field, cannot set it to null

@Dao 
@DefaultNullSavingStrategy(NullSavingStrategy.DO_NOT_SET) 
public interface ChapterDao extends BaseDao<ChapterRecord> {

  /** Simple selection by full primary key. */
  @Select
  ChapterRecord findOne(String book, Integer number);

}
