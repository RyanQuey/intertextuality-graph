CREATE TABLE IF NOT EXISTS intertextuality_graph.intertextual_connections (
  // table for the edge
  id UUID,
  confidence_level FLOAT,
  source_text_id UUID,
  source_text_starting_book TEXT,
  source_text_friendly TEXT,
  alluding_text_id UUID,
  alluding_text_starting_book TEXT,
  alluding_text_friendly TEXT,
  user_id UUID,
  beale_categories LIST<TEXT>,
  connection_type TEXT, 
  connection_significance TEXT, 
  comments TEXT,
  // not just different languages
  source_version TEXT,
  source_language TEXT,
  updated_at TIMESTAMP, 
  // this will allow good default order in the db when we do basic selects. 
  PRIMARY KEY (id)
);
