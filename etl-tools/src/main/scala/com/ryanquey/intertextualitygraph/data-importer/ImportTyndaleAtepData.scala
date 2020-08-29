package com.ryanquey.intertextualitygraph.dataimporter
import scala.collection.immutable.Map
import scalaj.http._
import java.nio.file.{Path, Paths, Files}
import java.nio.charset.StandardCharsets

object ImportTyndaleStepData {
  val dataFilePath : Path = Paths.get(sys.env("INTERTEXTUALITY_GRAPH_RAW_DATA_DIR"))

  // note that urls are currently pointing towards Tyndale's GitHub, but I forked it so can use that in the future if I need to
  // urls are based on when I last modified this file, which might be as early as 08/30/2020
  val dataSourceUrls = Map(
    // SBL GNT with apparatus
    // "Greek text created from the SBLGNT+apparatus, following the decisions made by NA28, listing the major editions that also use that form (SBL, Treg, TR, Byz, WH, NA28). Variants are being added from major editions plus the 1st 4 centuries of MSS (from Bunning). All words are tagged lexically (extended Strong linked to LSJ) and morphologically (Robinson based on Tauber plus a few missing details) plus context-sensitive meanings for words with more than one meaning. For copyright reasons, any words, variants or punctuation that occur only in NA27 and/or in NA28 are omitted, so that this data cannot be used to reconstruct those texts."
    ("sbl-gnt-all", "https://raw.githubusercontent.com/tyndale/STEPBible-Data/master/TANTT%20-%20Tyndale%20Amalgamated%20NT%20Tagged%20texts%20-%20TyndaleHouse.com%20STEPBible.org%20CC%20BY-NC.txt"),

    // leningradensia
    // "The Leningrad codex based on Westminster via OpenScriptures, with full morphological and semantic tags for all words, prefixes and suffixes. Semantic tags use the extended Strongs linked to BDB by OS, is backwardly compatible with simple Strongs tags and includes all affixes (as defined in TBESH). Morphological tags are from ETCBC converted to the format of OS (similar to Westminster) with different morphology for Ketiv/Qere when needed."
    ("OT-gen-deut", "https://github.com/tyndale/STEPBible-Data/blob/master/TOTHT%20-%20Tyndale%20OT%20Hebrew%20Tagged%20text%20Gen-Deu%20-%20TyndaleHouse.com%20STEPBible.org%20CC%20BY-NC.txt"),
    ("OT-josh-esth", "https://raw.githubusercontent.com/tyndale/STEPBible-Data/master/TOTHT%20-%20Tyndale%20OT%20Hebrew%20Tagged%20text%20Jos-Est%20-%20TyndaleHouse.com%20STEPBible.org%20CC%20BY-NC.txt"),
    ("OT-job-songs", "https://raw.githubusercontent.com/tyndale/STEPBible-Data/master/TOTHT%20-%20Tyndale%20OT%20Hebrew%20Tagged%20text%20Job-Sng%20-%20TyndaleHouse.com%20STEPBible.org%20CC%20BY-NC.txt"),
    ("OT-isa-mal", "https://raw.githubusercontent.com/tyndale/STEPBible-Data/master/TOTHT%20-%20Tyndale%20OT%20Hebrew%20Tagged%20text%20Isa-Mal%20-%20TyndaleHouse.com%20STEPBible.org%20CC%20BY-NC.txt")
  )

  // https://github.com/tyndale/STEPBible-Data#data-format
  val tyndaleOTAbbrs = Map(
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
  val tyndaleApocryphalAbbrs = Map(
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
  val alternateMSS = Map(
		("JsA" -> ""),
		("JdB" -> ""),
		("TbS" -> ""),
		("SsT" -> ""),
		("DnT" -> ""),
		("BlT" -> "")
  )
  val tyndaleNTAbbrs = Map(
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

  def main (args: Array[String]) = {
    // get the data files
    for ((key, url) <- dataSourceUrls) {  
      val response: HttpResponse[String] = Http(url).asString
      val body = response.body
      // write to file
      val filename = s"${key}.txt";

      println(s"Writing $filename to ${dataFilePath.toString()}")
      Files.write(Paths.get(dataFilePath.toString(), filename), body.getBytes(StandardCharsets.UTF_8))
    }
  }
}
