package com.ryanquey.intertextualitygraph.dataimporter
import scala.collection.immutable.Map
import com.ryanquey.intertextualitygraph.dataimporter.models._

object ImportTyndaleStepData {
  /*
   * TODO eventually will probably extract data for each word from these, but for now just getting each verse
   * Having this much will make it easy to eventually get other things from similar text files, or from these same tax files but extracting more data like getting the word data
   */
  val dataFilePath : String = sys.env("INTERTEXTUALITY_GRAPH_RAW_DATA_DIR")

  // note that urls are currently pointing towards Tyndale's GitHub, but I forked it so can use that in the future if I need to
  // urls are based on when I last modified this file, which might be as early as 08/30/2020
  val dataSourceUrls = Map(
    // SBL GNT with apparatus
    // "Greek text created from the SBLGNT+apparatus, following the decisions made by NA28, listing the major editions that also use that form (SBL, Treg, TR, Byz, WH, NA28). Variants are being added from major editions plus the 1st 4 centuries of MSS (from Bunning). All words are tagged lexically (extended Strong linked to LSJ) and morphologically (Robinson based on Tauber plus a few missing details) plus context-sensitive meanings for words with more than one meaning. For copyright reasons, any words, variants or punctuation that occur only in NA27 and/or in NA28 are omitted, so that this data cannot be used to reconstruct those texts."
    ("NT.sblgnt.all", "https://raw.githubusercontent.com/tyndale/STEPBible-Data/master/TANTT%20-%20Tyndale%20Amalgamated%20NT%20Tagged%20texts%20-%20TyndaleHouse.com%20STEPBible.org%20CC%20BY-NC.txt"),

    // leningradensia
    // "The Leningrad codex based on Westminster via OpenScriptures, with full morphological and semantic tags for all words, prefixes and suffixes. Semantic tags use the extended Strongs linked to BDB by OS, is backwardly compatible with simple Strongs tags and includes all affixes (as defined in TBESH). Morphological tags are from ETCBC converted to the format of OS (similar to Westminster) with different morphology for Ketiv/Qere when needed."
    ("OT.Leningrad.gen-deut", "https://raw.githubusercontent.com/tyndale/STEPBible-Data/master/TOTHT%20-%20Tyndale%20OT%20Hebrew%20Tagged%20text%20Gen-Deu%20-%20TyndaleHouse.com%20STEPBible.org%20CC%20BY-NC.txt"),
    ("OT.Leningrad.josh-esth", "https://raw.githubusercontent.com/tyndale/STEPBible-Data/master/TOTHT%20-%20Tyndale%20OT%20Hebrew%20Tagged%20text%20Jos-Est%20-%20TyndaleHouse.com%20STEPBible.org%20CC%20BY-NC.txt"),
    ("OT.Leningrad.job-songs", "https://raw.githubusercontent.com/tyndale/STEPBible-Data/master/TOTHT%20-%20Tyndale%20OT%20Hebrew%20Tagged%20text%20Job-Sng%20-%20TyndaleHouse.com%20STEPBible.org%20CC%20BY-NC.txt"),
    ("OT.Leningrad.isa-mal", "https://raw.githubusercontent.com/tyndale/STEPBible-Data/master/TOTHT%20-%20Tyndale%20OT%20Hebrew%20Tagged%20text%20Isa-Mal%20-%20TyndaleHouse.com%20STEPBible.org%20CC%20BY-NC.txt")
  )


  def main (args: Array[String]) = {
    // get the data files
    for ((key, url) <- dataSourceUrls) {  
      val dataFile = new TyndaleDataFile(key, url);

      dataFile.downloadIfNecessary()
      dataFile.parseFile()
    }
  }
}
