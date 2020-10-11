ALTER TABLE intertextuality_graph.intertextual_connections
  WITH EDGE LABEL "alludes_to"
  FROM text(alluding_text_starting_book, alluding_text_id)
  TO text(source_text_starting_book, source_text_id);
