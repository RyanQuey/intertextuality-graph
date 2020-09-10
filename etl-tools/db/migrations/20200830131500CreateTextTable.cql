CREATE TABLE IF NOT EXISTS intertextuality_graph.texts (
  year_written INT, 
  author TEXT,
  id UUID, 
  canonical BOOLEAN, 
  canonical_text TEXT,
  starting_book TEXT,
  starting_chapter INT,
  starting_verse INT,
  ending_book TEXT,
  ending_chapter INT,
  ending_verse INT,
  split_passages LIST<TEXT>,
  testament TEXT, 
  greek_translation TEXT, 
  english_translation TEXT, 
  comments TEXT,
  updated_at TIMESTAMP, 
  // this will allow good default order in the db when we do basic selects. 
  PRIMARY KEY ((starting_book_id), starting_chapter_id, starting_verse_id, id)
);
