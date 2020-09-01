
class Verse
  number INT,
  year_written DATE, 
  author TEXT,
  scrollmapper_id TEXT, 
  canonical BOOLEAN, 
  canonical_text TEXT,
  chapter_id TEXT, 
  book TEXT, 
  book_series TEXT, 
  testament TEXT, 
  user_id UUID, 
  comments TEXT,
  updated_at TIMESTAMP, 
  PRIMARY KEY (scrollmapper_id)
