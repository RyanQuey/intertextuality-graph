import axios from 'axios'
import Helpers from '../helpers/base-helpers'
import _ from 'lodash'

const apiUrl = process.env.INTERTEXTUALITY_GRAPH_PLAY_API_URL || "http://localhost:9000"
const verticesUrlBase = apiUrl + "/sources-for-ref-with-alluding-texts"
const pathsUrlBase = apiUrl + "/paths-for-sources-starting-with-ref"

/*
 * receives a object with keys: alludingText, sourceText. Each of those keys is an object with keys
 * corresponding to our db fields, but in camelcase
 *
 */
export async function getPathsForRef (book, chapter, verse) { 
  try {
    // filter out parts that don't exist
    const queryParts = _.pickBy({book, chapter, verse})
    // const query = `book=${startingBook}&chapter=1&verse=1`
    const qs = Helpers.toQueryString(queryParts)

    const result = await axios.get(`${pathsUrlBase}?${qs}`)
    return result.data

  } catch (err) {
    console.error(err)
    throw err
  }
}
/*
 * gets both nodes that start with this ref and goes out one edge
 */ 
export async function getVerticesForRef (book, chapter, verse) { 
  try {
    // filter out parts that don't exist
    const queryParts = _.pickBy({book, chapter, verse})
    const qs = Helpers.toQueryString(queryParts)

    const result = await axios.get(`${verticesUrlBase}?${qs}`)
    return result.data

  } catch (err) {
    console.error(err)
    throw err
  }
}

export async function createConnection (connectionData) { 
  try {
    const result = await axios.post(`${apiUrl}/connections`, connectionData)
    return result.data

  } catch (err) {
    console.error(err)
    throw err
  }
}
