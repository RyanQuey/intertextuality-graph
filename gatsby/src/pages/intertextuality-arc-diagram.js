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
import {getVerticesForRef, getPathsForRef, getPathsWithValuesForRef, extractNodesAndEdgesFromPaths} from '../helpers/connection-helpers'

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

// allow between 1 and 4 hops
const hopsCountOptions = [...Array(4).keys()].map(hopCount => ({
  label: hopCount + 1, 
  value: hopCount + 1}
))

// make these functions, so even if the option list changes, these will stay the same. Advantages of
// immutable stuff
const initialChapterOption = () => ({label: 1, value: 1})
const initialVerseOption = () => ({label: 1, value: 1})

class IArcDiagram extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
      // Genesis
      startingBook: bookOptions[0],
      startingChapter: initialChapterOption(),
      startingVerse: initialVerseOption(),
      refreshCounter: 0,
      hopsCount: hopsCountOptions[0],
    }

    this.selectStartingBook = this.selectStartingBook.bind(this)
    this.selectStartingChapter = this.selectStartingChapter.bind(this)
    this.selectStartingVerse = this.selectStartingVerse.bind(this)
    this.fetchVerticesAndEdges = this.fetchVerticesAndEdges.bind(this)
    this.refreshData = this.refreshData.bind(this)
    this.refreshDataWithCurrentState = this.refreshDataWithCurrentState.bind(this)
    this.triggerChangeSource = this.triggerChangeSource.bind(this)
    this.changeHopsCount = this.changeHopsCount.bind(this)
  }

  componentDidMount () {
    this.refreshDataWithCurrentState()
  }

  // TODO maybe always just use current state?
  refreshDataWithCurrentState () {
    const { startingBook, startingChapter, startingVerse, hopsCount } = this.state
    this.refreshData(startingBook.value, startingChapter.value, startingVerse && startingVerse.value, hopsCount.value) 
  }

  // TODO not yet passing in verse
  refreshData (book, chapter, verse, hopsCount) {
    this.setState({chapterOptions: false, verseOptions: false})

    getBookData(book)
    .then((startingBookData) => {
      // range from 0 -> amount of chapters in this book
      const chapterList = [...Array(startingBookData.chapterCount).keys()]
      // make option for each
      const chapterOptions = chapterList.map(c => ({
        label: c + 1, 
        value: c + 1}
      ))

      this.setState({
        startingBookData,
        chapterOptions,
      })
    })

    if (chapter) {
      getChapterData(book, chapter)
      .then((startingChapterData) => {
      // range from 0 -> amount of verses in this chapter
      const verseList = [...Array(startingChapterData.verseCount).keys()]
      // make option for each
        const verseOptions = verseList.map(v => ({
          label: v + 1, 
          value: v + 1}
        ))

        this.setState({startingChapterData, verseOptions})
      })
    }

    // get edges and vertices one-time
    this.fetchVerticesAndEdges(book, chapter, verse, hopsCount)
  }

  async fetchVerticesAndEdges (book, chapter, verse, hopsCount) {
    // const [vertices, edges, pathsWithValues] = await Promise.all([
    const [pathsWithValues] = await Promise.all([
      // getVerticesForRef(book, chapter, verse, hopsCount),
      // getPathsForRef(book, chapter, verse, hopsCount),
      getPathsWithValuesForRef(book, chapter, verse, hopsCount),
    ])

    const [ edges, vertices ] = extractNodesAndEdgesFromPaths(pathsWithValues)
    console.log("got data for ref", book, chapter, verse, "hopsCount:", hopsCount, "edges and vertices", {edges, vertices})
    this.setState({edges, vertices})
  }

  selectStartingBook (option) {
    this.setState({
      startingBook: option,
      // restart the chapter as well, since this book probably does not have the same count as the
      // previous one
      startingChapter: initialChapterOption(),
      // don't know the chapter, so no verse 
      startingVerse: initialVerseOption(),
    })

    const { startingChapter, startingVerse, hopsCount } = this.state
    const startingBook = option
    this.refreshData(startingBook.value, startingChapter.value, startingVerse && startingVerse.value, hopsCount.value)
  }

  selectStartingChapter (option) {
    this.setState({
      startingChapter: option,
      startingVerse: initialVerseOption(),
    })

    const { startingBook, startingVerse, hopsCount } = this.state
    const startingChapter = option
    this.refreshData(startingBook.value, startingChapter.value, startingVerse && startingVerse.value, hopsCount.value)
  }

  selectStartingVerse (option) {
    this.setState({
      startingVerse: option
    })
    const { startingBook, startingChapter, hopsCount } = this.state
    const startingVerse = option
    this.refreshData(startingBook.value, startingChapter.value, startingVerse && startingVerse.value, hopsCount.value)
  }

  /*
   * change number of times to go "out" on a connection edge
   */ 
  changeHopsCount (option) {
    this.setState({
      hopsCount: option
    })

    const { startingBook, startingChapter, startingVerse} = this.state
    const hopsCount = option
    this.refreshData(startingBook.value, startingChapter.value, startingVerse && startingVerse.value, hopsCount.value)
  }

  /*
   * take osis data (full parsed object) of source text that is being added and show current
   * allusions to it
   *
   */ 
  triggerChangeSource (sourceOsisData) {
    const startingBook = startingBookFromOsis(sourceOsisData)
    const startingChapter = startingChapterFromOsis(sourceOsisData)
    const startingVerse = startingVerseFromOsis(sourceOsisData)

    console.log("changed source; setting diagram to ", startingBook)
    this.selectStartingBook({value: startingBook, label: startingBook})
    this.selectStartingChapter({value: startingChapter, label: startingChapter})
    this.selectStartingVerse({value: startingVerse, label: startingVerse})
  }

  render () {
    const { startingBook, startingChapter, startingVerse, hopsCount, startingBookData, startingChapterData, edges, vertices, chapterOptions, verseOptions  } = this.state

    const spec = specBuilder({edges, nodes: vertices, books})

    

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
            <div className="ref-selects-configs">
              <h2>Now showing:</h2>
              <div className="ref-selects">
                <div>
                  Texts that allude to &nbsp;
                </div>
                <Select 
                  options={bookOptions}
                  className="book-select"
                  onChange={this.selectStartingBook}
                  currentOption={startingBook}
                />

                {chapterOptions && (
                  <Select 
                    options={chapterOptions}
                    onChange={this.selectStartingChapter}
                    currentOption={startingChapter}
                  />
                )}
                {verseOptions && (
                  <Select 
                    options={verseOptions}
                    onChange={this.selectStartingVerse}
                    currentOption={startingVerse}
                  />
                )}
              </div>
            </div>
            <div className="other-configs">
              Hops:
                <Select 
                  options={hopsCountOptions}
                  onChange={this.selectHopsCount}
                  currentOption={hopsCount}
                />
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
