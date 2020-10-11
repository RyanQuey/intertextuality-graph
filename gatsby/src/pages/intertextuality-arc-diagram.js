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
import {getVerticesForRef, getPathsForRef, getTextsRefAlludesTo, getTextsAlludedToByRef, extractNodesAndEdgesFromPaths, extractNodesAndEdgesFromMixedPaths} from '../helpers/connection-helpers'
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
import './scss/arc-diagram.scss'
import './scss/selected-item-info.scss'

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

const allusionDirectionOptions = [
  {
    label: "Text alludes to", 
    value: "alludes-to",
  },
  {
    label: "Texts Alluded to By", 
    value: "alluded-to-by",
  },
  // {
  // // TODO might just make these "alluded-to" or something, not sure yet. Make sure to prefer precision over performance though, performance is fine
  //// for synoptic gospels, or James > Gospels, or Hebrews > John.
  //   label: "Shared Source", 
  //   value: "shared-source", 
  // },
  // {
  //   label: "All", 
  //   value: "all",
  // },
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
      allusionDirection: allusionDirectionOptions[0],
      refreshCounter: 0,
      hopsCount: hopsCountOptions[0],
      dataSet: dataSetOptions[0],
      loadingBookData: false,
      loadingChapterData: false,
      loadingEdges: false,
      selectedNode: null,
      selectedEdge: null,
    }

    this.selectStartingBook = this.selectStartingBook.bind(this)
    this.selectStartingChapter = this.selectStartingChapter.bind(this)
    this.selectStartingVerse = this.selectStartingVerse.bind(this)
    this.selectDataSet = this.selectDataSet.bind(this)
    this.selectAllusionDirection = this.selectAllusionDirection.bind(this)
    this.fetchVerticesAndEdges = this.fetchVerticesAndEdges.bind(this)
    this.refreshData = this.refreshData.bind(this)
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
    this.refreshData()
  }


  downloadAsCSV () {
    const { startingBook, startingChapter, startingVerse, hopsCount } = this.state
    // this just gets used for the file name
    const refString = `${startingBook.value}.${startingChapter.value}.${startingVerse.value}-${hopsCount.value}hops`

    downloadGraphDataAsCSV(this.state.edges, this.state.vertices, refString)
  }

  /*
   * @param optionOverrides object to override current state
   * TODO not yet passing in verse
   */
  refreshData (paramOverrides = {}) {
    this.setState({
      loadingBookData: true, 
      loadingChapterData: true, 
      chapterOptions: false, 
      verseOptions: false
    })
    
    // merge current state with the options that are getting passed in
    // especially important if just barely recently setState
    const options = Object.assign({
      startingBook: this.state.startingBook.value, 
      startingChapter: this.state.startingChapter.value, 
      startingVerse: this.state.startingVerse.value, 
      hopsCount: this.state.hopsCount.value, 
      dataSet: this.state.dataSet.value, 
      allusionDirection: this.state.allusionDirection.value
    }, paramOverrides)
    
    // convert to format we can send to our api
    const queryOptions = {
      book: options.startingBook, 
      chapter: options.startingChapter, 
      verse: options.startingVerse, 
      hopsCount: options.hopsCount, 
      dataSet: options.dataSet, 
      allusionDirection: options.allusionDirection,
    }

    // start hitting our api
    getBookData(queryOptions.book)
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

    if (queryOptions.chapter) {
      getChapterData(queryOptions.book, queryOptions.chapter)
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
    this.fetchVerticesAndEdges(queryOptions)
  }

  async fetchVerticesAndEdges (queryOptions) {
    const {book, chapter, verse, hopsCount, dataSet, allusionDirection } = queryOptions
    this.setState({loadingEdges: true})
    
    const fetchFunc = allusionDirection == "alludes-to" ? getTextsRefAlludesTo : getTextsAlludedToByRef

    // const [vertices, edges, pathsWithValues] = await Promise.all([
    const [pathsWithValues] = await Promise.all([
      // getVerticesForRef(book, chapter, verse, hopsCount),
      // getPathsForRef(book, chapter, verse, hopsCount),
      fetchFunc(book, chapter, verse, hopsCount, dataSet),
    ])

    const [ edges, vertices ] = extractNodesAndEdgesFromMixedPaths(pathsWithValues)

    console.log("got data for ref", book, `${chapter}:${verse}`, " with hopsCount:", hopsCount, "===== edges and vertices", {edges, vertices})
    this.setState({
      loadingEdges: false,
      edges, 
      vertices,
    })
  }

  selectStartingBook (option, details, skipRefresh = false) {
    const startingChapter = initialChapterOption()
    const startingVerse = initialVerseOption()

    this.setState({
      startingBook: option,
      // restart the chapter as well, since this book probably does not have the same count as the
      // previous one
      startingChapter,
      // don't know the chapter, so no verse 
      startingVerse,
    })

    !skipRefresh && this.refreshData({
      startingBook: option.value, 
      startingChapter: startingChapter.value, 
      startingVerse: startingVerse.value,
    })
  }

  selectStartingChapter (option, details, skipRefresh = false) {
    const startingVerse = initialVerseOption()

    this.setState({
      startingChapter: option,
      startingVerse,
    })
    
    !skipRefresh && this.refreshData({startingVerse: startingVerse.value, startingChapter: option.value})
  }
  
  selectStartingVerse (option, details, skipRefresh = false) {
    this.setState({
      startingVerse: option,
    })
    
    !skipRefresh && this.refreshData({startingVerse: option.value})
  }

  /*
   * change number of times to go "out" on a connection edge
   */ 
  changeHopsCount (option, details, skipRefresh = false) {
    this.setState({
      hopsCount: option,
    })

    !skipRefresh && this.refreshData({hopsCount: option.value})
  }

  selectDataSet (option, details, skipRefresh = false) {
    this.setState({
      dataSet: option,
    })

    !skipRefresh && this.refreshData({dataSet: option.value})
  }
 
  selectAllusionDirection (option, details, skipRefresh = false) {
    this.setState({
      allusionDirection: option,
    })
    
    console.log("hi there", skipRefresh)

    !skipRefresh && this.refreshData({allusionDirection: option.value})
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
    // skipped the refresh in the next three, so can call manually all at once, to avoid raace conditions as well as do one instead of three queries
    const skipRefresh = true
    const startingBookOption = {value: startingBook, label: startingBook}
    const startingChapterOption = {value: startingChapter, label: startingChapter}
    const startingVerseOption = {value: startingVerse, label: startingVerse}
    
    this.selectStartingBook(startingBookOption, null, skipRefresh)
    this.selectStartingChapter(startingChapterOption, null, skipRefresh)
    this.selectStartingVerse(startingVerseOption, null, skipRefresh)
    
    this.refreshData({
      startingBook,
      startingChapter,
      startingVerse,
    })
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
    this.setState({
      selectedNode: value,
      selectedEdge: null,
    })
  }

  handleClickEdge(signalName, value) {
    console.log(signalName, value);
    this.setState({
      selectedNode: null,
      selectedEdge: value,
    })
  }

  handleClearDiagram(signalName, value) {
    console.log("clear selected nodes/edges", value);
    this.setState({
      selectedNode: null,
      selectedEdge: null,
    })
  }


  render () {
    const { allusionDirection, dataSet, startingBook, startingChapter, startingVerse, hopsCount, startingBookData, startingChapterData, edges, vertices, chapterOptions, verseOptions, loadingBookData, loadingChapterData, loadingEdges, selectedNode, selectedEdge  } = this.state

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

        <div className={"diagram-header"}>
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
                <div>
                  Alludes or Alluded to?
                  <Select 
                    options={allusionDirectionOptions}
                    onChange={this.selectAllusionDirection}
                    currentOption={allusionDirection}
                  />
                </div>

              </div>
              <Button onClick={this.downloadAsCSV} disabled={loadingEdges}>
                Download {startingBook.value} {startingChapter.value}:{startingVerse.value} as CSV
              </Button>


            </Form>

          </div>
          <div id="selected-item-info">
            {selectedEdge && (
              <div className="selected-item-container">
                <h2 className="selected-item-header">
                  Selected Connection
                </h2>
                <div className="selected-item-fields">
                  <div className="selected-item-field-container">
                    <div>Source Text:</div>
                    <div>{selectedEdge.edgeData.sourceSplitPassages[0]}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Alluding Text:</div>
                    <div>{selectedEdge.edgeData.alludingSplitPassages[0]}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Connection type:</div>
                    <div>{selectedEdge.edgeData.connectionType}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Confidence level:</div>
                    <div>{selectedEdge.edgeData.confidenceLevel}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Volume level:</div>
                    <div>{selectedEdge.edgeData.volumeLevel}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Description:</div>
                    <div>{selectedEdge.edgeData.description}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Source version:</div>
                    <div>{selectedEdge.edgeData.sourceVersion}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Beale Categories:</div>
                    <div>{selectedEdge.edgeData.bealeCategories}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Comments:</div>
                    <div>{selectedEdge.edgeData.comments}</div>
                  </div>
                </div>
              </div>
            )}
            {selectedNode && (
              <div className="selected-item-container">
                <h2 className="selected-item-header">
                  Selected Text
                </h2>
                <div className="selected-item-fields">
                  <div className="selected-item-field-container">
                    <div>Passage:</div>
                    <div> {selectedNode.nodeData.split_passages[0]}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Is source for how many passages here:</div>
                    <div> {selectedNode.nodeData.sourceDegree.count}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Alludes to how many passages here:</div>
                    <div> {selectedNode.nodeData.alludingDegree.count}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Comments:</div>
                    <div>{selectedNode.nodeData.comments}</div>
                  </div>
                  <div className="selected-item-field-container">
                    <div>Description:</div>
                    <div>{selectedNode.nodeData.description}</div>
                  </div>
                </div>
              </div>
            )}
          </div>
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
