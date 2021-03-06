import axios from 'axios'
import Helpers from '../helpers/base-helpers'
import _ from 'lodash'

const apiUrl = process.env.GATSBY_PLAY_API_URL || "http://localhost:9000"
const verticesUrlBase = apiUrl + "/texts/sources-for-ref-with-alluding-texts"

/*
 * receives a object with keys: alludingText, sourceText. Each of those keys is an object with keys
 * corresponding to our db fields, but in camelcase
 * - older function, might not use at all in the future
 * params: {book, chapter, verse, hopsCount, dataSet}
 */
export async function getTextsRefAlludesTo (params = {}) { 
  try {
    // filter out parts that don't exist
    const queryParts = _.pickBy(params)
    // const query = `book=${startingBook}&chapter=1&verse=1&hopsCount=2`
    const qs = Helpers.toQueryString(queryParts)

    const result = await axios.get(`${apiUrl}/texts/alluded-to-by-ref/paths?${qs}`)
    return result.data

  } catch (err) {
    console.error(err)
    throw err
  }
}

/*
* same api as getTextsRefAlludesTo
* - older function, might not use at all in the future
*/
export async function getTextsAlludedToByRef (params = {}) { 
  try {
    // filter out parts that don't exist
    const queryParts = _.pickBy(params)
    // const query = `book=${startingBook}&chapter=1&verse=1&hopsCount=2`
    const qs = Helpers.toQueryString(queryParts)

    const result = await axios.get(`${apiUrl}/texts/allude-to-ref/paths?${qs}`)
    return result.data

  } catch (err) {
    console.error(err)
    throw err
  }
}


/**
 * this is basically our advanced search
 */
export async function tracePathsFilteredByHop (params = {}) { 
  try {
    // filter out parts that don't exist
    const result = await axios.post(`${apiUrl}/texts/trace-paths-filtered-by-hop`, params)

    return result.data

  } catch (err) {
    console.error(err)
    throw err
  }
}

/**
 * takes the form param values we have in redux and converts to what we send to our play api
 */
export function convertHopSetFormValuesToPostBody (hopSetValues) {
  const arr = _.values(hopSetValues)
  const parsedArr = arr.map(hopSet => {
    return {
      allusionDirection: hopSet.allusionDirection,
      dataset: hopSet.dataset || "all",
      referenceOsis: hopSet.reference.osis,
    }
  })

  return {
    hopParamsSets: parsedArr
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
 * - assumes that each item in the objects array is a VERTEX (not mixed, if mixed vertices and edges
 *   use extractNodesAndEdgesFromMixedPaths
 *
 * TODO eventually will want to improve performance of this kind of iteration, first of all by doing
 * more logic in the backend, second of all by how we're doing dedup stuff
 */
export function extractNodesAndEdgesFromPaths (pathsWithValues) {
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

/*
 * takes raw graphson and converts to nodes and edges data that we can hand to vega
 * - assumes that each item in the objects array is alternating: one vertex and edge and vertex and
 *   edge. 
 *   - if only vertices, use extractNodesAndEdgesFromMixedPaths
 */
export function extractNodesAndEdgesFromMixedPaths (pathsWithValues) {
    console.log("pathsWithValues", pathsWithValues.slice(0,99).map(p => p.objects[0].starting_chapter[0] + ":" + p.objects[0].starting_verse[0]))

  // nodes in hte chart (a text)
  const vertices = []
  // edges between the nodes in teh chart
  const edges = []

  pathsWithValues.forEach((path) => {
    // drill into objects
    const itemsInPath = path.objects
    let finished = false

    // increment by two, to skip the edge
    for (let i = 0; i < itemsInPath.length; i += 2) {
      let vertex = itemsInPath[i]

      vertices.push(vertex)

      // next item, if exists, is an edge
      const edge = itemsInPath[i + 1]
      // next vertex. Don't add for now though, will do on next iteration
      const nextVInPath = itemsInPath[i + 2]

      if (edge && !nextVInPath) {
        // something didn't work!!!!
        throw new Error("had edge but no 2nd vertex...", edge, nextVInPath)
      } else if (edge) {
        // currently just replicating what we received before, since our Vega config is expecting in
        // handling it that way
        const newEdge = Object.assign(edge, {sourceText: vertex, alludingText: nextVInPath})
        edges.push(newEdge)
      }

      // skip the edge and continue
    }
  })

  // dedupe by id
  const dedupedVertices = _.uniqBy(vertices, function (e) {
    return e.id[0];
  });

  return [edges, dedupedVertices]
}
