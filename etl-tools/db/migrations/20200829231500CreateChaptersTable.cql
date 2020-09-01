CREATE TABLE IF NOT EXISTS intertextuality_graph.chapters (
  number INT,
  year_written DATE, 
  author TEXT,
  scrollmapper_id TEXT, 
  canonical BOOLEAN, 
  book TEXT, 
  book_series_id TEXT, 
  verse_count INT,
  testament TEXT, 
  user_id UUID, 
  comments TEXT,
  updated_at TIMESTAMP, 
  PRIMARY KEY ((book), number)
);
