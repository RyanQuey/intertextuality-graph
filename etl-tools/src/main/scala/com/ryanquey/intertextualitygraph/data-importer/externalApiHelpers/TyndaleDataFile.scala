package com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers
import java.nio.file.{Path, Paths, Files}
import scalaj.http._
import scala.io.Source
import java.nio.charset.StandardCharsets


class TyndaleDataFile (key : String, url : String) {
  val dataDirPath : String = sys.env("INTERTEXTUALITY_GRAPH_RAW_DATA_DIR")
  val filename = s"${key}.txt";
  val fullPath : Path = Paths.get(dataDirPath, filename)
  val testament = key.slice(0, 2)
  var headers Array[String] = _;

  def downloadIfNecessary () = {

    if (Files.exists(fullPath)) {
      println(s"Found file $filename, not downloading")

    } else {
      val response: HttpResponse[String] = Http(url).asString
      val body = response.body
      // write to file

      println(s"Writing $filename to ${dataDirPath}")
      Files.write(fullPath, body.getBytes(StandardCharsets.UTF_8))
    }
  }

  def parseFile () = {
    // skip all the way until the actual data headers
    var foundHeader = false;
    for (line <- Source.fromFile(fullPath.toString()).getLines) {
      println(line)
      if (!foundHeader) {
        // if starts with Ref\t, it's the header (pretty hacky but whatever)
        // https://stackoverflow.com/questions/35048059/scala-escaping-newline-and-tab-characters
        if (line.startsWith("""Ref\t""")) {
          foundHeader = true;
          this.headers = line.split("""\t""")
        }
        // make sure not to go below if this is the line where the header is, or will try to parse the header as a row
      } else if (foundHeader) {
        val values = line.split(" ")
        for (value <- values) {
          value match {
            case "Ref in Heb" => {
              //Gen.1.7-03	


              //Gen.1.7-0
              //HTo	
              //H0853=אֵת
              //=obj./H9014=־
              //=link
    "Eng ref",
    "Pointed", 
    "Accented",  
    "Morphology",
    "Extended Strongs"
    )

    // NT
    "Ref", 
    Editions with this form
    Interlinear (Marvel)  
    Formatted Greek 
    Extended Strong # 
    Morphology  
    Lexical form  
    Meaning 
    Sub-meaning 
    Variants
              

          }
        }

      }
      
    }

  }


}
