const os = require('os');
const fs = require('fs').promises;
const parse = require('csv-parse/lib/sync');
const stringify = require('csv-stringify/lib/sync')

const bcv_parser = require("bible-passage-reference-parser/js/en_bcv_parser").bcv_parser;
const bcv = new bcv_parser;

/* convert ref to list of refs, e.g., ps 1:3-4;2:5 > osis standard refs Ps.1.3-Ps.1.4,Ps.2.5
  * - we'll convert to ; delimited below, when stringifying.
  * - can then easily split by "," then by "-"
  */

// whether or not this file is our tsk file 
const isTsk = process.argv[1] == "tsk-true"

// absolute filepath to the csv file
const filepath = process.argv[0]
const formattedFilepath = isTsk ? filepath.replace(".txt", "-formatted.csv") : filepath.replace(".csv", "-formatted.csv")

const parseRef = (rawRef) => {
  return bcv.parse(rawRef).osis()
}

function parseRecord (rawRecord) {
  if (isTsk) {
    return parseTSKRecord(rawRecord)
  } 

  // currently assuming a user's csv file. Note that they might not have a super standard format,
  // but do have to follow our header guidelines
  // "source_texts", "alluding_text", "confidence_level",  "volume_level", "description", "comments",  "connection_type", "source_version",  "beale_categories",
  console.log("rawRecord: ", Object.keys(rawRecord))
  const rawSourceRefs = rawRecord.source_texts.split(";")

  const formattedRefs = rawRefs.map(parseRef)
  const parsedRecord = Object.assign(rawRecord)
  parsedRecord.refs = formattedRefs

  return parsedRecord
}

function parseTSKRecord (rawRecord) {
  
  const rawRefs = rawRecord.refs.split(";")
  const formattedRefs = rawRefs.map(parseRef)
  const parsedRecord = Object.assign(rawRecord)
  parsedRecord.refs = formattedRefs

  return parsedRecord
}

/*
 * the main file
 *
 */
const readParseWriteFile = async () => {
  // Read the content

  // before, with tsk was this, Trying with different encoding
  // const content = await fs.readFile(filepath)
  // Following this example:
  // https://csv.js.org/parse/recipies/file_interaction/#file-system-interaction
  console.log("== reading file ==")
  const content = await fs.readFile(filepath)
  console.log(content)

  // so far only TSK uses tab delimited csv files
  const delimiter = isTsk ? "\t" : ","
  
  // Parse the CSV content
  const rawRecords = parse(content, {
    delimiter,
    columns: true, // marks our csv as having headers, and using those headers for fields
    trim: true, // trim whitespae
    //relax: true, // needed if we want to get every line, even those with e.g., quotes (see line 17328)
    skipLinesWithError: true, // just skip them, there's not many and they're not important
  })

  console.log("first record:", rawRecords[0])
  const parsedRecords = rawRecords.map(parseRecord)
  
  // Print records to the console
  // records.map(record => console.log(record))
  
  console.log(`writing to: ${formattedFilepath}`)

  const columns = Object.keys(rawRecords[0])
  console.log("columns:", columns)

  // Join each field of object by a ___ delimter (I forgot what, maybe comma), to prepare to write to file
  const stringified = stringify(parsedRecords, {
    bom: isTsk ? true : false, // Byte order mark. Keep it since original had it too in TSK. Hopefully user ones don't have it...
    columns,
    header: true,
    cast: {
      // cast objects (ie arrays) like this
      object: (arr) => {
        // assuming is array for now
        // join by ;, so doesn't conflict with csv comma delimitation
        return {value: arr.join(";"), quote: false}
      }
    }
  })

  await fs.writeFile(formattedFilepath,  
    //BOM + our csv. NO HEADER YET. Join  each item in array of records by a newline (so csv has one line per record)
    stringified, 
    {encoding: 'utf8'})
}

readParseWriteFile()
.then(() => {console.log("finished!")})
.catch(console.error)

