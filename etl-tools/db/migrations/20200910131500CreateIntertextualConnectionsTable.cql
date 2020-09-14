CREATE TABLE IF NOT EXISTS intertextuality_graph.intertextual_connections (
  // how confident we can be that this connection was meant by original author
  confidence_level FLOAT,

  // how important this connection is in teh argument of original author. Hays' criteria "volume"
  volume_level FLOAT,
  source_text_id TIMEUUID,
  source_text_starting_book TEXT,
  alluding_text_id TIMEUUID,
  alluding_text_starting_book TEXT,
  // who created this connection
  user_id UUID,
  beale_categories LIST<TEXT>,
  // e.g., allusion, quote, echo, etc (?). Use list of constants to validate this field
  connection_type TEXT, 
  connection_significance TEXT, 
  comments TEXT,
  // not just different languages. E.g., can be Theodotian vs other LXX versions
  source_version TEXT,
  source_language TEXT,
  updated_at TIMESTAMP, 
  // this will allow good default order in the db when we do basic selects. 
  PRIMARY KEY (source_text_id, source_text_starting_book, alluding_text_id, alluding_text_starting_book)
);
