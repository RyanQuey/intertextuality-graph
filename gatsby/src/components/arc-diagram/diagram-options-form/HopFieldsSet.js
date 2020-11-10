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
  initialAllusionDirection,
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
    this.setParams = this.setParams.bind(this)
    this.getParamKey = this.getParamKey.bind(this)
    this.changeAllusionDirection = this.changeAllusionDirection.bind(this)
	}

  componentDidMount () {
  }

	getParamKey () {
	  const {index} = this.props
	  return `hopSet${index}` 
  }


	getParams () {
	  const {hopParams} = this.props

    const params = Helpers.safeDataPath(hopParams, this.getParamKey(), {[this.getParamKey()]: {}})
    console.log("current params:", params)
    return params
  }

	setParams (key, value) {
    const params = this.getParams()

    // set it on the specific key, not overwriting the whole object belonging to this hops set
    _.set(params, `${this.getParamKey()}.${key}`, value)

    // set to redux
    formActions.setParams("HopFieldsSet", "referenceFilter", params)
  }

  changeAllusionDirection (option) {
    // merge allusion direction into existing parameters
    this.setParams("allusionDirection", option.value)

  }

  changeReference (reference) {
    console.log("reference:", reference)
    const {startingBook, startingChapter, startingVerse} = reference

    // merge current reference data, into existing parameters
    console.log("current params:", this.getParams())
    const params = Object.assign(this.getParams(), {...reference})

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
      chapterOptions, verseOptions, dataSet, hopsCount, filterByChapter, 
      index,
      hopParams 
    } = this.props

    let {
      startingChapter,
      startingBook,
      startingVerse,
      allusionDirection
    } = this.getParams()

    if (!allusionDirection) {
      allusionDirection = initialAllusionDirection().value
    }

    const {reference} = this.state

    return (
        <div className="hop-fields-set">
          <div className="ref-selects-configs">
            <h2>Now showing:</h2>
            <div>
            </div>
            <div>
              <Select 
                options={allusionDirectionOptions}
                onChange={this.changeAllusionDirection}
                currentOption={allusionDirectionOptions.find(op => op.value == allusionDirection)}
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
    // cannot access props here, so set defaults in a wrapper function (getParams)
    hopParams: Helpers.safeDataPath(state.forms, "HopFieldsSet.referenceFilter.params")
  }
}

export default connect(mapStateToProps)(HopFieldsSet)
