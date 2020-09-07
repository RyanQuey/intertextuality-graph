package com.ryanquey.intertextualitygraph.models.books

import com.ryanquey.datautils.models.Model;

class Book(name : String) extends BookBase with Model {

  // empty constructor
  def this () = {
    this(null)
  }
}
