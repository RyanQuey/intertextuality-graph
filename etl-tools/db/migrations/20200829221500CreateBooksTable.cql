CREATE TABLE IF NOT EXISTS intertextuality_graph.books (
  name TEXT, 
  date_written DATE, 
  author TEXT,
  tyndale_abbreviation TEXT,
  id UUID, 
  canonical BOOLEAN, 
  book_series_id UUID, 
  testament TEXT, 
  chapter_count INT,
  user_id UUID, 
  comments TEXT,
  updated_at TIMESTAMP, 
  PRIMARY KEY ((name), id) 
);
