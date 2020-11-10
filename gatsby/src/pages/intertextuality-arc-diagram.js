import React from "react"
import { connect } from 'react-redux'
import { Link } from "gatsby"
import { Vega } from 'react-vega';
import { Handler } from 'vega-tooltip';

import Layout from "../components/layout"
import Image from "../components/image"
import SEO from "../components/seo"
import Button from '../components/shared/elements/Button';

import DiagramOptionsForm from '../components/arc-diagram/diagram-options-form/DiagramOptionsForm';
import AddConnectionForm from '../components/arc-diagram/AddConnectionForm';
import SelectedItemInfo from '../components/arc-diagram/SelectedItemInfo';
import UploadCSVForm from '../components/arc-diagram/UploadCSVForm';


// TODO for some reason changing this file does not really get updated by webpack hot reload
//import specBuilder from '../configs/intertextual-arc-spec-data-url';
//import specBuilder from '../configs/intertextual-arc-spec-data-supplied';
import specBuilder from '../configs/intertextual-arc-spec-data-assumed';
import {
  initialChapterOption,
  initialVerseOption,
  allusionDirectionOptions,
  hopsCountOptions,
  dataSetOptions,
} from '../constants/arc-diagram'

import {
  alertActions,
  bookActions
} from "../actions"

import bookData from '../data/books';
/*
import edgesData from '../data/intertextuality-edges.json';
import targetVerticesData from '../data/intertextuality-vertices.json';
import sourceVerticesData from '../data/intertextuality-source-vertices.json';
import allVerticesData from '../data/intertextuality-all-vertices.json';
*/
import {getBookData} from '../helpers/book-helpers'
import {getChapterData} from '../helpers/chapter-helpers'
import { getTextsRefAlludesTo, getTextsAlludedToByRef, extractNodesAndEdgesFromMixedPaths} from '../helpers/connection-helpers'
import {downloadGraphDataAsCSV} from '../helpers/file-io-helpers'
import {
  startingBookFromOsis,
  startingChapterFromOsis,
  startingVerseFromOsis,
  endingBookFromOsis,
  endingChapterFromOsis,
  endingVerseFromOsis,
} from '../helpers/text-helpers'
import './scss/arc-diagram.scss'

import Helpers from '../helpers/base-helpers'
import _ from "lodash"

const apiUrl =  process.env.INTERTEXTUALITY_GRAPH_PLAY_API_URL || "http://localhost:9000"

// if using intertextual-arc-spec-data-assumed
const spec = specBuilder({books: bookData})

class IArcDiagram extends React.Component {
  constructor (props) {
    super(props)


    this.state = {
      hopsCount: hopsCountOptions[0],
      // all
      dataSet: dataSetOptions[0],
      
      loadingEdges: false,
      selectedNode: null,
      selectedEdge: null,
      tooltipOptions: {
        theme: "dark"
      }
    }

    // TODO when move to redux, can put a lot of this in store and move these functions to the child components
    this.selectDataSet = this.selectDataSet.bind(this)
    
    
    this.fetchVerticesAndEdges = this.fetchVerticesAndEdges.bind(this)
    this.refreshData = this.refreshData.bind(this)
    this.downloadAsCSV = this.downloadAsCSV.bind(this)

    this.onParseError = this.onParseError.bind(this)
    this.onNewView = this.onNewView.bind(this)
    this.handleClearDiagram = this.handleClearDiagram.bind(this)
    this.handleClickNode = this.handleClickNode.bind(this)
    this.handleClickEdge = this.handleClickEdge.bind(this)
    
    this.setTooltip = this.setTooltip.bind(this)
  }

  componentDidMount () {
    this.refreshData()

  // we have to do this after mounting component, so it runs client side, not server side
    this.setTooltip()

  }

  componentDidUpdate (previousProps, previousState) {
    // now in react, you use this instead of componentWillReceiveProps to trigger side effects after
    // props change

    if (!_.isEqual(previousProps.hopsParams, this.props.hopsParams)) {
      // don't do it this way anymore, manually trigger instead
      // this.refreshData()
    }

    // TODO if book or chapter change, refresh book data or chpater data
  }

  setTooltip () {
    this.setState({
      tooltip: new Handler(this.state.tooltipOptions).call
    })
  }
  
  
  downloadAsCSV () {
    const {hopSet0} = this.props.hopsParams
    const { 
      startingBook, 
      // not currently setting
      startingBookData, 
      startingChapter, 
      startingChapterData, 
      startingVerse, 
      allusionDirection,
    } = hopSet0

    const { hopsCount } = this.state

    // this just gets used for the file name
    const refString = `${allusionDirection.value}-${hopsCount.value}hops`
    
    downloadGraphDataAsCSV(this.state.edges, this.state.vertices, refString)
  }

