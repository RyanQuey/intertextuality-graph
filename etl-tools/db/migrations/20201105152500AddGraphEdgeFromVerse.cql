ALTER TABLE intertextuality_graph.text_from_verse_edges
  WITH EDGE LABEL "from_verse"
  FROM text(text_starting_book, text_id)
  TO verse(verse_book, verse_chapter, verse_number);
