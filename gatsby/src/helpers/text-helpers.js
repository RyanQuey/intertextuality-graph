import axios from 'axios'
import Helpers from '../helpers/base-helpers'

const apiUrl = process.env.INTERTEXTUALITY_GRAPH_PLAY_API_URL || "http://localhost:9000"

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
  return startingRefFromOsis(osisData).b
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
  return endingRefFromOsis(osisData).b
}
export function endingChapterFromOsis (osisData) {
  return endingRefFromOsis(osisData).c
}
export function endingVerseFromOsis (osisData) {
  return endingRefFromOsis(osisData).v
}
