CREATE TABLE IF NOT EXISTS intertextuality_graph.books (
  name TEXT, 
  year_written INT, 
  place_written TEXT, 
  author TEXT,
  book_order INT,
  tyndale_abbreviation TEXT,
  osis_abbreviation TEXT,
  slug TEXT,
  theographic_short_name TEXT,
  scrollmapper_id TEXT, 
  canonical BOOLEAN, 
  book_series TEXT, 
  testament TEXT, 
  chapter_count INT,
  verse_count INT,
  comments TEXT,
  updated_at TIMESTAMP, 
  PRIMARY KEY (name) 
);
