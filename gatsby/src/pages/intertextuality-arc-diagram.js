import React from "react"
import { Link } from "gatsby"

import Layout from "../components/layout"
import Image from "../components/image"
import SEO from "../components/seo"

import { Vega } from 'react-vega';
// TODO for some reason changing this file does not really get updated by webpack hot reload
//import specBuilder from '../configs/intertextual-arc-spec-data-url';
//import specBuilder from '../configs/intertextual-arc-spec-data-supplied';
import specBuilder from '../configs/intertextual-arc-spec-data-assumed';
import { Handler } from 'vega-tooltip';

/*
import edgesData from '../data/intertextuality-edges.json';
import targetVerticesData from '../data/intertextuality-vertices.json';
import sourceVerticesData from '../data/intertextuality-source-vertices.json';
import allVerticesData from '../data/intertextuality-all-vertices.json';
*/
import {getBookData} from '../helpers/book-helpers'
import {getChapterData} from '../helpers/chapter-helpers'
import {getVerticesForRef, getPathsForRef, getPathsWithValuesForRef, extractNodesAndEdgesFromPaths, extractNodesAndEdgesFromMixedPaths} from '../helpers/connection-helpers'
import {downloadAsCSV, downloadGraphDataAsCSV} from '../helpers/file-io-helpers'

import Form from '../components/shared/elements/Form';
import Button from '../components/shared/elements/Button';
import Select from '../components/shared/groups/Select';

import AddConnectionForm from '../components/AddConnectionForm';
import UploadCSVForm from '../components/UploadCSVForm';
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

const tooltipOptions = {
  theme: "dark"
}

const tooltip = new Handler(tooltipOptions).call
const apiUrl =  process.env.INTERTEXTUALITY_GRAPH_PLAY_API_URL || "http://localhost:9000"


// if using intertextual-arc-spec-data-url
// const verticesUrlBase = apiUrl + "/sources-for-ref-with-alluding-texts"
// const edgesUrlBase = apiUrl + "/paths-for-sources-starting-with-ref"

// if using intertextual-arc-spec-data-assumed
const spec = specBuilder({books})
const bookOptions = books.map(b => ({
  label: b, 
  value: b}
))

