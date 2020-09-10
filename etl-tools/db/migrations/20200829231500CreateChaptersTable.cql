CREATE TABLE IF NOT EXISTS intertextuality_graph.chapters (
  number INT,
  year_written INT, 
  author TEXT,
  scrollmapper_id TEXT, 
  osis_ref TEXT, 
  canonical BOOLEAN, 
  book TEXT, 
  book_series_id TEXT, 
  verse_count INT,
  testament TEXT, 
  comments TEXT,
  updated_at TIMESTAMP, 
  PRIMARY KEY ((book), number)
);
