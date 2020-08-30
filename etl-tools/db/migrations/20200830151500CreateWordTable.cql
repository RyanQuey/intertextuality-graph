CREATE TABLE IF NOT EXISTS intertextuality_graph.words (
  id UUID, 
  word
  verse_id UUID,
  chapter_id UUID,
  book_id UUID,
  greek_translation TEXT, 
  english_translation TEXT, 
  user_id UUID, 
  comments TEXT,
  updated_at TIMESTAMP, 
  PRIMARY KEY (id)
);
