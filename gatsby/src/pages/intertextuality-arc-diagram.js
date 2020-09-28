import React from "react"
import { Link } from "gatsby"

import Layout from "../components/layout"
import Image from "../components/image"
import SEO from "../components/seo"


import { Vega } from 'react-vega';
// TODO for some reason changing this file does not really get updated by webpack hot reload
//import specBuilder from '../configs/intertextual-arc-spec';
import specBuilder from '../configs/intertextual-arc-spec-data-supplied';
import { Handler } from 'vega-tooltip';

/*
import edgesData from '../data/intertextuality-edges.json';
import targetVerticesData from '../data/intertextuality-vertices.json';
import sourceVerticesData from '../data/intertextuality-source-vertices.json';
import allVerticesData from '../data/intertextuality-all-vertices.json';
*/
import {getBookData} from '../helpers/book-helpers'
import {getChapterData} from '../helpers/chapter-helpers'
import {getVerticesForRef, getPathsForRef} from '../helpers/connection-helpers'

import Form from '../components/shared/elements/Form';
import Button from '../components/shared/elements/Button';
import Select from '../components/shared/groups/Select';

import AddConnectionForm from '../components/AddConnectionForm';
import books from '../data/books';
import {
  osisDataValue, 
  osisDataIsValid,
  startingBookFromOsis,
  startingChapterFromOsis,
  startingVerseFromOsis,
  endingBookFromOsis,
  endingChapterFromOsis,
  endingVerseFromOsis,
} from '../helpers/text-helpers'
import classes from './scss/arc-diagram.scss'

function handleHover(...args){
  console.log(args);
}

const signalListeners = { hover: handleHover };
const tooltipOptions = {
  theme: "dark"
}

const tooltip = new Handler(tooltipOptions).call
const apiUrl =  process.env.INTERTEXTUALITY_GRAPH_PLAY_API_URL || "http://localhost:9000"


// const verticesUrlBase = apiUrl + "/sources-for-ref-with-alluding-texts"
// const edgesUrlBase = apiUrl + "/paths-for-sources-starting-with-ref"

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
      refreshCounter: 0,
    }

    this.selectStartingBook = this.selectStartingBook.bind(this)
    this.selectStartingChapter = this.selectStartingChapter.bind(this)
    this.fetchVerticesAndEdges = this.fetchVerticesAndEdges.bind(this)
    this.refreshData = this.refreshData.bind(this)
    this.refreshDataWithCurrentState = this.refreshDataWithCurrentState.bind(this)
    this.triggerChangeSource = this.triggerChangeSource.bind(this)
  }

  componentDidMount () {
    this.refreshDataWithCurrentState()
  }

  // TODO maybe always just use current state?
  refreshDataWithCurrentState () {
    const { startingBook, startingChapter} = this.state
    this.refreshData(startingBook.value, startingChapter.value) 
  }

  // TODO not yet passing in verse
  refreshData (book, chapter, verse) {
    getBookData(book)
      .then((result) => {
        this.setState({startingBookData: result})
      })

    if (chapter) {
      getChapterData(book, chapter)
        .then((result) => {
          this.setState({startingChapterData: result})
        })
    }

    // get edges and vertices one-time
    this.fetchVerticesAndEdges(book, chapter, verse)
  }

  async fetchVerticesAndEdges (book, chapter, verse) {
    const [vertices, edges] = await Promise.all([
      getVerticesForRef(book, chapter, verse),
      getPathsForRef(book, chapter, verse),
    ])

    console.log("got data", {edges, vertices})
    this.setState({edges, vertices})
  }

  selectStartingBook (option) {
    this.setState({
      startingBook: option,
      // restart the chapter as well, since this book probably does not have the same count as the
      // previous one
      startingChapter: initialChapterOption,
    })

    const book = option.value
    this.refreshData(book, this.state.startingChapter.value)
  }

  selectStartingChapter (option) {
    this.setState({
      startingChapter: option
    })

    const chapter = option.value
    this.refreshData(this.state.startingBook.value, chapter)
  }

  /*
   * take osis data (full parsed object) of source text that is being added and show current
   * allusions to it
   *
   */ 
  triggerChangeSource (sourceOsisData) {
    const startingBook = startingBookFromOsis(sourceOsisData)
    const startingChapter = startingChapterFromOsis(sourceOsisData)

    this.selectStartingBook({value: startingBook, label: startingBook})
    this.selectStartingChapter({value: startingChapter, label: startingChapter})
  }

  render () {
    const { startingBook, startingChapter, startingBookData, startingChapterData, edges, vertices  } = this.state

    const spec = specBuilder({edges, nodes: vertices, books})

    let chapterOptions
    if (startingBookData) {
      const chapterList = [...Array(startingBookData.chapterCount).keys()]
      chapterOptions = chapterList.map(c => ({
        label: c + 1, 
        value: c + 1}
      ))
    }

    return (
      <Layout>
        <SEO title="Intertextuality Arc Diagram" />
          <div>
            <AddConnectionForm 
              triggerUpdateDiagram={this.refreshDataWithCurrentState}
              onChangeSource={this.triggerChangeSource}
            />
          </div>


        <div className={"configForm"}>
          <Form>
            <h2>Now showing:</h2>
            <div>
              Texts that allude to {startingBook.label} {startingChapter.label}
              <Select 
                options={bookOptions}
                onChange={this.selectStartingBook}
                currentOption={startingBook}
              />

              Chapter: ({startingChapter.label})
              {chapterOptions && (
                <Select 
                  options={chapterOptions}
                  onChange={this.selectStartingChapter}
                  currentOption={startingChapter}
                />
              )}
            </div>

          </Form>

        </div>
        <Vega 
          spec={spec} 
        />
      </Layout>
    )
  }
}

export default IArcDiagram
