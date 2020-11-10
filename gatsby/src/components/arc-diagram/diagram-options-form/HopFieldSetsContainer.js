import React from "react"

import Form from '../../shared/elements/Form';
import Button from '../../shared/elements/Button';
import Select from '../../shared/groups/Select';
import TextReferenceInput from '../../shared/groups/TextReferenceInput';
import HopFieldsSet from "./HopFieldsSet"

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

class HopFieldSetsContainer extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
    }

    this.changeReference = this.changeReference.bind(this)
    this.addHop = this.addHop.bind(this)
	}

  componentDidMount () {
  }

  addHop () {

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

  render () {
    const { hopsParams } = this.props

    const hopFieldSetsKeys = Object.keys(hopsParams)
    const hopCount = hopFieldSetsKeys.length
    const canAddHop = hopCount < 3

    return (
      <div>
        <div>
          {Object.keys(hopsParams).map((hopKey, index) => {
            const value = hopsParams[hopKey]

            return (
              <HopFieldsSet 
                index={index}
              />
            )

          })}
        </div>
        <div>
          <Button
            onClick={this.addHop}
            disabled={!canAddHop}
            small={true}
            rectangle={true}
          >
            Add Hop
          </Button>
        </div>
      </div>
    )
  }
}

const mapStateToProps = state => {
  return {
    // cannot access props here, so set defaults in a wrapper function (getParams)
    hopsParams: Helpers.safeDataPath(state.forms, "HopFieldsSet.referenceFilter.params", {hopSet0: {}})
  }
}

export default connect(mapStateToProps)(HopFieldSetsContainer)
