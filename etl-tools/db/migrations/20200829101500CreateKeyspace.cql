CREATE KEYSPACE IF NOT EXISTS intertextuality_graph 
WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 } 
AND DURABLE_WRITES =  false;
