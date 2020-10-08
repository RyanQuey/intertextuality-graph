import axios from 'axios'
import {osisData} from '../constants/osis-data'

const apiUrl = process.env.INTERTEXTUALITY_GRAPH_PLAY_API_URL || "http://localhost:9000"

export async function getBookData (bookName) { 
  try {
    const result = await axios.get(`${apiUrl}/books/${bookName}`)
    console.log("book data for ", bookName, result)
    return result.data

  } catch (err) {
    console.error("FAILED TO GET BOOK", err)
    // TODO probably better to have diff error, but for now throw it
    throw err
  }
}

export function osisToBookName (osisBookName) {
  const match = osisData.find(entry => entry.osisID == osisBookName)
  return match["Book Name"]
}
 
