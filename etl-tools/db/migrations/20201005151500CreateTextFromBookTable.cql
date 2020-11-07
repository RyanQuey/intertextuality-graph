CREATE TABLE IF NOT EXISTS intertextuality_graph.text_from_book_edges (
  book_name TEXT,
  text_id TIMEUUID,
  text_starting_book TEXT,
  updated_at TIMESTAMP, 
  // this will allow good default order in the db when we do basic selects. 
  PRIMARY KEY (book_name, text_starting_book, text_id)
);
