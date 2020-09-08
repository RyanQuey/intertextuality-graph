package com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers
import java.util.Map

import com.ryanquey.datautils.models.{Model, Record}
import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse


object Helpers {
  def convertRawValue (record : Model, field : String, rawValue : String) = {
    val schema : Map[String, String] = record match {
      case b: Book => b.schema
      // TODO add the others
    }

    println(s"schema found: $schema")
    // note that this is a java map, not a scala map
    val fieldType : String = schema.get(field);

    println(s"field type for field $field: $fieldType")
    println(s"rawValue $rawValue")
    val value = fieldType match {
      case "String" => rawValue
      case "Integer" => rawValue.toInt
    }

    value
  }
}

