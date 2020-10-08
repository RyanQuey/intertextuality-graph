import axios from 'axios'
import Helpers from './base-helpers'
const apiUrl = process.env.INTERTEXTUALITY_GRAPH_PLAY_API_URL || "http://localhost:9000"

/*
 * take an array of array of strings and turn into json
 * https://stackoverflow.com/a/14966131/6952495
 *
 * array needs to be in this format:
  const rows = [
      ["name1", "city1", "some other info"],
      ["name2", "city2", "more info"]
  ];
  downloadAsCSV(rows)
 *
 */
export function downloadAsCSV(rows, filename) {

  let csvContent = "data:text/csv;charset=utf-8,";

  rows.forEach(function(rowArray) {
    let row = rowArray.join(",");
    csvContent += row + "\r\n";
  });

  var encodedUri = encodeURI(csvContent);
  var link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute("download", filename);
  document.body.appendChild(link); // Required for FF

  link.click();
}

/*
 * takes paths with values and converts into something we can download as a CSV
 *
 * @param pathsWithValues 
 *    edge should look like this
 *    {
 *      sourceText: {"updated_at":["2020-10-07T20:09:38.091Z"],"split_passages":[["Exod.1.2-Exod.1.5"]],"starting_book":["Exodus"],"ending_chapter":[1],"ending_verse":[5],"ending_book":["Exodus"],"starting_verse":[2],"updated_by":["treasury-of-scripture-knowledge"],"starting_chapter":[1],"id":["017af598-01a1-11eb-836d-33546186cca1"],"created_by":["treasury-of-scripture-knowledge"]},
 *      alludingText: {"updated_at":["2020-10-07T20:11:33.761Z"],"split_passages":[["Matt.1.2"]],"starting_book":["Matthew"],"ending_chapter":[1],"ending_verse":[2],"ending_book":["Matthew"],"starting_verse":[2],"updated_by":["treasury-of-scripture-knowledge"],"starting_chapter":[1],"id":["b5dc8901-ff7f-11ea-824b-eb29555fd621"],"created_by":["treasury-of-scripture-knowledge"]},
 *      confidence_level: 50, // ie 50%
 *      connection_type: "allusion",
 *      updated_at: "2020-09-25T22:37:21.378Z",
 *    }
 *
 *    As we are exporting them, we are getting rid of updated_at and updated_by info. 
 *    The assumption is that if the user imports this later, it will be "updated_by" that user, and
 *    updated_at that date. 
 *
 *    The use case for this importer is not to be a db backup, but for users to copy each others'
 *    info, or a personal quick backup of the core info.
 *    
 *
 *    NOTE not actually using vertices currently, but might in hte future. Just getting vertex info
 *    from the edge
 */
export function convertPathsWithValuesForCSV(edges, vertices) {
  const headers = [
    "source_texts", "alluding_text", "confidence_level",  "volume_level", "description", "comments",  "connection_type", "source_version",  "beale_categories",
  ]

  const rows = [
    headers
  ]

  edges.forEach((edge) => {
    // map them out in same order as headers
    rows.push([
      // this one often has commas, so add quotes. 
      // It's pretty futile trying to make everything in semicolons, since DB lists in C* return as
      // comma separated, and osis parsers separate by comma too.
      `"${edge.sourceText.split_passages}"`, 
      `"${edge.alludingText.split_passages}"`, 
      edge.confidence_level, 
      edge.volume_level, 
      `"${edge.description}"`, 
      `"${edge.comments}"`, 
      edge.connection_type, 
      edge.source_version, 
      `"${edge.beale_categories}"`, 
    ])
  })

  return rows
}

export function downloadGraphDataAsCSV(edges, vertices, refString) {
  const rows = convertPathsWithValuesForCSV(edges, vertices)
  const filename = `i-graph-${refString}-${Helpers.timestamp()}-export.csv`
  
  downloadAsCSV(rows, filename)
}

// TODO remove this and get the uploadFile function back where it was, so it works for images
const _getData = (file) => {
  return new Promise((resolve, reject) => {
    let namedFile = new File([file], file.name, {type: file.type})

    return resolve(namedFile); 
  })
}

export const uploadCSVFile = (file) => {
  return new Promise((resolve, reject) => {
    const formData = new FormData()
    //rename to ensure unique path, and will work as link AND for background image
    //some of the regex is overkill...but whatever
    let newName = `${file.name.replace(/\(,|\/|\\|\s|\?|:|@|&|=|\+|#\)+/g, "_").replace(".csv", "")}-${Helpers.timestamp()}.csv`
    //NOTE: using the File API might make incompatibility with old IE11, Edge 16, old android
    let namedFile = new File([file], newName, {type: file.type})

    formData.append("userCSVFile", namedFile)
    // formData.append("user", store.getState().user)
    axios.post(`${apiUrl}/upload-csv`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      }
    })
    .then((result) => {
      const uploadedFile = result.data

      return resolve(uploadedFile)
    })
    .catch((err) => {
      console.log("fail to upload");
      console.error(err);
      return reject(err)
    })

  })
}