// for now, just user data or TSK data
const dataSetOptions = [
  // includes uploads and through the form
  {
    label: "All", 
    value: "all",
  },
  {
    label: "User Data", 
    value: "user",
  },
  {
    label: "Treasury of Scripture Knowledge", 
    value: "treasury-of-scripture-knowledge",
  },
]

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
      dataSet: dataSetOptions[0],
      loadingBookData: false,
      loadingChapterData: false,
      loadingEdges: false,
    }

    this.selectStartingBook = this.selectStartingBook.bind(this)
    this.selectStartingChapter = this.selectStartingChapter.bind(this)
    this.selectStartingVerse = this.selectStartingVerse.bind(this)
    this.selectDataSet = this.selectDataSet.bind(this)
    this.fetchVerticesAndEdges = this.fetchVerticesAndEdges.bind(this)
    this.refreshData = this.refreshData.bind(this)
    this.refreshDataWithCurrentState = this.refreshDataWithCurrentState.bind(this)
    this.triggerChangeSource = this.triggerChangeSource.bind(this)
    this.changeHopsCount = this.changeHopsCount.bind(this)
    this.downloadAsCSV = this.downloadAsCSV.bind(this)

    this.onParseError = this.onParseError.bind(this)
    this.onNewView = this.onNewView.bind(this)
    this.handleClearDiagram = this.handleClearDiagram.bind(this)
    this.handleClickNode = this.handleClickNode.bind(this)
    this.handleClickEdge = this.handleClickEdge.bind(this)
  }

  componentDidMount () {
    this.refreshDataWithCurrentState()
  }


  downloadAsCSV () {
    const { startingBook, startingChapter, startingVerse, hopsCount } = this.state
    // this just gets used for the file name
    const refString = `${startingBook.value}.${startingChapter.value}.${startingVerse.value}-${hopsCount.value}hops`

    downloadGraphDataAsCSV(this.state.edges, this.state.vertices, refString)
  }


  // TODO maybe always just use current state?
  refreshDataWithCurrentState () {
    const { startingBook, startingChapter, startingVerse, hopsCount, dataSet } = this.state
    this.refreshData(startingBook.value, startingChapter.value, startingVerse && startingVerse.value, hopsCount.value, dataSet.value) 
  }

  // TODO not yet passing in verse
  refreshData (book, chapter, verse, hopsCount, dataSet) {
    this.setState({
      loadingBookData: true, 
      loadingChapterData: true, 
      chapterOptions: false, 
      verseOptions: false
    })

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
        loadingBookData: false,
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

        this.setState({
          startingChapterData, 
          verseOptions,
          loadingChapterData: false,
           
        })
      })

    } else {
      this.setState({
        loadingChapterData: false,
      })
    }

    // get edges and vertices one-time
    this.fetchVerticesAndEdges(book, chapter, verse, hopsCount, dataSet)
  }

  async fetchVerticesAndEdges (book, chapter, verse, hopsCount, dataSet) {
    this.setState({loadingEdges: true})

    // const [vertices, edges, pathsWithValues] = await Promise.all([
    const [pathsWithValues] = await Promise.all([
      // getVerticesForRef(book, chapter, verse, hopsCount),
      // getPathsForRef(book, chapter, verse, hopsCount),
      getPathsWithValuesForRef(book, chapter, verse, hopsCount, dataSet),
    ])

    const [ edges, vertices ] = extractNodesAndEdgesFromMixedPaths(pathsWithValues)

    console.log("got data for ref", book, chapter, verse, "hopsCount:", hopsCount, "edges and vertices", {edges, vertices})
    this.setState({
      loadingEdges: false,
      edges, 
      vertices,
    })
  }

  selectStartingBook (option) {
    const startingBook = option
    const startingChapter = initialChapterOption()
    const startingVerse = initialVerseOption()

    this.setState({
      startingBook,
      // restart the chapter as well, since this book probably does not have the same count as the
      // previous one
      startingChapter,
      // don't know the chapter, so no verse 
      startingVerse,
    })

    const { hopsCount, dataSet } = this.state
    this.refreshData(startingBook.value, startingChapter.value, startingVerse && startingVerse.value, hopsCount.value, dataSet.value)
  }

  selectStartingChapter (option) {
    const startingChapter = option
    const startingVerse = initialVerseOption()
    this.setState({
      startingChapter,
      startingVerse,
    })

    const { startingBook, hopsCount, dataSet } = this.state
    this.refreshData(startingBook.value, startingChapter.value, startingVerse && startingVerse.value, hopsCount.value, dataSet.value)
  }

  selectStartingVerse (option) {
    this.setState({
      startingVerse: option
    })
    const { startingBook, startingChapter, hopsCount, dataSet } = this.state
    const startingVerse = option
    this.refreshData(startingBook.value, startingChapter.value, startingVerse && startingVerse.value, hopsCount.value, dataSet.value)
  }

  /*
   * change number of times to go "out" on a connection edge
   */ 
  changeHopsCount (option) {
    this.setState({
      hopsCount: option
    })

    const { startingBook, startingChapter, startingVerse, dataSet} = this.state
    const hopsCount = option
    this.refreshData(startingBook.value, startingChapter.value, startingVerse && startingVerse.value, hopsCount.value, dataSet.value)
  }

  selectDataSet (option) {
    this.setState({
      dataSet: option
    })

    const { startingBook, startingChapter, startingVerse, hopsCount} = this.state
    const dataSet = option
    this.refreshData(startingBook.value, startingChapter.value, startingVerse && startingVerse.value, hopsCount.value, dataSet.value)
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

  /*
   * They say that this is included in the documentation, but I have not been able to get it to work
   * https://github.com/vega/react-vega/tree/master/packages/react-vega#event-listeners
   * (ie Have never seen any errors return here)
   *
   */ 
  onParseError(...args) {
    console.log("any error?")
    console.error(args);
  }

  /*
   * I believe this is the Vega view object
   */
  onNewView(view) {
    console.log("New view:", view)
  }

  handleClickNode(signalName, value) {
    console.log(signalName, value);
  }

  handleClickEdge(signalName, value) {
    console.log(signalName, value);
  }

  handleClearDiagram(signalName, value) {
    console.log("clear selected nodes/edges", value);
  }


  render () {
    const { dataSet, startingBook, startingChapter, startingVerse, hopsCount, startingBookData, startingChapterData, edges, vertices, chapterOptions, verseOptions, loadingBookData, loadingChapterData, loadingEdges  } = this.state

    //for using intertextual-arc-spec-data-assumed
    //const spec = specBuilder({edges, nodes: vertices, books})
    const loading = loadingBookData || loadingChapterData || loadingEdges

    

    return (
      <Layout>
        <SEO title="Intertextuality Arc Diagram" />
          <div>
            <UploadCSVForm 
              triggerUpdateDiagram={this.refreshDataWithCurrentState}
            />
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
              <div>
                Hops:
                <Select 
                  options={hopsCountOptions}
                  onChange={this.changeHopsCount}
                  currentOption={hopsCount}
                />
              </div>
              <div>
                Data Set
                <Select 
                  options={dataSetOptions}
                  onChange={this.selectDataSet}
                  currentOption={dataSet}
                />
              </div>

            </div>
            <Button onClick={this.downloadAsCSV} disabled={loadingEdges}>
              Download {startingBook.value} {startingChapter.value}:{startingVerse.value} as CSV
            </Button>


          </Form>

        </div>
        <Vega 
          // note that you can pass in any of these props as well, e.g,. width="300" to set width of
          // view as 300px 
          // https://github.com/vega/vega-embed#options
          // docs say so here: https://github.com/vega/react-vega/tree/master/packages/react-vega#props-from-vega-embeds-api
          spec={spec} 
          // NOTE keys correspond to the signals defined in the spec
          signalListeners={{ 
            clickedNode: this.handleClickNode,
            clickedEdge: this.handleClickEdge,
            clear: this.handleClearDiagram,
          }}
          onError={this.onParseError} 
          onNewView={this.onNewView} 
          data={{edges, nodes: vertices}}
        />
      </Layout>
    )
  }
}

export default IArcDiagram
