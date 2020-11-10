import React from "react"
import Form from '../../shared/elements/Form';
import Button from '../../shared/elements/Button';
import Select from '../../shared/groups/Select';

import classes from './scss/diagram-options-form.scss'

import { connect } from 'react-redux'
import {
  initialChapterOption,
  initialVerseOption,
  bookOptions,
  hopsCountOptions,
  allusionDirectionOptions,
} from '../../../constants/arc-diagram'


import {
  alertActions,
  formActions,
} from "../../../actions"

import Helpers from '../../../helpers/base-helpers'
import _ from "lodash"

/*
 * NOTE currently not using, but keeping on hand in case we switch from a simple input field back to
 * the selects
 */
class SelectReferenceFields extends React.Component {
  constructor (props) {
    super(props)




    this.state = {

    }

    this.selectStartingBook = this.selectStartingBook.bind(this)
    this.selectStartingChapter = this.selectStartingChapter.bind(this)
    this.selectStartingVerse = this.selectStartingVerse.bind(this)
    this.getSetOptions = this.getSetOptions.bind(this)
	}

	getSetOptions () {
	  const {index, hopOptions} = this.props

	  return Helpers.safeDataPath(hopOptions, `hopSet${index}`, {})
  }

  selectStartingBook (value, action) {
    // TODO iindex will be dynamic later
    // reinitialize chapter and verse when you select a book
    const startingChapter = initialChapterOption()
    const startingVerse = initialVerseOption()

    const params = {
      [`hopSet${this.props.index}`]: {
        startingBook: value,
        startingChapter,
        startingVerse,
      }
    }

    formActions.setOptions("HopFieldsSet", "referenceFilter", params)
  }
  selectStartingChapter (value, action) {
    // TODO index will be dynamic later
    // reinitialize  verse when you select a chapter
    const params = {[`hopSet${this.props.index}`]: {startingChapter: value}}
    formActions.setOptions("HopFieldsSet", "referenceFilter", params)
  }
  selectStartingVerse (value, action) {
    // TODO index will be dynamic later
    const params = {[`hopSet${this.props.index}`]: {startingVerse: value}}
    formActions.setOptions("HopFieldsSet", "referenceFilter", params)
  }



  componentDidMount () {
  }

  render () {
    const { 
      chapterOptions, verseOptions, allusionDirection, dataSet, hopsCount, filterByChapter, filterByVerse, 
      index,
      hopOptions 
    } = this.props

    const {
      startingChapter,
      startingBook,
      startingVerse,
    } = this.getSetOptions()

    return (
            <div className="ref-selects">
              <Select 
                options={bookOptions}
                className="book-select"
                onChange={this.selectStartingBook}
                currentOption={startingBook}
              />

              {filterByChapter && chapterOptions && (
                <Select 
                  options={chapterOptions}
                  onChange={this.selectStartingChapter}
                  currentOption={startingChapter}
                />
              )}
              {filterByVerse && verseOptions && (
                <Select 
                  options={verseOptions}
                  onChange={this.selectStartingVerse}
                  currentOption={startingVerse}
                />
              )}
            </div>
    )
  }
}

const mapStateToProps = state => {
  return {
    hopOptions: Helpers.safeDataPath(state.forms, "HopFieldsSet.referenceFilter.options")
  }
}

export default connect(mapStateToProps)(SelectReferenceFields)
