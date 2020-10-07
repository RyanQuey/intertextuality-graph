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
 *  if it only had one path would look like this:
 * "[{
 *    "labels":[[],[],[],[],[]],
 *    "objects":[
 *      {"updated_at":["2020-10-07T20:09:38.091Z"],"split_passages":[["Exod.1.2-Exod.1.5"]],"starting_book":["Exodus"],"ending_chapter":[1],"ending_verse":[5],"ending_book":["Exodus"],"starting_verse":[2],"updated_by":["treasury-of-scripture-knowledge"],"starting_chapter":[1],"id":["017af598-01a1-11eb-836d-33546186cca1"],"created_by":["treasury-of-scripture-knowledge"]},{"updated_at":["2020-10-07T20:11:33.761Z"],"split_passages":[["Matt.1.2"]],"starting_book":["Matthew"],"ending_chapter":[1],"ending_verse":[2],"ending_book":["Matthew"],"starting_verse":[2],"updated_by":["treasury-of-scripture-knowledge"],"starting_chapter":[1],"id":["b5dc8901-ff7f-11ea-824b-eb29555fd621"],"created_by":["treasury-of-scripture-knowledge"]},{"updated_at":["2020-10-07T20:10:33.709Z"],"split_passages":[["Gen.11.24-Gen.11.32","Josh.24.2","1Chr.1.24-1Chr.1.28"]],"starting_book":["Luke"],"ending_chapter":[3],"ending_verse":[34],"ending_book":["Luke"],"starting_verse":[34],"updated_by":["treasury-of-scripture-knowledge"],"starting_chapter":[3],"id":["b0643c70-ff7f-11ea-824b-eb29555fd621"],"created_by":["treasury-of-scripture-knowledge"]},{"updated_at":["2020-10-07T20:11:33.761Z"],"split_passages":[["Matt.1.2"]],"starting_book":["Matthew"],"ending_chapter":[1],"ending_verse":[2],"ending_book":["Matthew"],"starting_verse":[2],"updated_by":["treasury-of-scripture-knowledge"],"starting_chapter":[1],"id":["b5dc8901-ff7f-11ea-824b-eb29555fd621"],"created_by":["treasury-of-scripture-knowledge"]},{"updated_at":["2020-10-07T20:10:33.709Z"],"split_passages":[["Gen.11.24-Gen.11.32","Josh.24.2","1Chr.1.24-1Chr.1.28"]],"starting_book":["Luke"],"ending_chapter":[3],"ending_verse":[34],"ending_book":["Luke"],"starting_verse":[34],"updated_by":["treasury-of-scripture-knowledge"],"starting_chapter":[3],"id":["b0643c70-ff7f-11ea-824b-eb29555fd621"],"created_by":["treasury-of-scripture-knowledge"]}]}]"
 *
 */
export function convertPathsWithValuesForCSV(pathsWithValues) {
  const headers = [
    "source_texts", "alluding_text", "confidence_level",  "volume_level", "description", "comments",  "connection_type", "source_version",  "beale_categories",
  ]

  return pathsWithValues[0].objects
}
