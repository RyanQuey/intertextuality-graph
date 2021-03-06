import axios from 'axios'

const apiUrl = process.env.GATSBY_PLAY_API_URL || "http://localhost:9000"

export async function getChapterData (bookName, chapter) { 
  try {
    const result = await axios.get(`${apiUrl}/books/${bookName}/chapters/${chapter}`)
    console.log("chapter data for chapter " + chapter, result)
    return result.data
  } catch (err) {
    console.error(err)
    throw err
  }
}
