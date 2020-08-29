CREATE TABLE IF NOT EXISTS intertextuality_graph.chapters (
  number INT,
  date_written DATE, 
  author TEXT,
  id UUID, 
  canonical BOOLEAN, 
  canonical_text TEXT,
  chapter_id UUID, 
  book_id UUID, 
  book_series_id UUID, 
  testament TEXT, 
  user_id UUID, 
  comments TEXT,
  updated_at TIMESTAMP, 
  PRIMARY KEY ((number), id)
);
