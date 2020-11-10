import axios from 'axios'
import _ from "lodash"
import Helpers from '../helpers/base-helpers'
import {
  NEW_ALERT,
  CLOSE_ALERTS,
} from '../constants/action-types'
import store from "../reducers"
import {
  alertActions,
  formActions,
} from "../actions"
const apiUrl = process.env.GATSBY_PLAY_API_URL || "http://localhost:9000"

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

/*
 * async not implemented correctly
 */ 
export async function fetchBookData (bookName, hopSetIndex = 0) {
  getBookData(bookName)
  .then((startingBookData) => {
    // range from 0 -> amount of chapters in this book
    const chapterList = [...Array(startingBookData.chapterCount).keys()]
    // make option for each
    const chapterOptions = chapterList.map(c => ({
      label: c + 1, 
      value: c + 1}
    ))

    formActions.setOptions("HopFieldsSet", "referenceFilter", {
      startingBookData, // proably should be different reducer TODO 
      chapterOptions,
    })
  })
  .catch(console.error)
}

