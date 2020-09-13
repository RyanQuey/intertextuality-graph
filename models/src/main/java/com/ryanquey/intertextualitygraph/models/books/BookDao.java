package com.ryanquey.intertextualitygraph.models.books;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.entity.saving.NullSavingStrategy;
import com.datastax.oss.driver.api.mapper.annotations.DefaultNullSavingStrategy;
import java.util.UUID;
import java.time.Instant;

import com.ryanquey.datautils.models.BaseDao;

// NOTE this means that in order to erase a field, cannot set it to null

@Dao 
@DefaultNullSavingStrategy(NullSavingStrategy.DO_NOT_SET) 
public interface BookDao extends BaseDao<BookRecord> {

  /** Simple selection by full primary key. */
  @Select
  BookRecord findOne(String name);

}
