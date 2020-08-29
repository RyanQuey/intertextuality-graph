CREATE TABLE IF NOT EXISTS intertextuality_graph.chapters (
  number INT,
  date_written DATE, 
  author TEXT,
  id UUID, 
  canonical BOOLEAN, 
  book_id UUID, 
  book_series_id UUID, 
  verse_count INT,
  testament TEXT, 
  user_id UUID, 
  comments TEXT,
  updated_at TIMESTAMP, 
  PRIMARY KEY ((number), id)
);
