package com.ryanquey.intertextualitygraph.dataimporter.externalApiHelpers
import java.nio.file.{Path, Paths, Files}
import scalaj.http._
import scala.io.Source
import java.nio.charset.StandardCharsets
import org.apache.commons.csv.{CSVParser, CSVRecord, CSVFormat.Predefined}
import com.ryanquey.intertextualitygraph.dataimporter.constants.TheographicConstants._



class TheographicDataFile (tablename : String, filename : String) {
  val dataFilePath : String = s"${sys.env("INTERTEXTUALITY_GRAPH_RAW_DATA_DIR")}/theographic-airtable-csvs"
  val fullPath : Path = Paths.get(dataFilePath, filename)
  var headers Array[String] = _;

  def parseFile (table : String) = {
    val bufferedSource = Source.fromFile()
    val fieldsMapping = table case {
      match "books": booksFieldsMapping,
      match "chapters": chaptersFieldsMapping,
      match "verses": versesFieldsMapping
    }

	  File csvData = new File(fullPath);
    // cannot just split by comma, since many fields have multiples (so commas separate) or commas inside fields
		CSVParser parser = CSVParser.parse(csvData, CSVFormat.RFC4180);

	 	for (CSVRecord csvRecord <- parser) {
	 	  // iterate over our mapping to get what fields we want into the db columns that they map to
      for ((csvCol, dbCol) <- fieldsMapping) {  
        csvRecord.get(csvCol)



      
    }

  }


}
