ALTER TABLE intertextuality_graph.text_from_book_edges
  WITH EDGE LABEL "from_book"
  FROM text(text_starting_book, text_id)
  TO book(book_name);
