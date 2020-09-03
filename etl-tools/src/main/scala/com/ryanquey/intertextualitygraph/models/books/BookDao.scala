package com.ryanquey.intertextualitygraph.dataimporter.models.books

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.entity.saving.NullSavingStrategy;
import com.datastax.oss.driver.api.mapper.annotations.DefaultNullSavingStrategy;
import java.util.UUID;

@Dao
// NOTE this means that in order to erase a field, cannot set it to null
@DefaultNullSavingStrategy(NullSavingStrategy.DO_NOT_SET)
trait BookDao {

  /** Simple selection by full primary key. */
  @Select
  def findOne(name : String) : BookRecord;

  //@Select(customWhereClause = "podcast_api = ':podcastApi' AND podcast_api_id = ':podcastApiId'")
  //BookRecord findAll(String podcastApi, String podcastApiId);
  /**
   * Selection by partial primary key, this will return multiple rows.
   *
   * <p>Also, note that this queries a different table: DAOs are not limited to a single entity, the
   * return type of the method dictates what rows will be mapped to.
   */
	/*
  @Select
  PagingIterable<EpisodeByLanguage> getByLanguage(String language);
  @Select
  PagingIterable<EpisodeByPrimaryGenre> getByLanguage(String language, String primaryGenre);
	*/

  /**
   * Creating a video is a bit more complex: because of denormalization, it involves multiple
   * tables.
   *
   * <p>A query provider is a nice way to wrap all the queries in a single operation, and hide the
   * details from the DAO interface.
   */
  // not doing for now, only doing one table
  /*
  @QueryProvider(
      providerClass = CreateEpisodeQueryProvider.class,
      entityHelpers = {Episode.class, UserEpisode.class, LatestEpisode.class, EpisodeByTag.class})
  create(Episode video);
  */
  @Insert
  def create(book : BookRecord);

  /**
   * Update using a template: the template must have its full primary key set; beyond that, any
   * non-null field will be considered as a value to SET on the target row.
   *
   * <p>Note that we specify the null saving strategy for emphasis, but this is the default.
   *
   * For more on how to do customWhereClause and other options, see here: https://github.com/datastax/java-driver/tree/4.x/manual/mapper/daos/update#parameters
   *
   * TODO maybe have to pass in primary_genre and feed_url also, and work those into the customWhereClause also
   */
  @Update
  def save(book : BookRecord);
}
