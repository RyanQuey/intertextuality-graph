import React from "react"

import Form from '../../shared/elements/Form';
import Button from '../../shared/elements/Button';
import Select from '../../shared/groups/Select';
import TextReferenceInput from '../../shared/groups/TextReferenceInput';

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

class HopFieldsSet extends React.Component {
  constructor (props) {
    super(props)




    this.state = {
      reference: {},

    }

    this.changeReference = this.changeReference.bind(this)
    this.getParams = this.getParams.bind(this)
	}

  componentDidMount () {
  }

	getParams () {
	  const {index, hopParams} = this.props

	  return Helpers.safeDataPath(hopParams, `hopSet${index}`, {})
  }

  changeReference (reference) {
    console.log("reference:", reference)
    const {startingBook, startingChapter, startingVerse} = reference

    const params = {
      [`hopSet${this.props.index}`]: reference
    }

    formActions.setParams("HopFieldsSet", "referenceFilter", params)
    if (reference.valid) {
      // TODO not yet implemented
      this.props.onChangeReference && this.props.onChangeReference(reference.referenceData)
    }

    // TODO move into redux store later
    this.setState({reference})
  }

  render () {
    const { 
      chapterOptions, verseOptions, allusionDirection, dataSet, hopsCount, filterByChapter, filterByVerse, 
      index,
      hopParams 
    } = this.props

    const {
      startingChapter,
      startingBook,
      startingVerse,
    } = this.getParams()

    const {reference} = this.state

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
            <div className="ref-input">
              <TextReferenceInput 
                label="Text"
                onChange={this.changeReference}
                reference={reference}
              />
            </div>
          </div>
          <div className="other-configs">
          </div>
        </div>
    )
  }
}

const mapStateToProps = state => {
  return {
    hopParams: Helpers.safeDataPath(state.forms, "HopFieldsSet.referenceFilter.params")
  }
}

export default connect(mapStateToProps)(HopFieldsSet)
