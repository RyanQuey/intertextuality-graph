CREATE TABLE IF NOT EXISTS intertextuality_graph.book_series (
  id UUID, 
  name TEXT, 
  canonical BOOLEAN, 
  author TEXT,
  tyndale_abbreviation TEXT,
  osis_abbreviation TEXT,
  testament TEXT, 
  book_count LIST<TEXT>, 
  book_ids LIST<UUID>, 
  year_written INT, 
  updated_at TIMESTAMP, 
  comments TEXT,
  PRIMARY KEY ((name), id) 
);
