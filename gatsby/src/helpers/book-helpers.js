import axios from 'axios'

const apiUrl = process.env.INTERTEXTUALITY_GRAPH_PLAY_API_URL || "http://localhost:9000"

export async function getBookData (bookName) { 
  try {
    const result = await axios.get(`${apiUrl}/books/${bookName}`)
    console.log(result)
    return result.data

  } catch (err) {
    console.error(err)
  }
}
