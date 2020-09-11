const os = require('os');
const fs = require('fs').promises;
const parse = require('csv-parse/lib/sync');
const stringify = require('csv-stringify/lib/sync')

const bcv_parser = require("bible-passage-reference-parser/js/en_bcv_parser").bcv_parser;
const bcv = new bcv_parser;

const dataDir = `${process.env.INTERTEXTUALITY_GRAPH_RAW_DATA_DIR}/treasury-of-scripture-knowledge`


// convert ref to list of refs, e.g., ps 1:3-4;2:5 > Ps.1.3-Ps.1.4,Ps.2.5
// we'll convert to ; delimited below, when stringifying.
// can then easily split by "," then by "-"
const parseRef = (rawRef) => {
  return bcv.parse(rawRef).osis()
}

const parseRecord = (rawRecord) => {
  
  const rawRefs = rawRecord.refs.split(";")
  const formattedRefs = rawRefs.map(parseRef)
  const parsedRecord = Object.assign(rawRecord)
  parsedRecord.refs = formattedRefs

  return parsedRecord
}

const readParseWriteFile = async () => {
  // Read the content
  const content = await fs.readFile(`${dataDir}/tsk-cli.txt`)
  
  // Parse the CSV content
  const rawRecords = parse(content, {
    delimiter: "\t",
    columns: true, // marks our csv as having headers, and using those headers for fields
    trim: true, // trim whitespae
    //relax: true, // needed if we want to get every line, even those with e.g., quotes (see line 17328)
    skipLinesWithError: true, // just skip them, there's not many and they're not important
  })

  console.log(rawRecords[0])

  const parsedRecords = rawRecords.map(parseRecord)
  
  // Print records to the console
  // records.map(record => console.log(record))
  
  console.log("writing to: " + `${dataDir}/tsk-cli-formatted.csv`)

  const columns = Object.keys(rawRecords[0])
  console.log("columns:", columns)

  // Join each field of object by a tab delimter, to prepare to write to file
  const stringified = stringify(parsedRecords, {
    bom: true, // Byte order mark. Keep it since original had it too
    columns,
    header: true,
    cast: {
      // cast objects (ie arrays ) like this
      object: (arr) => {
        // assuming is array for now
        // join by ;, so doesn't conflict with csv comma delimitation
        return {value: arr.join(";"), quote: false}
      }
    }
  })

  await fs.writeFile(`${dataDir}/tsk-cli-formatted.csv`,  
    //BOM + our csv. NO HEADER YET. Join  each item in array of records by a newline (so csv has one line per record)
    stringified, 
    {encoding: 'utf8'})
}

readParseWriteFile()
.then(() => {console.log("finished!")})
.catch(console.error)

