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
  }

  addHop (e) {
    e && e.preventDefault && e.preventDefault() 
    
	  const {hopsParams} = this.props
    const hopFieldSetsKeys = Object.keys(hopsParams)
    const lastHopKey = hopFieldSetsKeys.length - 1
    const firstHopKey = hopFieldSetsKeys[0]

    const toSet = {[`hopSet${lastHopKey + 1}`]: defaultHopSetParams()}
    // if the first hop key (hopSet0) isn't real (ie, does not exist in the store) but is only being
    // recognized here because of set defaults, persist to store now
    if (_.isEqual(hopsParams[firstHopKey], {})) {
      toSet[firstHopKey] = defaultHopSetParams()
    }
    console.log("adding 1", `hopSet${lastHopKey + 1}`)
    console.log("now setting", toSet)

    formActions.setParams("HopFieldsSet", "referenceFilter", toSet)
  }

  render () {
    const { hopsParams } = this.props

    const hopFieldSetsKeys = Object.keys(hopsParams)
    const hopCount = hopFieldSetsKeys.length
    const canAddHop = hopCount < 3

    return (
      <div className="hop-field-sets-container">
        <div >
          {Object.keys(hopsParams).map((hopKey, index) => {
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
    hopsParams: Helpers.safeDataPath(state.forms, "HopFieldsSet.referenceFilter.params", {hopSet0: {}})
  }
}

export default connect(mapStateToProps)(HopFieldSetsContainer)
