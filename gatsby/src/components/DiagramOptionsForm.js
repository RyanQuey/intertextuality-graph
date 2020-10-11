import React from "react"
import Form from '../components/shared/elements/Form';
import Button from '../components/shared/elements/Button';
import Select from '../components/shared/groups/Select';

import classes from './scss/diagram-options-form.scss'

import bookData from '../data/books';
const bookOptions = bookData.map(b => ({
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



class DiagramOptionsForm extends React.Component {
  constructor (props) {
    super(props)

    this.selectDataSet = this.selectDataSet.bind(this)
    this.selectAllusionDirection = this.selectAllusionDirection.bind(this)
    this.changeHopsCount = this.changeHopsCount.bind(this)


    this.state = {
      allusionDirection: allusionDirectionOptions[0],
      // 1
      hopsCount: hopsCountOptions[0],
      // all
      dataSet: dataSetOptions[0],
    }

	}

  componentDidMount () {
  }


  /*
   * change number of times to go "out" on a connection edge
   */ 
  changeHopsCount (option, details, skipRefresh = false) {
    this.setState({
      hopsCount: option,
    })

    !skipRefresh && this.props.refreshData({hopsCount: option.value})
  }

  selectDataSet (option, details, skipRefresh = false) {
    this.setState({
      dataSet: option,
    })

    !skipRefresh && this.props.refreshData({dataSet: option.value})
  }
 
  selectAllusionDirection (option, details, skipRefresh = false) {
    this.setState({
      allusionDirection: option,
    })

    !skipRefresh && this.props.refreshData({allusionDirection: option.value})
  }

  render () {
    const { allusionDirection, dataSet, hopsCount } = this.state
    const { startingBook, startingChapter, startingVerse, chapterOptions, verseOptions } = this.props



    return (
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
                onChange={this.props.selectStartingBook}
                currentOption={startingBook}
              />

              {chapterOptions && (
                <Select 
                  options={chapterOptions}
                  onChange={this.props.selectStartingChapter}
                  currentOption={startingChapter}
                />
              )}
              {verseOptions && (
                <Select 
                  options={verseOptions}
                  onChange={this.props.selectStartingVerse}
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
        </Form>

      </div>
    )
  }
}

export default DiagramOptionsForm
