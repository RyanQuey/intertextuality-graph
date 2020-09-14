CREATE TABLE IF NOT EXISTS intertextuality_graph.texts (
  id TIMEUUID, 
  year_written INT, 
  author TEXT,
  canonical BOOLEAN, 
  canonical_text TEXT,
  starting_book TEXT,
  starting_chapter INT,
  starting_verse INT,
  ending_book TEXT,
  ending_chapter INT,
  ending_verse INT,
  // TODO make this a set? but then by default as easy to read...hmmm...
  split_passages LIST<TEXT>,
  testament TEXT, 
  greek_translation TEXT, 
  english_translation TEXT, 
  comments TEXT,
  // this is like through the play-app, or through treasury-of-scripture-knowledge, or ...(not user_id)
  created_by TEXT,
  updated_by TEXT,
  updated_at TIMESTAMP, 
  // allows for decent partitioning, and good sorting. Don't want to require chapter though, either for referencing or when adding rows, so don't make a partition key
  // Note that edge will have to reference it
  // not ussing uuid means only one text will be shared among all who reference this split passages
  PRIMARY KEY (starting_book, id)
);
