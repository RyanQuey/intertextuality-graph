import React from "react"
import Form from '../shared/elements/Form';
import Button from '../shared/elements/Button';
import Select from '../shared/groups/Select';

import classes from './scss/diagram-options-form.scss'

import {
  bookOptions,
  hopsCountOptions,
  dataSetOptions,
  allusionDirectionOptions,
} from '../../constants/arc-diagram'

import {
  alertActions,
} from "../../actions"


class HopFieldsSet extends React.Component {
  constructor (props) {
    super(props)




    this.state = {

    }
    

	}

  componentDidMount () {
  }

  render () {
    const { startingBook, startingChapter, startingVerse, chapterOptions, verseOptions, allusionDirection, dataSet, hopsCount, filterByChapter, filterByVerse } = this.props

    console.log("now filtering by chapter?", filterByChapter)


    return (
        <div className="hop-fields-set">
          <div className="ref-selects-configs">
            <h2>Now showing:</h2>
            <div>
              {chapterOptions && (
                <Button onClick={this.props.toggleFilterByChapter}>{filterByChapter ? "Filter by Book Only" : "Filter by Chapter"}</Button>
              )}
              {filterByChapter && verseOptions && (
                <Button onClick={this.props.toggleFilterByVerse}>{filterByVerse ? "Filter by Chapter Only" : "Filter by Verse"}</Button>
              )}
            </div>
            <div>
              <Select 
                options={allusionDirectionOptions}
                onChange={this.props.selectAllusionDirection}
                currentOption={allusionDirection}
              />
            </div>
            <div className="ref-selects">
              <Select 
                options={bookOptions}
                className="book-select"
                onChange={this.props.selectStartingBook}
                currentOption={startingBook}
              />

              {filterByChapter && chapterOptions && (
                <Select 
                  options={chapterOptions}
                  onChange={this.props.selectStartingChapter}
                  currentOption={startingChapter}
                />
              )}
              {filterByVerse && verseOptions && (
                <Select 
                  options={verseOptions}
                  onChange={this.props.selectStartingVerse}
                  currentOption={startingVerse}
                />
              )}
            </div>
          </div>
          <div className="other-configs">
          </div>
        </div>
    )
  }
}

export default HopFieldsSet
