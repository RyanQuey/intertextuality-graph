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
  defaultHopSetParams, 
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

    this.changeReference = this.changeReference.bind(this)
    this.getParams = this.getParams.bind(this)
    this.setParams = this.setParams.bind(this)
    this.getParamKey = this.getParamKey.bind(this)
    this.changeAllusionDirection = this.changeAllusionDirection.bind(this)
    this.isLastHopSet = this.isLastHopSet.bind(this)
    this.isFirstHopSet = this.isFirstHopSet.bind(this)
	}

  componentDidMount () {
  }

	getParamKey () {
	  const {index} = this.props
	  return `hopSet${index}` 
  }


	getParams () {
	  const {hopsParams} = this.props

    // set some basic defaults here, to avoid having to have as many conditionals everywhere else
    const params = Helpers.safeDataPath(hopsParams, this.getParamKey(), defaultHopSetParams())

    const cloned = _.cloneDeep(params)
    console.log("current cloned params:", cloned)

    return cloned
  }

	setParams (key, value) {
    const params = this.getParams()

    // set it on the specific key, not overwriting the whole object belonging to this hops set
    _.set(params, key, value)
    console.log("setting on", `${this.getParamKey()}.${key}`)

    // set to redux
    formActions.setParams("HopFieldsSet", "referenceFilter", {[this.getParamKey()]: params})
  }

  changeAllusionDirection (option) {
    // merge allusion direction into existing parameters
    this.setParams("allusionDirection", option.value)

  }

  changeReference (reference) {
    console.log("reference:", reference)
    const {startingBook, startingChapter, startingVerse} = reference

    // merge current reference data, into existing parameters
    this.setParams("reference", reference)

    if (reference.valid) {
      // TODO not yet implemented
      this.props.onChangeReference && this.props.onChangeReference(reference.referenceData)
    }
  }

  isLastHopSet () {
	  const {index, hopsParams} = this.props
    const hopFieldSetsKeys = Object.keys(hopsParams)
    const lastHopIndex = Math.max(hopFieldSetsKeys.length - 1, 1)
    console.log(lastHopIndex)

    return index == lastHopIndex
  }

  isFirstHopSet () {
	  const {index} = this.props
    return index == 0
  }

  render () {
    const { 
      chapterOptions, verseOptions, dataSet, 
      index,
    } = this.props

    const {
      allusionDirection,
      reference,
    } = this.getParams()
    console.log("index", index, "reference",reference)

    const {
      startingChapter,
      startingBook,
      startingVerse,
    } = reference

    return (
        <div className="hop-fields-set">
          <div className="ref-selects-configs">
            <div>
            </div>
            <div className="ref-input">
              <TextReferenceInput 
                label="Text"
                onChange={this.changeReference}
                reference={reference}
              />
            </div>

            {!this.isLastHopSet() && (
              <div>
                <Select 
                  options={allusionDirectionOptions}
                  onChange={this.changeAllusionDirection}
                  currentOption={allusionDirectionOptions.find(op => op.value == allusionDirection)}
                />
              </div>
            )}
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
    hopsParams: Helpers.safeDataPath(state.forms, "HopFieldsSet.referenceFilter.params", defaultHopSetParams())
  }
}

export default connect(mapStateToProps)(HopFieldsSet)
