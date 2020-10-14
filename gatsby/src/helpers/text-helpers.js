import axios from 'axios'
import Helpers from './base-helpers'
import {osisToBookName} from './book-helpers'
import {osisData} from '../constants/osis-data'
import bookData from '../data/books';


const apiUrl = process.env.GATSBY_PLAY_API_URL || "http://localhost:9000"

  /* takes the return value of parsed_entities() and returns Boolean if valid
  */
export function osisDataIsValid (osisData) {
    // only should have a single entity, no more and no less
    // that single entity should have an osis reference
  return osisData.length == 1 && Helpers.safeDataPath(osisData, "0.osis", false)
}
export function osisDataValue (osisData) {
  return osisDataIsValid(osisData) ? Helpers.safeDataPath(osisData, "0.osis", "") : "invalid"
}

// assumes only one entry, which is all that we are allowing actually currently
export function startingRefFromOsis (osisData) {
  return osisData[0].entities[0].start
}
export function startingBookFromOsis (osisData) {
  // watch out, will be e.g., Exod (osis format) not Exodus 
  const osisBookName = startingRefFromOsis(osisData).b
  return osisToBookName(osisBookName)
}
export function startingChapterFromOsis (osisData) {
  return startingRefFromOsis(osisData).c
}
export function startingVerseFromOsis (osisData) {
  return startingRefFromOsis(osisData).v
}

export function endingRefFromOsis (osisData) {
  return osisData[0].entities[0].end
}
export function endingBookFromOsis (osisData) {
  // watch out, will be e.g., Exod (osis format) not Exodus 
  const osisBookName = endingRefFromOsis(osisData).b
  return osisToBookName(osisBookName)
}
export function endingChapterFromOsis (osisData) {
  return endingRefFromOsis(osisData).c
}
export function endingVerseFromOsis (osisData) {
  return endingRefFromOsis(osisData).v
}

// returns either "Old Testament" or "New Testament"
// assuming starting ref will be in same testament as ending ref
export function osisDataToTestament (osisRefData) {
  const osisBookName = startingRefFromOsis(osisRefData).b
  const match = osisData.find(entry => entry.osisID == osisBookName)
  return match ? match["book set"] : ""
}
 
