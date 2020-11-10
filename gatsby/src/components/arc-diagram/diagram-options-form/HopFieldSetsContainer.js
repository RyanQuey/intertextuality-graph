import React from "react"

import Form from '../../shared/elements/Form';
import Button from '../../shared/elements/Button';
import Select from '../../shared/groups/Select';
import TextReferenceInput from '../../shared/groups/TextReferenceInput';
import HopFieldsSet from "./HopFieldsSet"

import './scss/hop-field-sets-container.scss'

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

class HopFieldSetsContainer extends React.Component {
  constructor (props) {
    super(props)

    this.state = {
    }

    this.addHop = this.addHop.bind(this)
	}

  componentDidMount () {
    const base = this.props.hopsParams
    if (base == null) {
      const toSet = {
        hopSet0: defaultHopSetParams(),
        hopSet1: defaultHopSetParams(),
      }
      formActions.setParams("HopFieldsSet", "referenceFilter", toSet)
    }
  }

  addHop (e) {
    e && e.preventDefault && e.preventDefault() 
    
	  const {hopsParams} = this.props
    const hopFieldSetsKeys = Object.keys(hopsParams || {})
    const firstHopKey = hopFieldSetsKeys[0] || -1
    const secondHopKey = hopFieldSetsKeys[1]
    const lastHopIndex = hopFieldSetsKeys.length - 1
    const lastHopKey = hopFieldSetsKeys[hopFieldSetsKeys.length - 1]

    const toSet = {[`hopSet${lastHopIndex + 1}`]: defaultHopSetParams()}

    console.log("adding 1", `hopSet${lastHopKey + 1}`)
    console.log("now setting", toSet)

    formActions.setParams("HopFieldsSet", "referenceFilter", toSet)
  }

  render () {
    const { hopsParams } = this.props
    if (!hopsParams) {
      return null
    }

    const hopFieldSetsKeys = Object.keys(hopsParams)
    const hopCount = hopFieldSetsKeys.length
    const canAddHop = hopCount < 4

    return (
      <div className="hop-field-sets-container">
        <div >
          {hopsParams && Object.keys(hopsParams).map((hopKey, index) => {
            const value = hopsParams[hopKey]

            return (
              <HopFieldsSet 
                index={index}
                key={index}
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
            type="button"
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
    hopsParams: Helpers.safeDataPath(state.forms, "HopFieldsSet.referenceFilter.params", null)
  }
}

export default connect(mapStateToProps)(HopFieldSetsContainer)
