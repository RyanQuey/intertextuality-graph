package com.ryanquey.intertextualitygraph.initializers

/*
 * things to do when play app is initializing
 * https://www.playframework.com/documentation/2.6.x/ScalaDependencyInjection#Eager-bindings
 * https://stackoverflow.com/a/15987439/6952495
 * https://dzone.com/articles/run-code-on-startup-with-play-and-scala
 */

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.ryanquey.intertextualitygraph.models.ProjectInventoryMapper

class EagerLoadModule extends AbstractModule {
  override def configure() = {
    bind(classOf[Initialize]).asEagerSingleton
  }
}


@Singleton
class Initialize {
  val keyspace = "intertextuality_graph"
  CassandraDb.initialize(keyspace)

  inventoryMapper = ProjectInventoryMapper
    .builder(CassandraDb.session) // calls the builder method we defined in our InventoryMapper class, which is wrapper for InventoryMapperBuilder
    .withDefaultKeyspace(keyspace)
    .build();

  // we like putting it on our CassandraDb class, so the inventoryMapper intance only needs to be built once and then is globally accesible by importing CassandraDb.
  // I'm sure there is a better system, but this will work for now
  CassandraDb.inventoryMapper = inventoryMapper
}
