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

export function downloadAsCSV(rows) {

  let csvContent = "data:text/csv;charset=utf-8,";

  rows.forEach(function(rowArray) {
        let row = rowArray.join(",");
        csvContent += row + "\r\n";
  });

  var encodedUri = encodeURI(csvContent);
  var link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute("download", "my_data.csv");
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
      // this one often has commas, so add quotes
      `"${edge.sourceText.split_passages}"`, 
      `"${edge.alludingText.split_passages}"`, 
      edge.confidence_level, 
      edge.volume_level, 
      edge.description, 
      edge.comments, 
      edge.connection_type, 
      edge.source_version, 
      edge.beale_categories, 
    ])
  })

  return rows
}

export function downloadGraphDataAsCSV(edges, vertices) {
  const rows = convertPathsWithValuesForCSV(edges, vertices)
  
  downloadAsCSV(rows)
}
