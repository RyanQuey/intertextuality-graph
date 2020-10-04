import axios from 'axios'
import Helpers from '../helpers/base-helpers'
import _ from 'lodash'

const apiUrl = process.env.INTERTEXTUALITY_GRAPH_PLAY_API_URL || "http://localhost:9000"
const verticesUrlBase = apiUrl + "/texts/sources-for-ref-with-alluding-texts"
const pathsUrlBase = apiUrl + "/texts/paths-for-sources-starting-with-ref"
// switch to url that returns all values for all vertices along the way
const pathsWithValuesUrlBase = apiUrl + "/texts/all-values-along-path-for-ref"

/*
 * receives a object with keys: alludingText, sourceText. Each of those keys is an object with keys
 * corresponding to our db fields, but in camelcase
 *
 */
export async function getPathsWithValuesForRef (book, chapter, verse, hopsCount) { 
  try {
    // filter out parts that don't exist
    const queryParts = _.pickBy({book, chapter, verse, hopsCount})
    // const query = `book=${startingBook}&chapter=1&verse=1&hopsCount=2`
    const qs = Helpers.toQueryString(queryParts)

    const result = await axios.get(`${pathsWithValuesUrlBase}?${qs}`)
    return result.data

  } catch (err) {
    console.error(err)
    throw err
  }
}
export async function getPathsForRef (book, chapter, verse, hopsCount) { 
  try {
    // filter out parts that don't exist
    const queryParts = _.pickBy({book, chapter, verse, hopsCount})
    // const query = `book=${startingBook}&chapter=1&verse=1&hopsCount=2`
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
export async function getVerticesForRef (book, chapter, verse, hopsCount) { 
  try {
    // filter out parts that don't exist
    const queryParts = _.pickBy({book, chapter, verse, hopsCount})
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

/*
 * takes raw graphson and converts to nodes and edges data that we can hand to vega
 * TODO eventually will want to improve performance of this kind of iteration, first of all by doing
 * more logic in the backend, second of all by how we're doing dedup stuff
 */
export function extractNodesAndEdgesFromPaths (pathsWithValues) {
  console.log("pathsWithValues", pathsWithValues)
  // nodes in hte chart (a text)
  const vertices = []
  // edges between the nodes in teh chart
  const edges = []

  pathsWithValues.forEach((path) => {
    // drill into objects
    const verticesInPath = path.objects
    verticesInPath.forEach((vertex, vertexIndex) => {
      // add each vertex as a node in teh chart
      // TODO maybe dedupe here? 
      vertices.push(vertex)

      // break apart each chain in the path, so each has only two segments
      const nextVInPath = verticesInPath[vertexIndex + 1]
      // if there's another Vertex in the past after this:
      if (nextVInPath) {
        // currently just replicating what we received before, since our Vega config is expecting in
        // handling it that way
        const newEdge = {labels: [], objects: [vertex, nextVInPath]}
        edges.push(newEdge)
      }
    })
  })


  // dedupe by id
  const dedupedVertices = _.uniqBy(vertices, function (e) {
    return e.id[0];
  });

  return [edges, dedupedVertices]
}
