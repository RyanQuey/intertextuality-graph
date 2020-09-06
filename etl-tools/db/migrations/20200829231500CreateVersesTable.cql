CREATE TABLE IF NOT EXISTS intertextuality_graph.verses (
  number INT,
  year_written INT, 
  author TEXT,
  scrollmapper_id TEXT, 
  canonical BOOLEAN, 
  canonical_text TEXT,
  chapter INT, 
  book TEXT, 
  book_series TEXT, 
  testament TEXT, 
  user_id UUID, 
  comments TEXT,
  updated_at TIMESTAMP, 
  PRIMARY KEY ((book), chapter, number)
);
