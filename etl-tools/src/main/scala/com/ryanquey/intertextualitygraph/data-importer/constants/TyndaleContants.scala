package com.ryanquey.intertextualitygraph.dataimporter
import scala.collection.immutable.Map

object TyndaleConstants {
  // https://github.com/tyndale/STEPBible-Data#data-format
  val otBooksAbbrs = Map(
		("Gen" -> "Genesis"),
		("Exo" -> "Exodus"),
		("Lev" -> "Leviticus"),
		("Num" -> "Numbers"),
		("Deu" -> "Deuteronomy"),
		("Jos" -> "Joshua"),
		("Jdg" -> "Judges"),
		("Rut" -> "Ruth"),
		("1Sa" -> "1 Samuel"),
		("2Sa" -> "2 Samuel"),
		("1Ki" -> "1 Kings"),
		("2Ki" -> ""),
		("1Ch" -> ""),
		("2Ch" -> ""),
		("Ezr" -> ""),
		("Neh" -> ""),
		("Est" -> ""),
		("Job" -> ""),
		("Psa" -> ""),
		("Pro" -> ""),
		("Ecc" -> ""),
		("Sng" -> ""),
		("Isa" -> ""),
		("Jer" -> ""),
		("Lam" -> ""),
		("Ezk" -> ""),
		("Dan" -> ""),
		("Hos" -> ""),
		("Jol" -> ""),
		("Amo" -> ""),
		("Oba" -> ""),
		("Jon" -> ""),
		("Mic" -> ""),
		("Nam" -> ""),
		("Hab" -> ""),
		("Zep" -> ""),
		("Hag" -> ""),
		("Zec" -> ""),
		("Mal" -> "")
  )
  val apocryphalBooksAbbrs = Map(
		("Tob" -> ""),
		("Jdt" -> ""),
		("EsG" -> ""),
		("Wis" -> ""),
		("Sir" -> ""),
		("Bar" -> ""),
		("LJe" -> ""),
		("S3Y" -> ""),
		("Sus" -> ""),
		("Bel" -> ""),
		("1Ma" -> ""),
		("2Ma" -> ""),
		("3Ma" -> ""),
		("4Ma" -> ""),
		("1Es" -> ""),
		("2Es" -> ""),
		("Man" -> ""),
		("Ps2" -> ""),
		("Oda" -> ""),
		("PsS" -> "")
  )
  val alternateMSSAbbrs = Map(
		("JsA" -> ""),
		("JdB" -> ""),
		("TbS" -> ""),
		("SsT" -> ""),
		("DnT" -> ""),
		("BlT" -> "")
  )
  val ntBooksAbbrs = Map(
		("Mat" -> ""),
		("Mrk" -> ""),
		("Luk" -> ""),
		("Jhn" -> ""),
		("Act" -> ""),
		("Rom" -> ""),
		("1Co" -> ""),
		("2Co" -> ""),
		("Gal" -> ""),
		("Eph" -> ""),
		("Php" -> ""),
		("Col" -> ""),
		("1Th" -> ""),
		("2Th" -> ""),
		("1Ti" -> ""),
		("2Ti" -> ""),
		("Tit" -> ""),
		("Phm" -> ""),
		("Heb" -> ""),
		("Jas" -> ""),
		("1Pe" -> ""),
		("2Pe" -> ""),
		("1Jn" -> ""),
		("2Jn" -> ""),
		("3Jn" -> ""),
		("Jud" -> ""),
		("Rev" -> "")
  )

  val dataFileHeaders = Map(


