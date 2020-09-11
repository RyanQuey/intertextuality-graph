CREATE KEYSPACE IF NOT EXISTS intertextuality_graph 
  // unlike other migrations, this should always be kept up to date with the latest keyspace definition
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 } 
  AND graph_engine = 'Core'
  AND DURABLE_WRITES = true;
