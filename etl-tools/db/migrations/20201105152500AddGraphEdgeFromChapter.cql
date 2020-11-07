ALTER TABLE intertextuality_graph.text_from_chapter_edges
  WITH EDGE LABEL "from_chapter"
  FROM text(text_starting_book, text_id)
  TO chapter(chapter_book, chapter_number);
