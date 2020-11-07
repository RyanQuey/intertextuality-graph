CREATE TABLE IF NOT EXISTS intertextuality_graph.text_from_chapter_edges (
  chapter_book TEXT,
  chapter_number INT,
  text_id TIMEUUID,
  text_starting_book TEXT,
  updated_at TIMESTAMP, 
  // this will allow good default order in the db when we do basic selects. 
  PRIMARY KEY (chapter_book, chapter_number, text_starting_book, text_id)
);
