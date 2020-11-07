CREATE TABLE IF NOT EXISTS intertextuality_graph.text_from_verse_edges (
  verse_book TEXT,
  verse_chapter INT,
  verse_number INT,
  text_id TIMEUUID,
  text_starting_book TEXT,
  updated_at TIMESTAMP, 
  // this will allow good default order in the db when we do basic selects. 
  PRIMARY KEY (verse_book, verse_chapter, verse_number, text_starting_book, text_id)
);
