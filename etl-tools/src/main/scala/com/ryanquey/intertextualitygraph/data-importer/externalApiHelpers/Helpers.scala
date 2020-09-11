package com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers
import java.util.Map
import java.util.UUID;

import com.ryanquey.datautils.models.{Model, Record}
import com.ryanquey.intertextualitygraph.models.books.Book
import com.ryanquey.intertextualitygraph.models.chapters.Chapter
import com.ryanquey.intertextualitygraph.models.verses.Verse


object Helpers {
  // field is modelField
  def convertRawValue (modelInstance : Model, field : String, rawValue : String) : Any = {
    val schema : Map[String, String] = modelInstance match {
      case b: Book => b.schema
      case c: Chapter => c.schema
      case v: Verse => v.schema
    }

    println(s"schema found: $schema")
    // note that this is a java map, not a scala map
    val fieldType : String = schema.get(field);

    println(s"field type for field $field: $fieldType")
    println(s"rawValue $rawValue")

    // for csvs, blank string means nothing there
    if (rawValue == "") {
      return null
    }

    val value = modelInstance match {
      case b: Book => booksFieldsConverter(rawValue, field, fieldType)
      case c: Chapter => chaptersFieldsConverter(rawValue, field, fieldType)
      case v: Verse => versesFieldsConverter(rawValue, field, fieldType)
    }

    value
  }


  /*
   * Other helpers
   */

  // just a simple typecast based on schema
  def typecast (rawValue : String, fieldType : String) = {
    val value = fieldType match {
      case "String" => rawValue
      // TODO handle if empty string
      case "Integer" => rawValue.toInt
      case "UUID" => UUID.fromString(rawValue)
    }

    println(s"typecasting to $value")
    value
  }

  /*
   * converters for the different tables
   *
   */


  def booksFieldsConverter (rawValue : String, field : String, fieldType : String) = {
    val value = field match {
      // they return a string, get the int at the end and use instead
      case "writers" => {
        val all = rawValue.split(",")

        // just store the first, since it's most likely the best
        all(0)
      }

      case _ => typecast(rawValue, fieldType)
    }  

    value
  }

  def chaptersFieldsConverter (rawValue : String, field : String, fieldType : String) = {
    val value = field match {
      // they return a string, get the int at the end and use instead
      case "verseCount" => {
        val vss = rawValue.split(",")
        val length = vss.length
        val last = vss(length -1)

        // first is book, then chapter, so get third
        last.split("\\.")(2).toInt
      }

      case _ => typecast(rawValue, fieldType)
    }
    
    value
  }

  def versesFieldsConverter (rawValue : String, field : String, fieldType : String) = {
    val value = field match {
      // they return a string, get the int at the end and use instead
      case "chapter" => rawValue.split("\\.")(1).toInt

      case _ => typecast(rawValue, fieldType)
    }

    value
    
  }
}