  /*
   * @param optionOverrides object to override current state
   * TODO not yet passing in verse
   */
  refreshData (paramOverrides = {}) {
    const {hopSet0} = this.props.hopsParams

    // Need at least one!
    if (!hopSet0) {
      return 
    }

    const { 
      reference,
      allusionDirection,
    } = hopSet0

    if (!reference) {
      return 
    }
    const { 
      startingBook, 
      // not currently setting
      startingBookData, 
      startingChapter, 
      startingChapterData, 
      startingVerse, 
    } = reference
    
    // don't bother refreshing anything if we don't have some basic params set
    // just using starting book as a barometer see if anything is here yet
    if (!startingBook) {
      return 
    }

    // merge current state with the options that are getting passed in
    // especially important if just barely recently setState
    const options = Object.assign({
      startingBook, 
      startingChapter, 
      startingVerse, 
      hopsCount: this.state.hopsCount.value, 
      dataSet: this.state.dataSet.value, 
      allusionDirection,
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

    bookActions.fetchBookData(options.startingBook.value, 0)

    // TODO move this out of this component, into helpers and use store
    // start hitting our api
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
        })
      })
    }

    // get edges and vertices one-time
    this.fetchVerticesAndEdges(queryOptions)
  }

  async fetchVerticesAndEdges (queryOptions) {
    // at this point, these are not select dropdown options, these should be real values
    const {book, chapter, verse, hopsCount, dataSet, allusionDirection, 
    } = queryOptions
    this.setState({loadingEdges: true})
    
    const fetchFunc = allusionDirection == "alludes-to" ? getTextsRefAlludesTo : getTextsAlludedToByRef

    const params = {book, chapter, verse, hopsCount, dataSet}

    // const [vertices, edges, pathsWithValues] = await Promise.all([
    const [pathsWithValues] = await Promise.all([
      // getVerticesForRef(book, chapter, verse, hopsCount),
      // getPathsForRef(book, chapter, verse, hopsCount),
      fetchFunc(params),
    ])

    const [ edges, vertices ] = extractNodesAndEdgesFromMixedPaths(pathsWithValues)
    alertActions.newAlert({
      title: "Success:",
      message: `${pathsWithValues.length} connections found`,
      level: "SUCCESS",
      options: {timer: true},
    })

    console.log("got data for ref", book, `${chapter}:${verse}`, " with hopsCount:", hopsCount, "===== edges and vertices", {edges, vertices})
    this.setState({
      loadingEdges: false,
      edges, 
      vertices,
    })
  }

  selectDataSet (option, details, skipRefresh = false) {
    this.setState({
      dataSet: option,
    })

    !skipRefresh && this.refreshData({dataSet: option.value})
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

    const { 
      edges, 
      vertices, 
      loadingEdges,
      hopsCount,
      dataSet,
      tooltip, 
    } = this.state

    const {hopSet0} = this.props.hopsParams
    
    const invalidHopSetParams = !Helpers.safeDataPath(this.props.hopsParams, "hopSet0.reference.valid", false)

    //for using intertextual-arc-spec-data-assumed
    //const spec = specBuilder({edges, nodes: vertices, books})
    // not currently setting
    const loading = loadingEdges

    // keep this in a place that will work for server side rendering...it might not be here
    
    const canDownload = !invalidHopSetParams && !loading
    const directionText = hopSet0.allusionDirection == "alludes-to" ? "allusions to " : "source texts for "
    // it would be better to have the book/ch/v information, but commenting out for now as we refactor this code
    const downloadButtonText = canDownload ? `Download as CSV ${directionText} ${hopSet0.reference.osis}` : "Download as CSV"

    return (
      <Layout>
        <SEO title="Intertextuality Arc Diagram" />
          <div>
            <UploadCSVForm 
              triggerUpdateDiagram={this.refreshData}
            />
            <AddConnectionForm 
              triggerUpdateDiagram={this.refreshData}
              onChangeSource={this.triggerChangeSource}
            />
          </div>

        <div className={"diagram-header"}>
          <div className={"left-panel"}>
            <DiagramOptionsForm
              selectDataSet={this.selectDataSet}
              changeHopsCount={this.changeHopsCount}
              hopsCount={hopsCount}
              dataSet={dataSet}
              refreshData={this.refreshData}
            />
            <Button 
              onClick={this.downloadAsCSV} 
              disabled={loadingEdges}
              small={true}
              rectangle={true}
            >
              {downloadButtonText}
            </Button>
          </div>
          
          <SelectedItemInfo 
            selectedEdge={this.state.selectedEdge}
            selectedNode={this.state.selectedNode}
          />
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

const mapStateToProps = state => {
  return {
    hopsParams: Helpers.safeDataPath(state.forms, "HopFieldsSet.referenceFilter.params", {hopSet0: {}})
  }
}

export default connect(mapStateToProps)(IArcDiagram)
