import axios from 'axios'
import {osisData} from '../constants/osis-data'

const apiUrl = process.env.GATSBY_PLAY_API_URL || "http://localhost:9000"

export function osisToBookName (osisBookName) {
  const match = osisData.find(entry => entry.osisID == osisBookName)
  return match["Book Name"]
}
 
