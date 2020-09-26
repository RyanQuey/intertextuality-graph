import axios from 'axios'
import Helpers from '../helpers/base-helpers'

const apiUrl = process.env.INTERTEXTUALITY_GRAPH_PLAY_API_URL || "http://localhost:9000"

/*
 * receives a object with keys: alludingText, sourceText. Each of those keys is an object with keys
 * corresponding to our db fields, but in camelcase
 *
 */
export async function createConnection (connectionData) { 
  try {
    const result = await axios.post(`${apiUrl}/connections`, connectionData)
    console.log(result)
    return result.data

  } catch (err) {
    console.error(err)
    throw err
  }
}
