import React from "react"
import { Link } from "gatsby"

import Layout from "../components/layout"
import Image from "../components/image"
import SEO from "../components/seo"


import { Vega } from 'react-vega';
// TODO for some reason changing this file does not really get updated by webpack hot reload
import specBuilder from '../configs/intertextual-arc-spec';
import { Handler } from 'vega-tooltip';

import edgesData from '../data/intertextuality-edges.json';
import targetVerticesData from '../data/intertextuality-vertices.json';
import sourceVerticesData from '../data/intertextuality-source-vertices.json';
import allVerticesData from '../data/intertextuality-all-vertices.json';
import {getBookData} from '../helpers/book-helpers'
import {getChapterData} from '../helpers/chapter-helpers'

import Form from '../components/shared/elements/Form';
import Button from '../components/shared/elements/Button';
import Select from '../components/shared/groups/Select';
import books from '../data/books';
/*
 * Just a test component to see what kind of data I'm getting from my API, and how to manipulate it
 *
 */ 

function handleHover(...args){
  console.log(args);
}

const signalListeners = { hover: handleHover };
const tooltipOptions = {
  theme: "dark"
}

const tooltip = new Handler(tooltipOptions).call
const apiUrl = "http://localhost:9000"

const verticesUrlBase = apiUrl + "/sources-for-ref-with-alluding-texts"
const edgesUrlBase = apiUrl + "/paths-for-sources-starting-with-ref"

const bookOptions = books.map(b => ({
  label: b, 
  value: b}
))

const initialChapterOption = {label: 1, value: 1}

class IArcDiagram extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      // Genesis
      startingBook: bookOptions[0],
      startingChapter: initialChapterOption,
    }

    this.selectStartingBook = this.selectStartingBook.bind(this)
    this.selectStartingChapter = this.selectStartingChapter.bind(this)
  }

  componentDidMount () {
  }

  selectStartingBook (option) {
    this.setState({
      startingBook: option,
      // restart the chapter as well, since this book probably does not have the same count as the
      // previous one
      startingChapter: initialChapterOption,
    })

    getBookData(option.value)
      .then((result) => {
        this.setState({startingBookData: result})
      })
  }

  selectStartingChapter (option) {
    this.setState({
      startingChapter: option
    })

    getChapterData(this.state.selectStartingBook, option.value)
      .then((result) => {
        this.setState({startingChapterData: result})
      })
  }

  render () {
    const { startingBook, startingChapter, startingBookData, startingChapterData  } = this.state
    // const query = `book=${startingBook}&chapter=1&verse=1`
    const query = `book=${startingBook.value}&chapter=${startingChapter.value}`

    const edgesUrl = `${edgesUrlBase}?${query}`
    const verticesUrl = `${verticesUrlBase}?${query}`
    const spec = specBuilder(edgesUrl, verticesUrl, books)

    let chapterOptions
    if (startingBookData) {
      const chapterList = [...Array(startingBookData.chapterCount).keys()]
      chapterOptions = chapterList.map(c => ({
        label: c + 1, 
        value: c + 1}
      ))
    }

    console.log("starting book", startingBook)
    console.log("edgesUrl", edgesUrl)
    return (
      <Layout>
        <SEO title="Intertextuality Arc Diagram" />

        <Form>
          Find connections for book {startingBook.label} Chapter 1 (only shows where connection originates from {startingBook.label})
          <Select 
            options={bookOptions}
            onChange={this.selectStartingBook}
            currentOption={startingBook}
          />
          Starting chapter: ({startingChapter.label})

          {chapterOptions && (
            <Select 
              options={chapterOptions}
              onChange={this.selectStartingChapter}
              currentOption={startingChapter}
            />
          )}
        </Form>
        <Vega 
          spec={spec} 
        />
      </Layout>
    )
  }
}

export default IArcDiagram
